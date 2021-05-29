from datetime import timedelta, date

import requests
from bs4 import BeautifulSoup
import time
import json


class Weather:
    def __init__(self):
        # Service Key 불러오기
        serviceKeyFile = open('./weather/servicekey.txt', 'r')
        self.weatherKey = serviceKeyFile.readline()

        # base_date와 base_time을 위한 변수 선언
        self.time_date_Weather = time.strftime('%Y%m%d', time.localtime(time.time()))
        self.time_date_Dust = time.strftime('%Y-%m-%d', time.localtime(time.time()))

    def getWeatherHour(self):
        hours = (date.today() - timedelta(hours=2)).strftime('%H00')
        return hours

    def pm10Calculator(self, pm10):
        pm10 = int(pm10)

        if 0 <= pm10 <= 30:
            return '좋음'
        elif 31 <= pm10 <= 80:
            return '보통'
        elif 81 <= pm10 <= 150:
            return '나쁨'
        elif 151 <= pm10:
            return '매우 나쁨'
        else:
            return '알 수 없음'

    def pm25Calculator(self, pm25):
        pm25 = int(pm25)

        if 0 <= pm25 <= 15:
            return '좋음'
        elif 16 <= pm25 <= 35:
            return '보통'
        elif 36 <= pm25 <= 75:
            return '나쁨'
        elif 76 <= pm25:
            return '매우 나쁨'
        else:
            return '알 수 없음'

    def getWeatherInfo(self):
        # 날씨 크롤링을 위한 요청 메시지값들

        url = 'http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst'
        queryParams_weather = '?' + \
                              'ServiceKey=' + self.weatherKey + \
                              '&numOfRows=' + '5' + \
                              '&pageNo=' + '1' + \
                              '&dataType=' + 'JSON' + \
                              '&base_date=' + self.time_date_Weather + \
                              '&base_time=' + self.getWeatherHour() + \
                              '&nx=' + '62' + \
                              '&ny=' + '124'

        # 날씨 크롤링 출력
        result_weather = requests.get(url + queryParams_weather)
        bs_obj_weather = BeautifulSoup(result_weather.content, "html.parser")

        weatherJson = json.loads(str(bs_obj_weather))
        weatherDataList = weatherJson['response']['body']['items']['item']

        returnData = {}
        for weatherData in weatherDataList:
            category = weatherData['category']
            if category == 'PTY':
                returnData['PTY'] = weatherData['obsrValue']
            elif category == 'REH':
                returnData['REH'] = weatherData['obsrValue']
            elif category == 'RN1':
                returnData['RN1'] = weatherData['obsrValue']
            elif category == 'T1H':
                returnData['T1H'] = weatherData['obsrValue']
            elif category == 'UUU':
                returnData['UUU'] = weatherData['obsrValue']
            elif category == 'VEC':
                returnData['VEC'] = weatherData['obsrValue']
            elif category == 'VVV':
                returnData['VVV'] = weatherData['obsrValue']
            elif category == 'WSD':
                returnData['WSD'] = weatherData['obsrValue']

        return returnData

    def getDustInfo(self):
        # 미세먼지 크롤링을 위한 요청 메시지값들
        url = 'http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty'
        queryParams_dust = '?' + \
                           'ServiceKey=' + self.weatherKey + \
                           '&returnType=' + 'JSON' + \
                           '&numOfRows=' + '1' + \
                           '&pageNo=' + '1' + \
                           '&stationName=복정동' + \
                           '&dataTerm=DAILY&ver=1.0'

        # 미세먼지 크롤링 출력
        result_dust = requests.get(url + queryParams_dust)
        bs_obj_dust = BeautifulSoup(result_dust.content, "html.parser")

        dustJson = json.loads(str(bs_obj_dust))

        dustDataList = dustJson['response']['body']['items']

        returnData = {}
        for dustData in dustDataList:
            returnData['so2'] = dustData['so2Value']
            returnData['co'] = dustData['coValue']
            returnData['o3'] = dustData['o3Value']
            returnData['no2'] = dustData['no2Value']
            returnData['pm10'] = dustData['pm10Value']
            returnData['pm25'] = dustData['pm25Value']
            break

        return returnData