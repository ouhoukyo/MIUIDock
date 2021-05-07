package cn.houkyo.miuidock

import android.content.Context
import android.content.DialogInterface
import android.content.res.XResources
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.houkyo.miuidock.ui.SeekBarView
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.system.exitProcess

class MainHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        const val MIUI_HOME_LAUNCHER_PACKAGENAME = "com.miui.home"
        const val DEVICE_CONFIG_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.DeviceConfig"
        const val LAUNCHER_CLASSNAME = "$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.Launcher"

        // 单位dip
        val DOCK_RADIUS = DefaultValue().radius
        val DOCK_HEIGHT = DefaultValue().height
        val DOCK_SIDE = DefaultValue().sideMargin
        val DOCK_BOTTOM = DefaultValue().bottomMargin
        val DOCK_ICON_BOTTOM = DefaultValue().iconBottomMargin

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
        if (lpparam.packageName != MIUI_HOME_LAUNCHER_PACKAGENAME) {
            return
        }
        EzXHelperInit.initHandleLoadPackage(lpparam)
        EzXHelperInit.setLogTag("MIUIDock")
        findMethodByCondition("$MIUI_HOME_LAUNCHER_PACKAGENAME.launcher.Application") { m ->
            m.name == "attachBaseContext" && m.parameterTypes[0] == Context::class.java
        }.hookAfter {
            EzXHelperInit.initAppContext(it.args[0] as Context)
            showSettingDialog()
            launcherHook(lpparam)
            deviceConfigHook(lpparam)
        }
    }

    private fun showSettingDialog() {
        findMethodByCondition("$MIUI_HOME_LAUNCHER_PACKAGENAME.settings.MiuiHomeSettings") { m ->
            m.name == "onCreatePreferences" && m.parameterTypes[0] == Bundle::class.java && m.parameterTypes[1] == String::class.java
        }.hookAfter {
            it.thisObject.getObjectOrNull("mSearchBarSetting")!!.invokeMethod(
                "setTitle",
                arrayOf("Dock设置"),
                arrayOf(CharSequence::class.java)
            )
        }
        findMethodByCondition("$MIUI_HOME_LAUNCHER_PACKAGENAME.settings.MiuiHomeSettings") { m ->
            m.name == "showHomeSearchBarDialog" && m.parameters.isEmpty()
        }.hookBefore {
            Log.d("hook start")
            val context = it.thisObject.invokeMethod("getContext") as Context
            val dialogBuilder = loadClass("miui.app.AlertDialog${'$'}Builder")
                .getConstructor(Context::class.java)
                .newInstance(context)
            dialogBuilder.apply {
                val radiusSeek =
                    SeekBarView.Builder(context, "Dock圆角", Utils.getData("DOCK_RADIUS", 20)).build()
                val heightSeek =
                    SeekBarView.Builder(context, "Dock高度", Utils.getData("DOCK_HEIGHT", 84)).build()
                val sideSeek =
                    SeekBarView.Builder(context, "Dock距离屏幕两侧", Utils.getData("DOCK_SIDE", 30))
                        .build()
                val bottomSeek =
                    SeekBarView.Builder(context, "Dock距离屏幕底部", Utils.getData("DOCK_BOTTOM", 23))
                        .build()
                val iconBottomSeek =
                    SeekBarView.Builder(
                        context,
                        "图标区域距离屏幕底部",
                        Utils.getData("DOCK_ICON_BOTTOM", 35)
                    ).build()
                var confirmDelete = false
                invokeMethod("setTitle", arrayOf("Dock设置"), arrayOf(CharSequence::class.java))
                invokeMethod(
                    "setNegativeButton",
                    arrayOf("删除", DialogInterface.OnClickListener { _, _ ->
                        it.thisObject.invokeMethod(
                            "setHomeScreenSearchBar",
                            arrayOf(context, false),
                            arrayOf(Context::class.java, Boolean::class.java)
                        )
                        confirmDelete = true
                    }),
                    arrayOf(CharSequence::class.java, DialogInterface.OnClickListener::class.java)
                )
                invokeMethod(
                    "setPositiveButton",
                    arrayOf("保存并重启", DialogInterface.OnClickListener { _, _ ->
                        Utils.getEditor()?.let { editor ->
                            editor.putInt("DOCK_RADIUS", radiusSeek.value)
                            editor.putInt("DOCK_HEIGHT", heightSeek.value)
                            editor.putInt("DOCK_SIDE", sideSeek.value)
                            editor.putInt("DOCK_BOTTOM", bottomSeek.value)
                            editor.putInt("DOCK_ICON_BOTTOM", iconBottomSeek.value)
                            if (editor.commit()) {
                                exitProcess(0)
                            }
                        }
                    }),
                    arrayOf(CharSequence::class.java, DialogInterface.OnClickListener::class.java)
                )
                invokeMethod(
                    "setOnDismissListener",
                    arrayOf(DialogInterface.OnDismissListener { _ ->
                        if (!confirmDelete) it.thisObject.getObjectOrNull("mSearchBarSetting")!!
                            .invokeMethod("setChecked", arrayOf(true), arrayOf(Boolean::class.java))
                    }),
                    arrayOf(DialogInterface.OnDismissListener::class.java)
                )
                val linearLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(radiusSeek)
                    addView(heightSeek)
                    addView(sideSeek)
                    addView(bottomSeek)
                    addView(iconBottomSeek)
                }
                invokeMethod("setView", arrayOf(linearLayout), arrayOf(View::class.java))
            }
            it.thisObject.putObject("mHomeSearchBarDialog", dialogBuilder.invokeMethod("create"))
            return@hookBefore
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
                        SearchBarObject.layoutParams.height = Utils.dip2px(
                            SearchBarDesktop.context,
                            Utils.getData("DOCK_HEIGHT", DOCK_HEIGHT)
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
                        SearchBarDrawer.layoutParams.height = Utils.dip2px(
                            SearchBarDesktop.context,
                            45
                        )
                        layoutParams.leftMargin = Utils.dip2px(SearchBarDesktop.context, 15)
                        layoutParams.rightMargin = Utils.dip2px(SearchBarDesktop.context, 15)
                        SearchBarDrawer.layoutParams = layoutParams
                    }
                })
        } catch (e: Exception) {
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
                        param.result =
                            Utils.dip2px(
                                _context,
                                Utils.getData("DOCK_ICON_BOTTOM", DOCK_ICON_BOTTOM)
                            )
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
                        val deviceWidth = Utils.px2dip(
                            _context,
                            _context.resources.displayMetrics.widthPixels
                        )
                        param.result =
                            Utils.dip2px(
                                _context,
                                deviceWidth - Utils.getData("DOCK_SIDE", DOCK_SIDE)
                            )
                    }
                })
            // Dock底部边距
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
                            Utils.dip2px(_context, Utils.getData("DOCK_BOTTOM", DOCK_BOTTOM))
                    }
                })
        } catch (e: Exception) {
            XposedBridge.log("[MIUIDock] DeviceConfigHook Error:" + e.message)
        }
    }

    private fun resetDockRadius(res: XResources, context: Context, drawableName: String) {
        try {
            res.setReplacement(
                MIUI_HOME_LAUNCHER_PACKAGENAME,
                "drawable",
                drawableName,
                object : XResources.DrawableLoader() {
                    override fun newDrawable(xres: XResources, id: Int): Drawable {
                        val background = context.getDrawable(
                            xres.getIdentifier(
                                drawableName,
                                "drawable",
                                MIUI_HOME_LAUNCHER_PACKAGENAME
                            )
                        ) as RippleDrawable
//                        val background = ContextCompat.getDrawable(
//                            context, xres.getIdentifier(
//                                drawableName,
//                                "drawable",
//                                MIUI_HOME_LAUNCHER_PACKAGENAME
//                            )
//                        ) as RippleDrawable
                        val backgroundShape = background.getDrawable(0) as GradientDrawable
                        backgroundShape.cornerRadius =
                            Utils.dip2px(context, Utils.getData("DOCK_RADIUS", DOCK_RADIUS))
                                .toFloat()
                        background.setDrawable(0, backgroundShape)
                        return background
                    }
                })
        } catch (e: Exception) {
            XposedBridge.log("[MIUIDock] ResourcesReplacement Error:" + e.message)
        }
    }

}