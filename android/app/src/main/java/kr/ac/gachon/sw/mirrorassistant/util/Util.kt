package kr.ac.gachon.sw.mirrorassistant.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import kr.ac.gachon.sw.mirrorassistant.service.NotificationListenerService
import java.util.regex.Pattern


class Util {
    companion object {
        fun checkUrl(URL: String): Boolean {
            val urlPatternStr = "^(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/?([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?\$"
            val urlPattern = Pattern.compile(urlPatternStr);

            return urlPattern.matcher(URL).matches()
        }

        fun checkNotificationPermission(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .any { enabledPackageName ->
                    enabledPackageName == context.packageName
                }
        }

        fun toggleNotificationListenerService(context: Context) {
            android.service.notification.NotificationListenerService
                .requestRebind(ComponentName(context, NotificationListenerService::class.java))
        }

        /**
         * 패키지명으로 앱 이름 얻기
         * @author Minjae Seon
         * @param context Application Context
         * @param packageName 패키지명
         */
        fun getApplicationNameFromPackageName(context: Context, packageName: String): String? {
            try {
                var appInfo: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
                return context.packageManager.getApplicationLabel(appInfo).toString()
            }
            catch (e: PackageManager.NameNotFoundException) {
                return null
            }
        }
    }
}