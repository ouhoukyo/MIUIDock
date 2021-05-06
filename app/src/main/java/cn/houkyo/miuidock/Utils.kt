package cn.houkyo.miuidock

import android.content.Context
import android.content.SharedPreferences
import com.github.kyuubiran.ezxhelper.init.InitFields.appContext
import com.github.kyuubiran.ezxhelper.utils.Log
import java.lang.Exception

object Utils {
    val DATAFILENAME = "MIUIDockConfig"

    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getEditor(): SharedPreferences.Editor? {
        return try {
            appContext.getSharedPreferences(DATAFILENAME, Context.MODE_PRIVATE).edit()
        } catch (e: Exception) {
            null
        }
    }

    fun saveData(key: String, value: Any) {
        try {
            val sharedPreferences =
                appContext.getSharedPreferences(DATAFILENAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            when (value) {
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
            }
            editor.apply()
        } catch (e: Exception) {
            // 也许是模块尚未加载
            Log.e(e, "saveData")
        }
    }

    fun getData(key: String, defValue: Int): Int {
        try {
            val sharedPreferences =
                appContext.getSharedPreferences(DATAFILENAME, Context.MODE_PRIVATE)
            return sharedPreferences.getInt(key, defValue)
        } catch (e: Throwable) {
            // 也许是模块尚未加载
            return defValue
        }
    }
}