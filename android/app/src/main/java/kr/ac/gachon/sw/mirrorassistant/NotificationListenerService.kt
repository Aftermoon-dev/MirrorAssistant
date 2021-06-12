package kr.ac.gachon.sw.mirrorassistant

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListenerService: NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val notification = sbn!!.notification
        val extras = notification.extras

        Log.d("Notification", "Notification : " +
                "${extras.getString(Notification.EXTRA_TITLE)}" +
                "${extras.getString(Notification.EXTRA_TEXT)}")
    }
}