package cn.houkyo.miuidock.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import cn.houkyo.miuidock.R
import java.lang.Exception

class CustomSeekBar : LinearLayout {
    private var mainSeekBar: SeekBar? = null
    private var minTextView: TextView? = null
    private var valueTextView: TextView? = null
    private var maxTextView: TextView? = null
    private lateinit var onValueChangeListener: (value: Int) -> Unit

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        init(context)
    }

    fun init(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_seek_bar, this)
        mainSeekBar = view.findViewById(R.id.mainSeekBar)
        minTextView = view.findViewById(R.id.minTextView)
        valueTextView = view.findViewById(R.id.valueTextView)
        maxTextView = view.findViewById(R.id.maxTextView)
        onValueChangeListener = { value: Int ->
            value
        }
    }

    fun setValue(i: Int) {
        mainSeekBar?.progress = i
        valueTextView?.text = i.toString()

        mainSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                valueTextView?.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 != null) {
                    onValueChangeListener.invoke(p0.progress)
                }
            }
        })
    }

    fun setMinValue(i: Int) {
        mainSeekBar?.min = i
        minTextView?.text = i.toString()
    }

    fun setMaxValue(i: Int) {
        mainSeekBar?.max = i
        maxTextView?.text = i.toString()
    }

    fun getValue(): Int {
        if (mainSeekBar != null) {
            return mainSeekBar!!.progress
        }
        return 0
    }

    fun setOnValueChangeListener(callback: (value: Int) -> Unit) {
        this.onValueChangeListener = callback
    }
}