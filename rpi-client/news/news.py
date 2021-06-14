import requests
import xml.etree.ElementTree as ET

class News:

    def __init__(self):
        # 속보 (최신), 정치, 경제, 사회, 지역, 국제, 문화, 연예, 날씨
        self.rssURL = ["https://www.yonhapnewstv.co.kr/browse/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/politics/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/economy/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/society/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/local/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/international/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/culture/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/sports/feed/",
                       "https://www.yonhapnewstv.co.kr/category/news/weather/feed/"]

        # UserAgent
        self.header = {'User-Agent' : "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"}

    def getRecentNewsTitleList(self, targetURL):
        # Get Data
        responseData = requests.get(targetURL, headers=self.header)

        # Response To Text
        xmlData = responseData.text

        # Text to XML
        xmlTree = ET.fromstring(xmlData)

        # Find All News
        items = xmlTree.findall("channel/item")

        titleList = []

        for item in items:
            titleList.append(item.findtext("title"))

        return titleList



