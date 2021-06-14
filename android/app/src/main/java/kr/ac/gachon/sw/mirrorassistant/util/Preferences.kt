package kr.ac.gachon.sw.mirrorassistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var lastIP: String
        get() = prefs.getString("lastIP", "")!!
        set(value) = prefs.edit().putString("lastIP", value).apply()

    var enableNoti: Boolean
        get() = prefs.getBoolean("enableNoti", true)
        set(value) = prefs.edit().putBoolean("enableNoti", value).apply()
}