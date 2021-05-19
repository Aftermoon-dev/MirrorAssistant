import requests
import xml.etree.ElementTree as ET

class News:

    def __init__(self):
        # 신규
        self.newRss = "https://www.yonhapnewstv.co.kr/browse/feed/"

        # 정치
        self.polRss = "https://www.yonhapnewstv.co.kr/category/news/politics/feed/"

        # 경제
        self.ecoRss = "https://www.yonhapnewstv.co.kr/category/news/economy/feed/"

        # 사회
        self.socRss = "https://www.yonhapnewstv.co.kr/category/news/society/feed/"

        # 지역
        self.locRss = "https://www.yonhapnewstv.co.kr/category/news/local/feed/"

        # 국제
        self.intRss = "https://www.yonhapnewstv.co.kr/category/news/international/feed/"

        # 문화
        self.culRss = "https://www.yonhapnewstv.co.kr/category/news/culture/feed/"

        # 연예
        self.spoRss = "https://www.yonhapnewstv.co.kr/category/news/sports/feed/"

        # 날씨
        self.weaRss = "https://www.yonhapnewstv.co.kr/category/news/weather/feed/"

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



