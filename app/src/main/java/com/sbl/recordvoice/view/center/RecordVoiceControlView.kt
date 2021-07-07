package com.sbl.recordvoice.view.center

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.content.ContextCompat
import com.sbl.recordvoice.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * sunbolin 2021/7/7
 */
class RecordVoiceControlView : View {


    companion object {
        private const val TAG = "CenterVoiceControlView"
    }

    //总计时时长(毫秒)
    private val totalRecordTime: Long = 60000

    //录音时每次刷新View间隔(毫秒)
    private val recordingRefreshIntervalTime: Long = 10

    //预完成发送阀值(毫秒)
    private val prepareSendRecordTime: Long = 50000

    //时间过短阀值(毫秒)
    private val tooShortTime: Long = 1000

    //绘制圆弧的画笔
    private var progressPaint: Paint? = null

    //绘制圆弧的矩形区域
    private var mProgressRectF: RectF? = null

    //可以更新的进度条数值
    private var progressNum = 0f

    //进度条最大值
    private var maxNum = 0f

    //进度条圆弧颜色
    private var progressColor = 0

    //背景圆弧的起始角度
    private var startAngle = 0f

    //背景圆弧扫过的角度
    private var sweepAngle = 0f

    //圆弧进度条宽度
    private var barWidth = 0f

    //进度条圆弧扫过的角度
    private var progressSweepAngle = 0f


