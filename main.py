import PyQt5
from covid19.covid19 import Covid19
from news.news import News

# 코로나 API
covid19 = Covid19()
print('오늘 확진자 수 :', covid19.getTodayDecideCount())
print('오늘 사망자 수 :', covid19.getTodayDeathCount())

# 뉴스 정보 가져오기
news = News()
print('최신 뉴스 :', news.getRecentNewsTitleList(news.newRss))
