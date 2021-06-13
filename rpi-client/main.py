import json
import platform
import sys
import netifaces
from datetime import datetime

import schedule
from PyQt5.QtCore import Qt, QTimer, QThread, pyqtSignal, pyqtSlot
from PyQt5.QtGui import QPixmap
from PyQt5.QtWidgets import *
from PyQt5 import uic
from flask import Flask, request, jsonify

from covid19.covid19 import Covid19
from weather.weather import Weather
from news.news import News
from face.face import Face
from face.db import FaceDatabase


uiFile = uic.loadUiType("ui/main.ui")[0]

class WindowClass(QMainWindow, uiFile):
    weather = Weather()
    covid19 = Covid19()
    news = News()

    def __init__(self):
        super().__init__()

        # 타이틀 바 없애기
        self.setWindowFlag(Qt.FramelessWindowHint)

        # 타이틀 설정
        self.setWindowTitle("MirrorAssistant")

        # UI 설정
        self.setupUi(self)

        # 알림 담을 List
        self.notificationList = []

        # Windows가 아니라면 (Test Enviroment)
        if platform.system() != 'Windows':
            # wlan0 NIC에서 IP 주소 불러오기
            ipAddress = netifaces.ifaddresses('wlan0')[netifaces.AF_INET][0]['addr']
            # 해당 주소 바탕으로 Text 설정
            self.label_deviceip.setText('<html><head/><body><p align="right"><span style=" font-size:8pt;">http://{}:5000/</span></p></body></html>'.format(ipAddress))
        # Windows라면
        else:
            # 해당 Text 비우기
            self.label_deviceip.setText('')

        # 시계 설정
        self.setDateTime()

        # 시계는 1초마다 새로고침
        self.clockTimer = QTimer()
        self.clockTimer.start(1000)
        self.clockTimer.timeout.connect(self.setDateTime)

        # 뉴스 설정
        self.setNewsData()

        # 뉴스는 1시간마다 새로고침
        self.newsTimer = QTimer()
        self.newsTimer.start(3600000)
        self.newsTimer.timeout.connect(self.setNewsData)

        # 코로나 설정
        self.setCovidData()

        # 코로나 정보는 매일 10:30에 새로고침
        schedule.every().day.at("10:30").do(self.setCovidData)
        self.covidTimer = QTimer(interval=1000, timeout=schedule.run_pending)
        self.covidTimer.start()
        schedule.run_pending()

        # 날씨 정보 설정
        self.setWeatherData()

        # 1시간마다 새로고침
        self.weatherTimer = QTimer()
        self.weatherTimer.start(3600000)
        self.weatherTimer.timeout.connect(self.setWeatherData)

        # 얼굴 인식 쓰레드 시작
        self.currentFace = None
        self.faceThread = FaceThread()
        self.faceThread.start()

        # 새 얼굴 인식 정보 Event 설정
        self.faceThread.currentFace.connect(self.setFaceUI)

        # API 서버 쓰레드 시작
        self.apiServerThread = ApiServerThread()
        self.apiServerThread.start()

        # 신규 얼굴 Layout Event
        self.apiServerThread.newPhotoAdded.connect(self.refreshFace)

        # 신규 Notification Event
        self.apiServerThread.newNotification.connect(self.newNotification)

    # 시간 설정
    def setDateTime(self):
        now = datetime.now()
        self.currentDate.setText(
            '<html><head/><body><p align="center"><span style=" font-size:22pt; font-weight:600; color:#ffffff;">' + now.strftime(
                "%Y년 %m월 %d일") +
            '</span></p></body></html>')
        self.currentTime.setText(
            '<html><head/><body><p align="center"><span style=" font-size:48pt; color:#ffffff;">' + now.strftime(
                "%H:%M:%S") +
            '</span></p></body></html>')

    def setNewsData(self):
        newsList = self.news.getRecentNewsTitleList(self.news.newRss)

        self.news_1.setText(
            '<html><head/><body><p><span style=" font-size:12pt; font-weight:600; color:#ffffff;">' + newsList[0]
            + '</span></p></body></html>')
        self.news_2.setText(
            '<html><head/><body><p><span style=" font-size:12pt; font-weight:600; color:#ffffff;">' + newsList[1]
            + '</span></p></body></html>')
        self.news_3.setText(
            '<html><head/><body><p><span style=" font-size:12pt; font-weight:600; color:#ffffff;">' + newsList[2]
            + '</span></p></body></html>')
        self.news_4.setText(
            '<html><head/><body><p><span style=" font-size:12pt; font-weight:600; color:#ffffff;">' + newsList[3]
            + '</span></p></body></html>')
        self.news_5.setText(
            '<html><head/><body><p><span style=" font-size:12pt; font-weight:600; color:#ffffff;">' + newsList[4]
            + '</span></p></body></html>')

    # 코로나 데이터 설정
    def setCovidData(self):
        self.news_6.setText(
            '<html><head/><body><p><span style=" font-size:12pt; color:#ffffff;">' + '코로나 확진자 수 : {0} / 사망자 수 : {1}'.format(
                self.covid19.getTodayDecideCount(), self.covid19.getTodayDeathCount()) +
            '</span></p></body></html>')

    def setWeatherData(self):
        weather = self.weather.getWeatherInfo()
        self.temp.setText(
            '<html><head/><body><p align="center"><span style=" font-size:48pt; color:#ffffff;">{0}℃</span></p></body></html>'.format(weather['temp']))

        self.weather_desc.setText('<html><head/><body><p align="center"><span style=" font-size:22pt;">{0}</span></p></body></html>'.format(weather['weatherKR']))

        weatherIcon = QPixmap()
        weatherIcon.loadFromData(self.weather.getImage(weather['icon']))
        self.weather_icon.setPixmap(weatherIcon)

        dustData = self.weather.getDustInfo()
        self.dust.setText(
            '<html><head/><body><p><span style=" font-size:12pt;">미세먼지 : {0} ({1}) / 초미세먼지 : {2} ({3}) </span></p></body></html>'
                .format(dustData['pm10'], self.weather.pm10Calculator(dustData['pm10']), dustData['pm25'], self.weather.pm25Calculator(dustData['pm25'])))

    # 얼굴에 따른 레이아웃 설정
    @pyqtSlot(int)
    def setFaceUI(self, data):
        # ID 반환됨
        self.currentFace = data

        print('New Face Detected :', data)

        # 설정 담을 Dictonary
        settingDict = {}

        try:
            faceDatabase = FaceDatabase()

            # 넘어온 ID로 getProfile
            profileData = faceDatabase.getProfile(data)

            settingDict['clock'] = profileData[3]
            settingDict['news'] = profileData[4]
            settingDict['weather'] = profileData[5]
            settingDict['noti'] = profileData[6]

            faceDatabase.close()
        # 에러 발생시
        except:
            # 딕셔너리에 기본 Layout
            settingDict['clock'] = 1
            settingDict['news'] = 3
            settingDict['weather'] = 0
            settingDict['noti'] = 2
        finally:
            # 레이아웃 설정
            print(settingDict)
            self.setLayout(settingDict)

    # 얼굴 파일 새로고침 요청
    @pyqtSlot(bool)
    def refreshFace(self, data):
        self.faceThread.requestRefresh = data

    # 새 알림 정보 추가
    # [시간] [타이틀] 메시지
    # [23:12] [ㅇㅇ] ㅇㅇ
    @pyqtSlot(dict)
    def newNotification(self, data):
        print('new notification received', data)

        # 앞에다 추가함
        self.notificationList.insert(0, data)

        # 최대 길이인 7개 이상인 경우
        if len(self.notificationList) > 7:
            # 마지막 요소 제거
            self.notificationList.pop()

        # 각 Label 설정
        notiLabels = [self.noti_1, self.noti_2, self.noti_3, self.noti_4, self.noti_5, self.noti_6, self.noti_7]
        i = 0
        for notification in self.notificationList:
            notiLabels[i].setText('<html><head/><body><p><span style=" font-size:12pt; font-weight:600;">[{}] [{}] {}</span></p></body></html>'.format(notification['time'], notification['title'], notification['msg']))
            i += 1

    # Layout Position 설정
    # 0 - 왼쪽 위 / 1 - 오른쪽 위 / 2 - 왼쪽 아래 / 3 - 오른쪽 아래
    def setLayout(self, settingDict):
        layoutPosition = [[10, 20], [530, 20], [10, 320], [530, 320]]
        self.timePanel.move(layoutPosition[settingDict['clock']][0], layoutPosition[settingDict['clock']][1])
        self.weatherPanel.move(layoutPosition[settingDict['weather']][0], layoutPosition[settingDict['weather']][1])
        self.newsPanel.move(layoutPosition[settingDict['news']][0], layoutPosition[settingDict['news']][1])
        self.textPanel.move(layoutPosition[settingDict['noti']][0], layoutPosition[settingDict['noti']][1])

