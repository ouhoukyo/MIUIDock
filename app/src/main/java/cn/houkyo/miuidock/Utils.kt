package cn.houkyo.miuidock

import android.annotation.SuppressLint
import android.content.Context;

class Utils {
    val DATAFILENAME = "MIUIDockConfig"

    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    @SuppressLint("SetWorldReadable")
    fun saveData(context: Context, key: String, value: Int) {
        try {
            val sharedPreferences = context.getSharedPreferences(DATAFILENAME, Context.MODE_WORLD_READABLE)
            val editor = sharedPreferences.edit()
            editor.putInt(key, value)
            editor.apply()
        } catch (e: Throwable) {
            // 也许是模块尚未加载
        }
    }

    fun getData(context: Context, key: String, defValue: Int): Int {
        try {
            val sharedPreferences = context.getSharedPreferences(DATAFILENAME, Context.MODE_WORLD_READABLE)
            val result = sharedPreferences.getInt(key, defValue)
            return result
        } catch (e: Throwable) {
            // 也许是模块尚未加载
        }
        return defValue
    }
}