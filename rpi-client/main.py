import sys
import datetime

import schedule
from PyQt5.QtCore import Qt, QTimer, QThread, pyqtSignal, pyqtSlot
from PyQt5.QtGui import QPixmap
from PyQt5.QtWidgets import *
from PyQt5 import uic
from covid19.covid19 import Covid19
from weather.weather import Weather
from news.news import News
from face.face import Face

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

        self.setupUi(self)

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

        # 코로나 정보는  매일 10:30에 새로고침
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
        self.faceThread = FaceThread()
        self.faceThread.start()
        self.faceThread.currentFace.connect(self.setFaceUI)

    # 시간 설정
    def setDateTime(self):
        now = datetime.datetime.now()
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

    @pyqtSlot(str)
    def setFaceUI(self, data):
        print(data)

# 얼굴 인식 Thread
class FaceThread(QThread):
    face = Face()
    currentFace = pyqtSignal(str)

    def run(self):
        # 이전 얼굴 설정 (기본 None)
        beforeFace = None

        # 무한 Loop
        while True:
            # 이전 얼굴이랑 인식 얼굴 파일명이 다르면
            if beforeFace != self.face.recognitionFace():
                # 이전 얼굴을 새로 설정
                beforeFace = self.face.recognitionFace()

                # 변화를 Emit
                self.currentFace.emit(beforeFace)

if __name__ == "__main__":
    app = QApplication(sys.argv)
    mainWindow = WindowClass()
    mainWindow.show()
    sys.exit(app.exec_())
