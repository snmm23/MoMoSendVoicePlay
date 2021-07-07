package com.sbl.recordvoice.view.line

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sbl.recordvoice.R


/**
 * sunbolin 2021/7/7
 */
class LinePlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * 画笔
     * */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 画笔的颜色
     * */
    var color = Color.BLACK
        set(value) {
            field = value
            paint.color = field
            invalidate()
        }

    /**
     * 画笔的宽度
     * */
    var lineWidth = 6
        set(value) {
            field = value
            paint.strokeWidth = field.toFloat()
            calculateLineSpace()
            invalidate()
        }

    /**
     * 线条的个数
     * */
    var count = 6
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 线条的原始高度
     * */
    var origHeightScale = arrayOf(0.28f, 0.71f, 1f, 0.57f, 0.42f, 0.21f)

    /**
     * 线条的动画的最大比例
     * */
    var animHeightScale = arrayOf(0.6f, 0.3f, 0.5f, 0.9f, 0.8f, 0.5f)

    /**
     * 线条与线条之间的间隔
     * */
    private var lineSpace = 0f

    private var lineMaxHeight = 0f

    /**
     * 线条的增长比例
     * */
    private var offsetScale = 0f

    private val valueAnimator by lazy {
        val animator = ValueAnimator.ofFloat(0f, 1f, 0f)
        animator.duration = 300
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            offsetScale = it.animatedFraction
            invalidate()
        }
        animator
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoicePlayView)
        count = typedArray.getInt(R.styleable.VoicePlayView_lineCount, 6)
        color = typedArray.getInt(R.styleable.VoicePlayView_lineColor, Color.BLACK)
        lineWidth = typedArray.getDimensionPixelOffset(R.styleable.VoicePlayView_lineWidth, 6)
        typedArray.recycle()

        paint.color = color
        paint.strokeWidth = lineWidth.toFloat()
        // 设置笔尖为圆形
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // 计算每个线条的位置
        calculateLineSpace()
    }

    private fun calculateLineSpace() {
        lineSpace = ((width - lineWidth * count) / (count - 1)).toFloat()
        // 减去两端的笔尖的宽度
        lineMaxHeight = height.toFloat() - lineWidth * 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // 计算五个线条的位置
        var tempX = (lineWidth / 2).toFloat()
        val centerY = height / 2
        for (i in 0..5) {
            // 线条的高度
            val lineHeight = lineMaxHeight.times(origHeightScale[i])
            // 计算高度的差值
            val offsetHeight =
                lineMaxHeight.times(offsetScale * (animHeightScale[i] - origHeightScale[i]))
            // 计算当前的高度
            val nowHeight = (lineHeight + offsetHeight) / 2
            canvas?.drawLine(tempX, centerY - nowHeight, tempX, centerY + nowHeight, paint)
            tempX += lineSpace
        }
    }

    /**
     * 动画开始
     * */
    fun startAnim() {
        valueAnimator.start()
    }

    /**
     * 动画结束
     * */
    fun stopAnim() {
        if (valueAnimator.isRunning) {
            valueAnimator.end()
            offsetScale = 0f
            invalidate()
        }
    }

    /**
     * 是否正在显示动画
     * */
    fun isPlaying(): Boolean = valueAnimator.isRunning

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isPlaying()) {
            stopAnim()
        }
    }
}