# 얼굴 인식 Thread
class FaceThread(QThread):
    face = Face()
    currentFace = pyqtSignal(int)
    requestRefresh = False

    def run(self):
        # 이전 얼굴 설정 (기본 None)
        beforeFace = None

        # 무한 Loop
        while True:
            # 새로고침 요청이 없을 경우
            if not self.requestRefresh:
                # 얼굴 인식 처리
                newFace = self.face.recognitionFace()
                # 이전 얼굴이랑 인식 얼굴 번호가 다르면
                if beforeFace != newFace:
                    # 이전 얼굴을 새로 설정
                    beforeFace = newFace

                    # 변화를 Emit
                    self.currentFace.emit(beforeFace)
            # 새로고침 요청 있을경우
            else:
                # 이미지 리프레쉬
                self.face.refreshImageList()

                # 요청 False로
                self.requestRefresh = False

# API Server Thread
class ApiServerThread(QThread):
    flaskApp = Flask(__name__)
    flaskApp.config['JSON_AS_ASCII'] = False

    newPhotoAdded = pyqtSignal(bool)
    newNotification = pyqtSignal(dict)

    def run(self):
        # Default Page
        @self.flaskApp.route('/')
        def index():
            # 안내 문구 출력
            return jsonify(
                code=404,
                success=False,
                msg='Please Using Application'
            )

        # 새 얼굴 등록 - POST only (사진 첨부)
        # Parameter
            # photoFile (사진 파일)
        @self.flaskApp.route('/newface', methods=['POST'])
        def addNewFace():
            # Param Json으로 로드
            params = json.loads(request.get_data(), encoding='utf-8')

            # 업로드한 사진 가져오기
            photoFile = request.files['photoFile']

            # 사진이 제대로 안 가져와진 경우
            if type(photoFile) is None:
                # 에러
                return jsonify(
                    code=500,
                    success=False,
                    msg='Empty File'
                )

            try:
                fileName = datetime.today().strftime("%Y%m%d%H%M%S")
                # 현재 날짜와 시간을 이름으로 faceimg 폴더에 저장
                photoFile.save('./faceimg/' + fileName + '.jpg')

                faceDB = FaceDatabase()
                faceDB.addNewProfile(params['name'], fileName)
                faceDB.close()

                # 새 사진 추가 알림
                self.newPhotoAdded.emit(True)

                # 성공
                return jsonify(
                    code=200,
                    success=True,
                    msg='OK'
                )
            except:
                # 에러 발생시 500
                return jsonify(
                    code=500,
                    success=False,
                    msg='Error'
                )

        # 얼굴 레이아웃 설정
        # Parameter
            # facename (얼굴 파일명)
            # clock (시계 위치)
            # news (뉴스 위치)
            # noti (알림 표시 위치)
        # 0 - 왼쪽 위 / 1 - 오른쪽 위 / 2 - 왼쪽 아래 / 3 - 오른쪽 아래
        @self.flaskApp.route('/setfacelayout', methods=['POST'])
        def setWindowLayout():
            # Param Json으로 로드
            params = json.loads(request.get_data(), encoding='utf-8')

            # 길이 0이면 500
            if len(params) == 0:
                return jsonify(
                    code=500,
                    success=False,
                    msg='Empty Parameter'
                )

            try:
                faceDB = FaceDatabase()
                faceDB.updateProfile(params['id'], params['clock'], params['news'],  params['weather'], params['noti'])
                faceDB.close()

                # 성공
                return jsonify(
                    code=200,
                    success=True,
                    msg='OK'
                )
            # 파일 Not Found
            except FileNotFoundError:
                return jsonify(
                    code=500,
                    success=False,
                    msg='File Not Found'
                )
            # 이외에는 Error
            except:
                return jsonify(
                    code=500,
                    success=False,
                    msg='Error'
                )

        # 얼굴 설정 리스트 불러오기
        @self.flaskApp.route('/getfacelist')
        def getFaceList():
            faceDB = FaceDatabase()
            allProfile = faceDB.getAllProfile()
            faceDB.close()

            returnList = []
            for profile in allProfile:
                returnDict = {}
                returnDict['id'] = profile[0]
                returnDict['name'] = profile[1]
                returnDict['filename'] = profile[2]
                returnDict['clock'] = profile[3]
                returnDict['news'] = profile[4]
                returnDict['weather'] = profile[5]
                returnDict['noti'] = profile[6]
                returnDict['create'] = profile[7]
                returnList.append(returnDict)

            return jsonify(
                code=200,
                success=True,
                facelist=returnList,
                msg='OK'
            )

        # 안드로이드 Client에서 MirrorAssistant 체크
        @self.flaskApp.route('/androidCheck')
        def mirrorAssistantCheck():
            return jsonify(
                code=200,
                success=True,
                msg='OK'
            )

        # Notification 받기
        @self.flaskApp.route('/newnoti', methods=['POST'])
        def newNotification():
            # Param Json으로 로드
            params = json.loads(request.get_data(), encoding='utf-8')

            # 길이 0이면 500
            if len(params) == 0:
                return jsonify(
                    code=500,
                    success=False,
                    msg='Empty Parameter'
                )

            # 알림 정보를 UI에 넘김
            notiDict = {}
            notiDict['title'] = params['title']
            notiDict['msg'] = params['msg']
            notiDict['time'] = params['time']
            self.newNotification.emit(notiDict)

        # Flask Run
        sys.exit(self.flaskApp.run(host="0.0.0.0", debug=True, use_reloader=False))

if __name__ == "__main__":
    app = QApplication(sys.argv)
    mainWindow = WindowClass()
    mainWindow.show()
    sys.exit(app.exec_())
