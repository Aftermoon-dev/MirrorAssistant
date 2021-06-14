package kr.ac.gachon.sw.mirrorassistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import kr.ac.gachon.sw.mirrorassistant.R
import kr.ac.gachon.sw.mirrorassistant.network.APICall
import kr.ac.gachon.sw.mirrorassistant.network.BaseResponse
import kr.ac.gachon.sw.mirrorassistant.network.RetrofitClient
import kr.ac.gachon.sw.mirrorassistant.util.Preferences
import kr.ac.gachon.sw.mirrorassistant.util.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

class NotificationListenerService: NotificationListenerService() {
    private var retrofitClient: Retrofit? = null
    private var apiCall: APICall? = null
    private var pref: Preferences? = null
    private var beforeSbn: StatusBarNotification? = null

    override fun onCreate() {
        Log.d("NotificationListener", "onCreate")

        pref = Preferences(this)
        if(pref!!.enableNoti) {
            retrofitClient = RetrofitClient.getCurrentRetrofitClient()
            if (retrofitClient == null) {
                Log.d("NotificationListener", "Client Null -> StopSelf")
                stopSelf()
            }
            else {
                Log.d("NotificationListener", "Client Not Null!")
                apiCall = retrofitClient!!.create(APICall::class.java)
                super.onCreate()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Connected")

        if(retrofitClient == null) {
            retrofitClient = RetrofitClient.getCurrentRetrofitClient()
            if (retrofitClient == null) {
                return
            }
            else {
                Log.d("NotificationListener", "Client Not Null!")
                apiCall = retrofitClient!!.create(APICall::class.java)

            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationListener", "Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if(retrofitClient == null) {
            retrofitClient = RetrofitClient.getCurrentRetrofitClient()
            if (retrofitClient == null) {
                return
            }
            else {
                Log.d("NotificationListener", "Client Not Null!")
                apiCall = retrofitClient!!.create(APICall::class.java)
            }
        }

        if(sbn != null) {
            if(sbn.tag == null) return
            if(beforeSbn != null && beforeSbn!! == sbn) return

            beforeSbn = sbn

            val notification = sbn.notification
            val extras = notification.extras

            Log.d("NotificationListener", sbn.packageName)
            val appName = Util.getApplicationNameFromPackageName(this, sbn.packageName)
            val title = extras.getString(Notification.EXTRA_TITLE)
            var text = extras.getCharSequence(Notification.EXTRA_TEXT)
            val subtext = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

            Log.d("NotificationListener", "AppName $appName Title $title Text $text SubText $subtext")

            if(!text.isNullOrEmpty() && text != "null" && !subtext.isNullOrEmpty() && subtext != "null") {
                text = "$text $subtext"
            }

            if (pref!!.enableNoti && !title.isNullOrEmpty() && !text.isNullOrEmpty() && !appName.isNullOrEmpty() && title != "null" && text != "null")
                    sendNotification(sbn.postTime, appName, title, text.toString())
        }
    }

    private fun sendNotification(time: Long, appName: String, title: String, msg: String) {
        if(apiCall == null) return

        val date = Date(time)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)

        val callResponse: Call<BaseResponse> = apiCall!!.sendNewNotification(appName, title, msg, timeFormat.format(date))

        callResponse.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                val body = response.body()
                if(body != null) {
                    if(body.code == 200) {
                        Log.d("NotificationListener", "OK")
                    }
                    else {
                        Log.i("NotificationListener", "${body.code} ${body.message}")
                    }
                }
                else {
                    Log.i("NotificationListener", "Body NULL")
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.e("NotificationListener", "Error!", t)
            }
        })
    }
}