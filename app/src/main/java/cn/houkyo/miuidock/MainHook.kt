package cn.houkyo.miuidock

import android.content.Context
import android.content.res.XResources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        const val SELF_PACKAGENAME = BuildConfig.APPLICATION_ID
        const val MIUI_HOME_LAUNCHER_PACKAGENAME = "com.miui.home"
        const val DEVICE_CONFIG_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.DeviceConfig"
        const val LAUNCHER_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.Launcher"
        const val BLUR_UTILS_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.common.BlurUtils"
        const val DEVICELEVEL_UTILS_CLASSNAME =
                "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.common.DeviceLevelUtils"
        const val CPULEVEL_UTILS_CLASSNAME =
                "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.common.CpuLevelUtils"
        const val UTILITIES_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.common.Utilities"

        // 单位dip
        val DOCK_RADIUS = DefaultValue().radius
        val DOCK_HEIGHT = DefaultValue().height
        val DOCK_SIDE = DefaultValue().sideMargin
        val DOCK_BOTTOM = DefaultValue().bottomMargin
        val DOCK_ICON_BOTTOM = DefaultValue().iconBottomMargin
        val HIGH_LEVEL = DefaultValue().highLevel

        // 需要修改圆角的资源
        val drawableNameList = arrayOf(
                "bg_search_bar_white85_black5",
                "bg_search_bar_black20_white10",
                "bg_search_bar_black8_white11",
                "bg_search_bar_d9_15_non",
                "bg_search_bar_e3_25_non",
                "bg_search_bar_button_dark",
                "bg_search_bar_button_light",
                "bg_search_bar_dark",
                "bg_search_bar_light"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            SELF_PACKAGENAME -> {
                XposedHelpers.findAndHookMethod("${SELF_PACKAGENAME}.MainActivity",
                        lpparam.classLoader, "isModuleEnable", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                })
            }
            MIUI_HOME_LAUNCHER_PACKAGENAME -> {
                launcherHook(lpparam)
                deviceConfigHook(lpparam)
                if (getData("HIGH_LEVEL", HIGH_LEVEL) == 1) {
                    deviceLevelHook(lpparam)
                }
            }

            else -> {
                return
            }
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName != MIUI_HOME_LAUNCHER_PACKAGENAME) {
            return
        }

        resparam.res.hookLayout(
                MIUI_HOME_LAUNCHER_PACKAGENAME,
                "layout",
                "layout_search_bar",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        // 替换资源圆角
                        val targetView = liparam.view
                        drawableNameList.forEach { drawableName ->
                            resetDockRadius(
                                    resparam.res,
                                    targetView.context,
                                    drawableName
                            )
                        }
                    }
                })
    }

    private fun launcherHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _LAUNCHER_CLASS = XposedHelpers.findClassIfExists(
                LAUNCHER_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        try {
            XposedHelpers.findAndHookMethod(
                    _LAUNCHER_CLASS,
                    "onCreate",
                    Bundle::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            super.afterHookedMethod(param)
                            val SearchBarObject = XposedHelpers.callMethod(
                                    param.thisObject,
                                    "getSearchBar"
                            ) as FrameLayout
                            val SearchBarDesktop = SearchBarObject.getChildAt(0) as RelativeLayout
                            val SearchBarDrawer = SearchBarObject.getChildAt(1) as RelativeLayout
                            val SearchBarContainer = SearchBarObject.parent as FrameLayout
                            val SearchEdgeLayout = SearchBarContainer.parent as FrameLayout
                            // 重新给Searbar容器排序
                            SearchEdgeLayout.removeView(SearchBarContainer)
                            SearchEdgeLayout.addView(SearchBarContainer, 0)
                            // 清空搜索图标和小爱同学
                            SearchBarDesktop.removeAllViews()
                            // 修改高度
                            SearchBarObject.layoutParams.height = Utils().dip2px(
                                    SearchBarDesktop.context,
                                    getData("DOCK_HEIGHT", DOCK_HEIGHT)
                            )

                            // 修改应用列表搜索框
                            val mAllAppViewField = _LAUNCHER_CLASS.getDeclaredField("mAppsView")
                            mAllAppViewField.isAccessible = true
                            val mAllAppView = mAllAppViewField.get(param.thisObject) as RelativeLayout
                            val mAllAppSearchView =
                                    mAllAppView.getChildAt(mAllAppView.childCount - 1) as FrameLayout
                            SearchBarObject.removeView(SearchBarDrawer)
                            mAllAppSearchView.addView(SearchBarDrawer)
                            SearchBarDrawer.bringToFront()
                            val layoutParams = SearchBarDrawer.layoutParams as FrameLayout.LayoutParams
                            SearchBarDrawer.layoutParams.height = Utils().dip2px(
                                    SearchBarDesktop.context,
                                    45
                            )
                            layoutParams.leftMargin = Utils().dip2px(SearchBarDesktop.context, 15)
                            layoutParams.rightMargin = Utils().dip2px(SearchBarDesktop.context, 15)
                            SearchBarDrawer.layoutParams = layoutParams
                        }
                    })
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] LauncherHook Error:" + e.message)
        }
    }

    private fun deviceConfigHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _DEVICE_CONFIG_CLASS = XposedHelpers.findClassIfExists(
                DEVICE_CONFIG_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        try {
            // 图标区域顶部边距
            XposedHelpers.findAndHookMethod(
                    _DEVICE_CONFIG_CLASS,
                    "calcHotSeatsMarginTop",
                    Context::class.java,
                    Boolean::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            param.args[1] = false
                            super.beforeHookedMethod(param)
                        }
                    })
            // 图标区域底部边距
            XposedHelpers.findAndHookMethod(
                    _DEVICE_CONFIG_CLASS,
                    "calcHotSeatsMarginBottom",
                    Context::class.java,
                    Boolean::class.java,
                    Boolean::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            super.beforeHookedMethod(param)
                            val _context = param.args[0] as Context
                            param.result = Utils().dip2px(_context, getData("DOCK_ICON_BOTTOM", DOCK_ICON_BOTTOM))
                        }
                    })
            // 搜索框宽度
            XposedHelpers.findAndHookMethod(
                    _DEVICE_CONFIG_CLASS,
                    "calcSearchBarWidth",
                    Context::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            super.beforeHookedMethod(param)
                            val _context = param.args[0] as Context
                            val deviceWidth = Utils().px2dip(
                                    _context,
                                    _context.resources.displayMetrics.widthPixels
                            )
                            param.result =
                                    Utils().dip2px(
                                            _context,
                                            deviceWidth - getData("DOCK_SIDE", DOCK_SIDE)
                                    )
                        }
                    })
            // 底部边距
            XposedHelpers.findAndHookMethod(
                    _DEVICE_CONFIG_CLASS,
                    "calcSearchBarMarginBottom",
                    Context::class.java,
                    Boolean::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            super.beforeHookedMethod(param)
                            val _context = param.args[0] as Context
                            param.result =
                                    Utils().dip2px(_context, getData("DOCK_BOTTOM", DOCK_BOTTOM))
                        }
                    })
            // 宽度变化量
            XposedHelpers.findAndHookMethod(
                _DEVICE_CONFIG_CLASS,
                "getSearchBarWidthDelta",
                XC_MethodReplacement.returnConstant(0)
            )
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] DeviceConfigHook Error:" + e.message)
        }
    }

    private fun deviceLevelHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        val _BLUR_UTILS_CLASS = XposedHelpers.findClassIfExists(
                BLUR_UTILS_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        val _DEVICELEVEL_UTILS_CLASS = XposedHelpers.findClassIfExists(
                DEVICELEVEL_UTILS_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        val _CPULEVEL_UTILS_CLASS = XposedHelpers.findClassIfExists(
                CPULEVEL_UTILS_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        val _UTILITIES_CLASS = XposedHelpers.findClassIfExists(
                UTILITIES_CLASSNAME,
                lpparam.classLoader
        ) ?: return
        // 高斯模糊类型
        replaceMethodResult(_BLUR_UTILS_CLASS, "getBlurType", 2)
        // 打开文件夹是否开启模糊
        replaceMethodResult(_BLUR_UTILS_CLASS, "isUserBlurWhenOpenFolder", true)
        // 设备等级
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "getDeviceLevel", 2)
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "getDeviceLevel", 2, Int::class.java)
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "getDeviceLevel", 2, Int::class.java, Int::class.java)
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "getDeviceLevel1", 2, Int::class.java)
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "isUseSimpleAnim", false)
        replaceMethodResult(_DEVICELEVEL_UTILS_CLASS, "getQualcommCpuLevel", 2, String::class.java)
        replaceMethodResult(_CPULEVEL_UTILS_CLASS, "getQualcommCpuLevel", 2, String::class.java)
        // 下载特效
        replaceMethodResult(_CPULEVEL_UTILS_CLASS, "needMamlDownload", true)
        // 平滑动画
        replaceMethodResult(_UTILITIES_CLASS, "isUseSmoothAnimationEffect", true)
    }

    private fun resetDockRadius(res: XResources, context: Context, drawableName: String) {
        try {
            res.setReplacement(
                    MIUI_HOME_LAUNCHER_PACKAGENAME,
                    "drawable",
                    drawableName,
                    object : XResources.DrawableLoader() {
                        override fun newDrawable(xres: XResources, id: Int): Drawable {
                            val background = ContextCompat.getDrawable(
                                    context, xres.getIdentifier(
                                    drawableName,
                                    "drawable",
                                    MIUI_HOME_LAUNCHER_PACKAGENAME
                            )
                            ) as RippleDrawable
                            val backgroundShape = background.getDrawable(0) as GradientDrawable
                            backgroundShape.cornerRadius =
                                    Utils().dip2px(context, getData("DOCK_RADIUS", DOCK_RADIUS))
                                            .toFloat()
                            background.setDrawable(0, backgroundShape)
                            return background
                        }
                    })
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] ResourcesReplacement Error:" + e.message)
        }
    }

    private fun replaceMethodResult(clazz: Class<*>, methodName: String, result: Any, vararg args: Any?) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, *args, XC_MethodReplacement.returnConstant(result))
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] Replace Method Result Error:" + e.message)
        }
    }

    private fun getData(key: String, defValue: Int): Int {
        try {
            val pref = XSharedPreferences(SELF_PACKAGENAME, Utils().DATAFILENAME)
            val value = pref.getInt(key, defValue)
            return value
        } catch (e: Throwable) {
            XposedBridge.log("[MIUIDock] Can not get data:" + key)
        }
        return defValue
    }
}