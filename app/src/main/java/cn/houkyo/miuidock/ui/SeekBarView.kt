package cn.houkyo.miuidock.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.lang.Exception

class SeekBarView(context: Context) : LinearLayout(context) {
    var text: String = ""  // #000000
        set(value) {
            field = value
            textView.text = value
        }
    var value: Int = 0
        set(value) {
            field = value
            seekBar.progress = value
            valueView.text = value.toString()
        }
    private val textView: TextView
    private val valueView: TextView
    private val seekBar: SeekBar
    private var minValue = 0
    private var maxValue = 100

    init {
        orientation = VERTICAL
        setPadding(20, 20, 20, 20)
        textView = TextView(context)
        valueView = TextView(context).apply {
            text = (maxValue / 2).toString()
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        textView.textSize = 15f
        seekBar = SeekBar(context).apply {
            min = minValue
            max = maxValue
            progress = valueView.text.toString().toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    value = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        addView(textView.apply {
            setPadding(0, 0, 0, 20)
        })

        addView(seekBar)
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            addView(TextView(context).apply {
                text = minValue.toString()
            }, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
            addView(valueView, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
            addView(TextView(context).apply {
                text = maxValue.toString()
                textAlignment = TextView.TEXT_ALIGNMENT_VIEW_END
            }, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        })
    }


    class Builder(
        private val mContext: Context,
        private val mText: String,
        private val defaultValue: Int = 0
    ) {
        fun build() = SeekBarView(mContext).apply {
            text = mText
            value = defaultValue
        }
    }
}