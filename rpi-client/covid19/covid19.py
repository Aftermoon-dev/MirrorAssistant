import requests
from datetime import datetime, timedelta
import xml.etree.ElementTree as ET
from urllib import parse


class Covid19:
    items = None
    covid19Url = None
    c19Key = None
    covid19Url = None
    covid19QueryParams = None

    def __init__(self):
        # 오늘 날짜
        todayDate = datetime.today()

        if int(todayDate.strftime("%H%M%S")) > 103000:
            startDate = (todayDate - timedelta(days=1)).strftime("%Y%m%d")
            endDate = todayDate.strftime("%Y%m%d")
        else:
            startDate = (todayDate - timedelta(days=2)).strftime("%Y%m%d")
            endDate = (todayDate - timedelta(days=1)).strftime("%Y%m%d")

        # API URL
        self.covid19Url = 'http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson'

        # Service Key 불러오기
        c19KeyFile = open('./covid19/servicekey.txt', 'r')
        self.c19Key = c19KeyFile.readline()

        # Parameters
        covid19QueryParams = '?' + parse.urlencode({parse.quote_plus('ServiceKey'): self.c19Key,
                                                    parse.quote_plus('pageNo'): '1',
                                                    parse.quote_plus('numOfRows'): '10',
                                                    parse.quote_plus('startCreateDt'): startDate,
                                                    parse.quote_plus('endCreateDt'): endDate})

        # Response
        covid19XMLData = requests.get(self.covid19Url + covid19QueryParams).text

        # Text to XML
        covid19Tree = ET.fromstring(covid19XMLData)

        # Get Item (코로나 19 API는 가장 최신 데이터가 맨 앞에)
        self.items = covid19Tree.findall('body/items/item')

    def updateData(self):
        # 오늘 날짜
        todayDate = datetime.today()

        print(todayDate.strftime("%H%M%S"))

        if int(todayDate.strftime("%H%M%S")) > 103000:
            startDate = (todayDate - timedelta(days=1)).strftime("%Y%m%d")
            endDate = todayDate.strftime("%Y%m%d")
        else:
            startDate = (todayDate - timedelta(days=2)).strftime("%Y%m%d")
            endDate = (todayDate - timedelta(days=1)).strftime("%Y%m%d")


        covid19QueryParams = '?' + parse.urlencode({parse.quote_plus('ServiceKey'): self.c19Key,
                                                    parse.quote_plus('pageNo'): '1',
                                                    parse.quote_plus('numOfRows'): '10',
                                                    parse.quote_plus('startCreateDt'): startDate,
                                                    parse.quote_plus('endCreateDt'): endDate})

        # Response
        covid19XMLData = requests.get(self.covid19Url + covid19QueryParams).text

        # Text to XML
        covid19Tree = ET.fromstring(covid19XMLData)

        # Get Item (코로나 19 API는 가장 최신 데이터가 맨 앞에)
        self.items = covid19Tree.findall('body/items/item')

    def getTodayDecideCount(self):
        return int(self.items[0].find("decideCnt").text) - int(self.items[1].find("decideCnt").text)

    def getTodayDeathCount(self):
        return int(self.items[0].find("deathCnt").text) - int(self.items[1].find("deathCnt").text)
