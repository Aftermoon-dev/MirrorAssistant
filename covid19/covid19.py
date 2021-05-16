import requests
from datetime import date, timedelta
import xml.etree.ElementTree as ET
from urllib import parse

# 어제 날짜
yesterdayDate = date.today() - timedelta(days=1)

# 오늘 날짜
todayDate = date.today()

# API URL
covid19Url = 'http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson'

# Service Key 불러오기
c19KeyFile = open('./covid19/servicekey.txt', 'r')
c19Key = c19KeyFile.readline()

# Parameters
covid19QueryParams = '?' + parse.urlencode({parse.quote_plus(
    'ServiceKey'): c19Key,
                                            parse.quote_plus('pageNo'): '1', parse.quote_plus('numOfRows'): '10',
                                            parse.quote_plus('startCreateDt'): yesterdayDate.strftime("%Y%m%d"),
                                            parse.quote_plus('endCreateDt'): todayDate.strftime("%Y%m%d")})

# Request
responseData = requests.get(covid19Url + covid19QueryParams)

# Response To Text
covid19XMLData = responseData.text

# Text to XML
covid19Tree = ET.fromstring(covid19XMLData)

# Get Item (코로나 19 API는 가장 최신 데이터가 맨 앞에)
items = covid19Tree.findall('body/items/item')


def getcovid19todaydecidenum():
    return int(items[0].find("decideCnt").text) - int(items[1].find("decideCnt").text)


def getcovid19todaydeathnum():
    return int(items[0].find("deathCnt").text) - int(items[1].find("deathCnt").text)