    //圆弧进度显示动画
    private var progressAnim = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            progressSweepAngle = interpolatedTime * sweepAngle * progressNum / maxNum
            postInvalidate()
        }
    }

    //View宽高
    private var mWidth = 0
    private var mHeight = 0

    //圆半径
    private var mRadius = 0f

    //图标中心点位置
    private var centerToLeft = 0f
    private var centerToTop = 0f

    //圆的边界
    private var mCircleLeftBorder = 0f
    private var mCircleRightBorder = 0f
    private var mCircleTopBorder = 0f
    private var mCircleBottomBorder = 0f

    //背景颜色的画笔
    private var bgPaint: Paint? = null

    //图标画笔
    private var imagePaint: Paint? = null

    //时间过短颜色的画笔
    private var tooShortBGPaint: Paint? = null

    //是否为按住状态
    private var isPressHold = false

    //是否可以取消
    private var isCancelable = false

    //当前进度
    private var nowPosition: Long = 0

    //进度是否全部走完
    private var isFullFinish = false

    //主计时线程
    private var mainDisposable: Disposable? = null

    //时间过短渐变位置
    private var tooShortAlphaIndex = 0

    //时间过短动画是否结束
    private var isTooShortAnimRun = false

    //时间过短动画时长
    private var tooShortAnimTime: Long = 1500


    //时间过短动画
    private var tooShortAnim = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            val item = 255 / 100
            val itt = 100 - (interpolatedTime * 100).toInt()
            tooShortAlphaIndex = item * itt
            postInvalidate()
        }
    }.apply {
        duration = tooShortAnimTime
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                isTooShortAnimRun = true
            }

            override fun onAnimationEnd(animation: Animation?) {
                isTooShortAnimRun = false
            }

            override fun onAnimationRepeat(animation: Animation?) {
                isTooShortAnimRun = false
            }
        })
    }


    //录音状态监听
    var recordStatusListener: RecordVoiceRecordStatusListener? = null

    //提示文字状态监听
    var textStatusListener: RecordVoiceTextStatusListener? = null


    constructor(context: Context) : super(context)


    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)


    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )


    /**
     * 初始化
     */
    init {
        progressColor = ContextCompat.getColor(context, R.color.blue_48B5ED)
        startAngle = -90f
        sweepAngle = 360f
        barWidth = dip2px(context, 4f).toFloat()
        progressNum = 0f
        maxNum = totalRecordTime.toFloat()
        mProgressRectF = RectF()
        progressPaint = Paint().apply {
            style = Paint.Style.STROKE //只描边，不填充
            color = progressColor
            isAntiAlias = true //设置抗锯齿
            strokeWidth = barWidth
            strokeCap = Paint.Cap.ROUND //设置画笔为圆角
        }

        bgPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL //设置背景为填充
        }

        imagePaint = Paint()

        tooShortBGPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.yellow_FFE1B24A)
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measureSize(mWidth, heightMeasureSpec)
        mHeight = measureSize(mHeight, widthMeasureSpec)
        val min = mWidth.coerceAtMost(mHeight) // 获取View最短边的长度
        setMeasuredDimension(min, min) // 强制改View为以最短边为长度的正方形

        mRadius = ((mWidth / 2).toFloat())
        centerToLeft = ((mWidth / 2).toFloat())
        centerToTop = ((mHeight / 2).toFloat())

        mCircleLeftBorder = mWidth / 2 - mRadius
        mCircleRightBorder = mWidth / 2 + mRadius
        mCircleTopBorder = mHeight / 2 - mRadius
        mCircleBottomBorder = mHeight / 2 + mRadius

        if (min >= barWidth * 2) {
            mProgressRectF!![barWidth / 2, barWidth / 2, min - barWidth / 2] = min - barWidth / 2
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgPaint!!.color = ContextCompat.getColor(context, R.color.white)
        canvas.drawCircle((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), mRadius, bgPaint!!)

        if (isPressHold) {
            canvas.drawArc(mProgressRectF!!, startAngle, progressSweepAngle, false, progressPaint!!)

            if (isCancelable) {
                setProgressColor(
                    ContextCompat.getColor(
                        context,
                        R.color.pink_FF4081
                    )
                )
                textStatusListener?.setStatusTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.pink_FF4081
                    )
                )
                textStatusListener?.setStatusTextFirst(context.resources.getString(R.string.voice_release_cancel))

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_voice_delete)
                val bitmapCenterToLeft = centerToLeft - bitmap.width / 2
                val bitmapCenterToTop = centerToTop - bitmap.height / 2
                canvas.drawBitmap(bitmap, bitmapCenterToLeft, bitmapCenterToTop, imagePaint)

            } else {
                if (nowPosition < prepareSendRecordTime) {
                    setProgressColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue_48B5ED
                        )
                    )
                    textStatusListener?.setStatusTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue_48B5ED
                        )
                    )
                }
                textStatusListener?.setStatusTextFirst(context.resources.getString(R.string.voice_up_slide_cancel))
            }
        } else {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_chat_voice_speak)
            val bitmapCenterToLeft = centerToLeft - bitmap.width / 2
            val bitmapCenterToTop = centerToTop - bitmap.height / 2
            canvas.drawBitmap(bitmap, bitmapCenterToLeft, bitmapCenterToTop, imagePaint)
        }

        tooShortBGPaint!!.alpha = tooShortAlphaIndex
        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            mRadius,
            tooShortBGPaint!!
        )
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTooShortAnimRun) {
                    return true
                }
                if (isFullFinish && !isPressHold) {
                    return true
                }
                if (event.x < mCircleRightBorder && event.x > mCircleLeftBorder && event.y < mCircleBottomBorder && event.y > mCircleTopBorder) {
                    isPressHold = true
                    isFullFinish = false
                    isCancelable = false
                    Log.e(TAG, "已开始, 开始录音")
                    recordStatusListener?.startSoundRecording("已开始, 开始录音")
                    startRecordTime()
                    invalidate()
                    textStatusListener?.showStatus()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isFullFinish && isPressHold) {
                    return true
                }
                val x = event.x
                val y = event.y
                isCancelable = (y < mCircleTopBorder
                        || y > mCircleBottomBorder
                        || x > mCircleRightBorder
                        || x < mCircleLeftBorder)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                if (isFullFinish && isPressHold) {
                    return true
                }
                if (!isCancelable && nowPosition >= 0 && nowPosition <= tooShortTime) {
                    doFinishRecord(true)
                } else {
                    doFinishRecord(false)
                }
            }
        }
        return true
    }


    private fun startRecordTime() {
        mainDisposable = Observable.interval(recordingRefreshIntervalTime, TimeUnit.MILLISECONDS)
            .take(totalRecordTime / recordingRefreshIntervalTime)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                nowPosition += recordingRefreshIntervalTime

                textStatusListener?.setStatusTextTime((nowPosition / 1000).toInt())

                if (nowPosition >= prepareSendRecordTime) {
                    setProgressColor(
                        ContextCompat.getColor(
                            context,
                            R.color.pink_FF4081
                        )
                    )
                    textStatusListener?.setStatusTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.pink_FF4081
                        )
                    )
                }
                setProgressNum(nowPosition.toFloat())
                if (nowPosition >= totalRecordTime) {
                    isFullFinish = true
                    isCancelable = false
                    doFinishRecord(false)
                }
            }) { throwable: Throwable -> throwable.printStackTrace() }
    }


    private fun doFinishRecord(isTooShort: Boolean) {
        if (!isPressHold) {
            return
        }

        mainDisposable?.dispose()
        mainDisposable = null
        nowPosition = 0
        progressNum = 0f
        isFullFinish = false
        isPressHold = false

        Log.e(TAG, "结束, 停止录音")
        recordStatusListener?.stopSoundRecording("结束, 停止录音")

        if (isTooShort) {
            Log.e(TAG, "时间过短, 取消发送")
            textStatusListener?.setStatusTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.yellow_FFE1B24A
                )
            )
            textStatusListener?.setStatusTextShort(context.resources.getString(R.string.voice_play_too_short))
            recordStatusListener?.cancelRecord("时间过短, 取消发送")

            startAnimation(tooShortAnim)
        } else {
            if (isCancelable) {
                isCancelable = false
                Log.e(TAG, "手动滑出, 取消发送")
                recordStatusListener?.cancelRecord("手动滑出, 取消发送")
            } else {
                Log.e(TAG, "已完成, 发送")
                recordStatusListener?.sendRecord("已完成, 发送")
            }
        }
        textStatusListener?.hideStatus()
        invalidate()
    }


    /**
     * 设置进度条颜色
     */
    private fun setProgressColor(color: Int) {
        this.progressColor = color
        progressPaint?.color = progressColor
    }


    /**
     * 设置进度条位置
     */
    private fun setProgressNum(progressNum: Float) {
        this.progressNum = progressNum
        startAnimation(progressAnim)
    }


    private fun measureSize(defaultSize: Int, measureSpec: Int): Int {
        var result = defaultSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = result.coerceAtMost(specSize)
        }
        return result
    }


    private fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}