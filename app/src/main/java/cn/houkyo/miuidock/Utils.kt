package cn.houkyo.miuidock

import android.content.Context;

class Utils {

    fun dip2px(context:Context,dpValue:Int): Int {
        var scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context:Context,pxValue:Int): Int {
        var scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}