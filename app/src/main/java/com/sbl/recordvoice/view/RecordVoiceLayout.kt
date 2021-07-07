package com.sbl.recordvoice.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.sbl.recordvoice.R
import com.sbl.recordvoice.view.center.RecordVoiceControlView
import com.sbl.recordvoice.view.center.RecordVoiceRecordStatusListener
import com.sbl.recordvoice.view.center.RecordVoiceTextStatusListener
import com.sbl.recordvoice.view.line.LinePlayView


/**
 * sunbolin 2021/7/7
 */
class RecordVoiceLayout : LinearLayout, RecordVoiceTextStatusListener {


    private var controlView: RecordVoiceControlView
    private var statusText: TextView
    private var statusLayout: LinearLayout
    private var loading1: LinePlayView
    private var loading2: LinePlayView

    private var showAnimator: ObjectAnimator? = null
    private var hideAnimator: ObjectAnimator? = null
    private var firstTextStr: String? = null


    constructor(context: Context) : super(context)


    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)


    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )


    init {
        LayoutInflater.from(context).inflate(R.layout.record_voice_layout, this)
        controlView = findViewById(R.id.controlView)
        statusText = findViewById(R.id.statusText)
        statusLayout = findViewById(R.id.statusLayout)
        loading1 = findViewById(R.id.loading1)
        loading2 = findViewById(R.id.loading2)

        controlView.textStatusListener = this
    }


    fun setRecordStatusListener(listener: RecordVoiceRecordStatusListener) {
        controlView.recordStatusListener = listener
    }


    override fun setStatusTextColor(color: Int) {
        statusText.setTextColor(color)
        loading1.color = color
        loading2.color = color
    }


    override fun setStatusTextTime(time: Int) {
        statusText.text = firstTextStr + " " + String.format("%02d", time) + "\""
    }


    override fun setStatusTextFirst(text: String?) {
        firstTextStr = text
    }


    override fun setStatusTextShort(text: String?) {
        statusText.text = text
    }


    override fun showStatus() {
        statusLayout.visibility = VISIBLE
        loading1.startAnim()
        loading2.startAnim()

        hideAnimator?.cancel()
        hideAnimator = null
        showAnimator = ObjectAnimator.ofFloat(statusLayout, "alpha", 0f, 1f).apply {
            duration = 1000
            start()
        }
    }


    override fun hideStatus() {
        showAnimator?.cancel()
        showAnimator = null
        hideAnimator = ObjectAnimator.ofFloat(statusLayout, "alpha", 1f, 0f).apply {
            duration = 1000
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    hindStatusLayout()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    hindStatusLayout()
                }

                override fun onAnimationRepeat(animation: Animator?) {
                    hindStatusLayout()
                }
            })
            start()
        }
    }

    private fun hindStatusLayout() {
        statusLayout.visibility = INVISIBLE
        loading1.stopAnim()
        loading2.stopAnim()
    }
}