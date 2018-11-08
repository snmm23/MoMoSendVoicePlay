package com.example.sunbolin.mytestapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatPressSpeakPlayView extends FrameLayout {

     // View宽高
    private int mWidth;
    private int mHeight;
    // 圆半径
    private float mRadius;

    // 圆的边界
    private float mCircleLeftBorder;
    private float mCircleRightBorder;
    private float mCircleTopBorder;
    private float mCircleBottomBorder;

    //画笔状态
    private Paint mCirclePaint;
    private int mCircleColor;

    private ImageView mMicView;
    private ImageView mTrashCanView;
    private CircleBarView progress;
    private ImageView tooShortView;
    private Subscription disposable;

    //是否为按住状态
    private boolean isPressed;
    //是否可以取消
    private boolean isCancelable;
    //当前进度
    private long nowPosition;
    //进度是否全部走完
    private boolean isFullFinish;

    //录音器发送者监听
    private ChatPressSpeakPlayStatusListener playStatusListener;
    //提示文字监听
    private ChatVoiceTextStatusListener textStatusListener;

    public ChatPressSpeakPlayView(Context context) {
        this(context, null);
    }

    public ChatPressSpeakPlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatPressSpeakPlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initDefaultValue(context);
        initPaint();
    }

    public void setPlayStatusListener(ChatPressSpeakPlayStatusListener playStatusListener){
        this.playStatusListener = playStatusListener;
    }

    public void setTextStatusListener(ChatVoiceTextStatusListener textStatusListener){
        this.textStatusListener = textStatusListener;
    }

    private void initDefaultValue(Context context) {
        isPressed = false;
        isCancelable = false;
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        mMicView = new ImageView(context);
        mMicView.setVisibility(View.VISIBLE);
        mMicView.setImageResource(R.mipmap.ic_chat_voice_speak);
        addView(mMicView, lp);

        mTrashCanView = new ImageView(context);
        mTrashCanView.setVisibility(View.GONE);
        mTrashCanView.setImageResource(R.mipmap.ic_voice_delete);
        addView(mTrashCanView, lp);

        progress = new CircleBarView(context);
        LayoutParams lp1 = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(progress, lp1);

        tooShortView = new ImageView(context);
        tooShortView.setVisibility(GONE);
        tooShortView.setBackgroundResource(R.drawable.bg_voice_speak_play_short);
        LayoutParams lp2 = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(tooShortView, lp2);
    }

    private void initPaint() {
        mCirclePaint = new Paint();
        mCircleColor = ContextCompat.getColor(getContext(), R.color.white);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        }

        mRadius = (float) ((mWidth) / 2);

        mCircleLeftBorder = mWidth / 2 - mRadius;
        mCircleRightBorder = mWidth / 2 + mRadius;
        mCircleTopBorder = mHeight / 2 - mRadius;
        mCircleBottomBorder = mHeight / 2 + mRadius;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPressed) {
            mMicView.setVisibility(GONE);
            if (isCancelable) {
                progress.setCircleProgressColor(ContextCompat.getColor(getContext(), R.color.pink_FF4081));
                textStatusListener.setStatusTextColor(ContextCompat.getColor(getContext(), R.color.pink_FF4081));
                textStatusListener.setStatusTextFirst(getContext().getResources().getString(R.string.voice_release_cancel));
                mTrashCanView.setVisibility(VISIBLE);
            }else{
                if(nowPosition < 50000){
                    progress.setCircleProgressColor(ContextCompat.getColor(getContext(), R.color.blue_48B5ED));
                    textStatusListener.setStatusTextColor(ContextCompat.getColor(getContext(), R.color.black_1a));
                }
                textStatusListener.setStatusTextFirst(getContext().getResources().getString(R.string.voice_up_slide_cancel));
                mTrashCanView.setVisibility(GONE);
            }
        }else{
            mMicView.setVisibility(VISIBLE);
            mTrashCanView.setVisibility(GONE);
        }

        mCirclePaint.setColor(mCircleColor);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isFullFinish && !isPressed){
                    return true;
                }
                if (event.getX() < mCircleRightBorder
                        && event.getX() > mCircleLeftBorder
                        && event.getY() < mCircleBottomBorder
                        && event.getY() > mCircleTopBorder) {
                    isPressed = true;
                    isFullFinish = false;
                    isCancelable = false;

                    Log.e("sunbolin", "已开始, 开始录音");
                    if(playStatusListener != null){
                        playStatusListener.startSoundRecording("已开始, 开始录音");
                    }
                    startTime();
                    invalidate();
                    textStatusListener.showStatus();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isFullFinish && isPressed){
                    return true;
                }
                float x = event.getX();
                float y = event.getY();
                isCancelable = (y < mCircleTopBorder)
                        || (y > mCircleBottomBorder)
                        || (x > mCircleRightBorder)
                        || (x < mCircleLeftBorder);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(isFullFinish && isPressed) {
                    return true;
                }
                if(!isCancelable && nowPosition > 0 && nowPosition <= 1000){
                    doFinish(true);
                }else {
                    doFinish(false);
                }
                break;
        }
        return true;
    }


    private void doFinish(boolean isTooShort){
        if(!isPressed){
            return;
        }
        isPressed = false;

        Log.e("sunbolin", "结束, 停止录音");
        if(playStatusListener != null){
            playStatusListener.stopSoundRecording("结束, 停止录音");
        }

        if(isTooShort){
            Log.e("sunbolin", "时间过短, 取消发送");

            textStatusListener.setStatusTextColor(ContextCompat.getColor(getContext(), R.color.yellow_FFE1B24A));
            textStatusListener.setStatusTextShort(getContext().getResources().getString(R.string.voice_play_too_short));

            if(playStatusListener != null){
                playStatusListener.cancelRecord("时间过短, 取消发送");
            }

            ObjectAnimator animatorSet = ObjectAnimator.ofFloat(tooShortView,
                    "alpha", 1, 1, 1, 1, 1, 1, 1, 0.9f, 0.7f, 0);
            animatorSet.addListener(animatorListener);
            animatorSet.setDuration(1000);
            animatorSet.start();

        }else {
            if (isCancelable) {
                isCancelable = false;
                Log.e("sunbolin", "手动滑出, 取消发送");
                if(playStatusListener != null){
                    playStatusListener.cancelRecord("手动滑出, 取消发送");
                }
            } else {
                Log.e("sunbolin", "已完成, 发送");
                if(playStatusListener != null){
                    playStatusListener.sendRecord("已完成, 发送");
                }
            }
        }

        textStatusListener.hideStatus();

        invalidate();

        if (disposable != null) {
            disposable.unsubscribe();
            disposable = null;
        }
        nowPosition = 0;
        progress.setProgressNum(0, 0);
        isFullFinish = false;
    }


    private void startTime(){
        disposable = Observable.interval(10, TimeUnit.MILLISECONDS)
                .take(6000)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    nowPosition = nowPosition + 10;

                    textStatusListener.setStatusTextTime((int) (nowPosition / 1000));

                    if(nowPosition >= 50000){
                        progress.setCircleProgressColor(ContextCompat.getColor(getContext(), R.color.pink_FF4081));
                        textStatusListener.setStatusTextColor(ContextCompat.getColor(getContext(), R.color.pink_FF4081));
                    }

                    progress.setProgressNum(nowPosition, 0);

                    if(nowPosition >= 60000){
                        isFullFinish = true;
                        isCancelable = false;
                        doFinish(false);
                    }
                }, throwable -> throwable.printStackTrace());
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            tooShortView.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            tooShortView.setVisibility(GONE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };
}
