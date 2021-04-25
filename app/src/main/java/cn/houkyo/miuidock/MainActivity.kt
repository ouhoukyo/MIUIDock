package cn.houkyo.miuidock

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cn.houkyo.miuidock.ui.CustomSeekBar
import java.io.DataOutputStream
import java.lang.Exception


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    var radius = DefaultValue().radius
    var height = DefaultValue().height
    var sideMargin = DefaultValue().sideMargin
    var bottomMargin = DefaultValue().bottomMargin
    var iconBottomMargin = DefaultValue().iconBottomMargin
    var highLevel = DefaultValue().highLevel
    var hideIcon = DefaultValue().hideIcon
    var isModuleEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isModuleEnable = Utils().getData(this, "TEST_MODULE", 1) == 1
        if (!isModuleEnable) {
            val toast = Toast(this)
            toast.setText(R.string.module_not_enable)
            toast.show()
        }
        radius = Utils().getData(this, "DOCK_RADIUS", radius)
        height = Utils().getData(this, "DOCK_HEIGHT", height)
        sideMargin = Utils().getData(this, "DOCK_SIDE", sideMargin)
        bottomMargin = Utils().getData(this, "DOCK_BOTTOM", bottomMargin)
        highLevel = Utils().getData(this, "HIGH_LEVEL", highLevel)
        hideIcon = Utils().getData(this, "HIDE_ICON", hideIcon)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val GoToSetting = R.id.menu_to_setting
        val About = R.id.menu_about
        when (item.getItemId()) {
            GoToSetting -> goToSetting()
            About -> showAbout()
        }
        return true;
    }

    fun init() {
        val RadiusSeekBar = findViewById<CustomSeekBar>(R.id.dockRadiusSeekBar)
        val HeightSeekBar = findViewById<CustomSeekBar>(R.id.dockHeightSeekBar)
        val SideSeekBar = findViewById<CustomSeekBar>(R.id.dockSideSeekBar)
        val BottomSeekBar = findViewById<CustomSeekBar>(R.id.dockBottomSeekBar)
        val IconBottomSeekBar = findViewById<CustomSeekBar>(R.id.dockIconBottomSeekBar)
        val HighLevelSwitch = findViewById<Switch>(R.id.highLevelSwitch)
        val SaveButton = findViewById<Button>(R.id.saveButton)
        val hideIconSwitch = findViewById<Switch>(R.id.hide_icon_switch)

        val hideIconTextView = findViewById<TextView>(R.id.hide_icon)
        //隐藏/显示图标的开关监听器
        //切换状态实时生效不需要点击保存按钮
        hideIconSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                hideIcon(hideIconTextView, p1)
            }
        })

        RadiusSeekBar.setMinValue(0)
        RadiusSeekBar.setMaxValue(height)
        RadiusSeekBar.setValue(radius)

        HeightSeekBar.setMinValue(30)
        HeightSeekBar.setMaxValue(120)
        HeightSeekBar.setValue(height)
        HeightSeekBar.setOnValueChangeListener { value -> RadiusSeekBar.setMaxValue(value) }

        val deviceWidth = Utils().px2dip(this, resources.displayMetrics.widthPixels)
        SideSeekBar.setMinValue(0)
        SideSeekBar.setMaxValue((deviceWidth / 2) + 10)
        SideSeekBar.setValue(sideMargin)

        BottomSeekBar.setMinValue(0)
        BottomSeekBar.setMaxValue(200)
        BottomSeekBar.setValue(bottomMargin)

        IconBottomSeekBar.setMinValue(0)
        IconBottomSeekBar.setMaxValue(200)
        IconBottomSeekBar.setValue(iconBottomMargin)

        HighLevelSwitch.isChecked = highLevel == 1
        //初始化隐藏/显示图标的按钮以及文字
        hideIconSwitch.isChecked = hideIcon == 1
        hideIconTextView.setText(if (hideIconSwitch.isChecked) R.string.hide_icon else R.string.show_icon)


        SaveButton.setOnClickListener {
            radius = RadiusSeekBar.getValue()
            height = HeightSeekBar.getValue()
            sideMargin = SideSeekBar.getValue()
            bottomMargin = BottomSeekBar.getValue()
            iconBottomMargin = IconBottomSeekBar.getValue()
            highLevel = if (HighLevelSwitch.isChecked) 1 else 0
            Utils().saveData(this, "DOCK_RADIUS", radius)
            Utils().saveData(this, "DOCK_RADIUS", radius)
            Utils().saveData(this, "DOCK_HEIGHT", height)
            Utils().saveData(this, "DOCK_SIDE", sideMargin)
            Utils().saveData(this, "DOCK_BOTTOM", bottomMargin)
            Utils().saveData(this, "DOCK_ICON_BOTTOM", iconBottomMargin)
            Utils().saveData(this, "HIGH_LEVEL", highLevel)
            val toast = Toast(this)
            if (isModuleEnable) {
                toast.setText(R.string.dock_save_tips)
            } else {
                toast.setText(R.string.module_not_enable)
            }
            toast.show()
        }
    }

    fun hideIcon(textView: TextView, hide: Boolean) {
        //默认显示图标
        var switch: Int = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        // 隐藏图标
        if (hide) {
            hideIcon = 1
            switch = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            textView.setText(R.string.hide_icon)
        } else {
            hideIcon = 0
            textView.setText(R.string.show_icon)
        }
        Utils().saveData(this, "HIDE_ICON", hideIcon)

        this.getPackageManager().setComponentEnabledSetting(
            ComponentName(this, this.javaClass.name + "Alias"),
            switch, PackageManager.DONT_KILL_APP
        )

    }

    fun goToSetting() {
        try {
            val exitValue: Int = Utils().killHomeProcess()
            if (exitValue == 0) {
                val toast = Toast(this)
                toast.setText(R.string.restart_launcher_tips)
                toast.show()
            } else {
                throw Exception()
            }
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", "com.miui.home", null)
            intent.data = uri
            startActivity(intent)
        }
    }

    fun showAbout() {
        val AlertDialogBuilder = AlertDialog.Builder(this)
        AlertDialogBuilder.setTitle(R.string.menu_about_title)
        AlertDialogBuilder.setMessage(R.string.dialog_about_message)
        AlertDialogBuilder.setCancelable(true)
        AlertDialogBuilder.setPositiveButton(
            "OK"
        ) { dialog, id -> dialog.cancel() }
        AlertDialogBuilder.setNegativeButton(
            "Github"
        ) { dialog, id ->
            val github = "https://www.github.com/ouhoukyo/MIUIDock"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(github)))
        }
        val dialog = AlertDialogBuilder.create()
        dialog.show()
    }
}