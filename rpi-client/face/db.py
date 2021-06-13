import sqlite3

class FaceDatabase:
    def __init__(self):
        self.database = sqlite3.connect('./face/facedb.db')
        self.cursor = self.database.cursor()
        self.cursor.execute("CREATE TABLE if not exists profile(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                            "name TEXT NOT NULL, imgName TEXT, clock INTEGER DEFAULT(1), news INTEGER DEFAULT(3), weather INTEGER DEFAULT(0), noti INTEGER DEFAULT(2), "
                            "createAt datetime DEFAULT(DATETIME('now', 'localtime')));")
        self.database.commit()

    def close(self):
        self.database.close()

    def addNewProfile(self, name, imgName):
        self.cursor.execute("INSERT INTO profile (name, imgName) VALUES('{}', '{}');".format(name, imgName))
        self.database.commit()

    def deleteProfile(self, _id):
        self.cursor.execute("DELETE from profile where _id = '{}';".format(_id))
        self.database.commit()

    def updateProfile(self, _id, clock, news, weather, noti):
        self.cursor.execute("UPDATE profile set clock = '{}', news = '{}', weather = '{}', noti = '{}' where _id = '{}';".format(clock, news, weather, noti, _id))
        self.database.commit()

    def getAllProfile(self):
        self.cursor.execute("SELECT * from profile")
        return self.cursor.fetchall()

    def getProfile(self, _id):
        self.cursor.execute("SELECT * from profile where _id = '{}';".format(_id))
        return self.cursor.fetchall()
