package cn.houkyo.miuidock

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


@SuppressLint("UseSwitchCompatOrMaterialCode")
class MainActivity : AppCompatActivity() {
    var radius = DefaultValue().radius
    var height = DefaultValue().height
    var sideMargin = DefaultValue().sideMargin
    var bottomMargin = DefaultValue().bottomMargin
    var highLevel = DefaultValue().highLevel
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
        val RadiusSeekBar = findViewById<SeekBar>(R.id.dockRadiusSeekBar)
        val RadiusMinTextView = findViewById<TextView>(R.id.dockRadiusMinTextView)
        val RadiusMaxTextView = findViewById<TextView>(R.id.dockRadiusMaxTextView)
        val RadiusValueTextView = findViewById<TextView>(R.id.dockRadiusValueTextView)

        val HeightSeekBar = findViewById<SeekBar>(R.id.dockHeightSeekBar)
        val HeightMinTextView = findViewById<TextView>(R.id.dockHeightMinTextView)
        val HeightMaxTextView = findViewById<TextView>(R.id.dockHeightMaxTextView)
        val HeightValueTextView = findViewById<TextView>(R.id.dockHeightValueTextView)

        val SideSeekBar = findViewById<SeekBar>(R.id.dockSideSeekBar)
        val SideMinTextView = findViewById<TextView>(R.id.dockSideMinTextView)
        val SideMaxTextView = findViewById<TextView>(R.id.dockSideMaxTextView)
        val SideValueTextView = findViewById<TextView>(R.id.dockSideValueTextView)

        val BottomSeekBar = findViewById<SeekBar>(R.id.dockBottomSeekBar)
        val BottomMinTextView = findViewById<TextView>(R.id.dockBottomMinTextView)
        val BottomMaxTextView = findViewById<TextView>(R.id.dockBottomMaxTextView)
        val BottomValueTextView = findViewById<TextView>(R.id.dockBottomValueTextView)

        val HighLevelSwitch = findViewById<Switch>(R.id.highLevelSwitch)

        val SaveButton = findViewById<Button>(R.id.saveButton)

        RadiusSeekBar.min = 0
        RadiusMinTextView.text = RadiusSeekBar.min.toString()
        RadiusSeekBar.max = height
        RadiusMaxTextView.text = RadiusSeekBar.max.toString()
        RadiusSeekBar.progress = radius
        RadiusValueTextView.text = radius.toString()
        RadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                RadiusValueTextView.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        HeightSeekBar.min = 30
        HeightMinTextView.text = HeightSeekBar.min.toString()
        HeightSeekBar.max = 120
        HeightMaxTextView.text = HeightSeekBar.max.toString()
        HeightSeekBar.progress = height
        HeightValueTextView.text = height.toString()
        HeightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                HeightValueTextView.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                RadiusSeekBar.max = HeightSeekBar.progress
                RadiusMaxTextView.text = RadiusSeekBar.max.toString()
            }
        })

        val deviceWidth = Utils().px2dip(this, resources.displayMetrics.widthPixels)
        SideSeekBar.min = 0
        SideMinTextView.text = SideSeekBar.min.toString()
        SideSeekBar.max = (deviceWidth / 2) + 10
        SideMaxTextView.text = SideSeekBar.max.toString()
        SideSeekBar.progress = sideMargin
        SideValueTextView.text = sideMargin.toString()
        SideSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                SideValueTextView.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        BottomSeekBar.min = 0
        BottomMinTextView.text = BottomSeekBar.min.toString()
        BottomSeekBar.max = 200
        BottomMaxTextView.text = BottomSeekBar.max.toString()
        BottomSeekBar.progress = bottomMargin
        BottomValueTextView.text = bottomMargin.toString()
        BottomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                BottomValueTextView.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        HighLevelSwitch.isChecked = highLevel == 1

        SaveButton.setOnClickListener {
            radius = RadiusSeekBar.progress
            height = HeightSeekBar.progress
            sideMargin = SideSeekBar.progress
            bottomMargin = BottomSeekBar.progress
            highLevel = if (HighLevelSwitch.isChecked) 1 else 0
            Utils().saveData(this, "DOCK_RADIUS", radius)
            Utils().saveData(this, "DOCK_RADIUS", radius)
            Utils().saveData(this, "DOCK_HEIGHT", height)
            Utils().saveData(this, "DOCK_SIDE", sideMargin)
            Utils().saveData(this, "DOCK_BOTTOM", bottomMargin)
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

    fun goToSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", "com.miui.home", null)
        intent.data = uri
        startActivity(intent)
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