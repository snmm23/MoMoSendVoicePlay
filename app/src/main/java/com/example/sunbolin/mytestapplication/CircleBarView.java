package com.example.sunbolin.mytestapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

public class CircleBarView extends View {

    private Paint bgPaint;//绘制背景圆弧的画笔
    private Paint progressPaint;//绘制圆弧的画笔

    private RectF mRectF;//绘制圆弧的矩形区域

    private CircleBarAnim anim;

    private float progressNum;//可以更新的进度条数值
    private float maxNum;//进度条最大值

    private int progressColor;//进度条圆弧颜色
    private int bgColor;//背景圆弧颜色
    private float startAngle;//背景圆弧的起始角度
    private float sweepAngle;//背景圆弧扫过的角度
    private float barWidth;//圆弧进度条宽度

    private int defaultSize;//自定义View默认的宽高
    private float progressSweepAngle;//进度条圆弧扫过的角度

    private TextView textView;
    private OnAnimationListener onAnimationListener;

    public CircleBarView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){

        progressColor = ContextCompat.getColor(context, R.color.blue_48B5ED);
        bgColor = ContextCompat.getColor(context, R.color.transparent);
        startAngle = -90;
        sweepAngle = 360;
        barWidth = dip2px(context,4);

        progressNum = 0;
        maxNum = 60000;
        defaultSize = dip2px(context,100);
        mRectF = new RectF();

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);//设置抗锯齿
        progressPaint.setStrokeWidth(barWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);//设置画笔为圆角

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        bgPaint.setColor(bgColor);
        bgPaint.setAntiAlias(true);//设置抗锯齿
        bgPaint.setStrokeWidth(barWidth);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        anim = new CircleBarAnim();
    }

    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = measureSize(defaultSize, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

        if(min >= barWidth * 2){
            mRectF.set(barWidth/2,barWidth/2,min-barWidth/2,min-barWidth/2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(mRectF,startAngle,sweepAngle,false,bgPaint);
        canvas.drawArc(mRectF,startAngle,progressSweepAngle,false, progressPaint);
    }

    public class CircleBarAnim extends Animation{

        public CircleBarAnim(){
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {//interpolatedTime从0渐变成1,到1时结束动画,持续时间由setDuration（time）方法设置
            super.applyTransformation(interpolatedTime, t);
            progressSweepAngle = interpolatedTime * sweepAngle * progressNum / maxNum;
            if(onAnimationListener!=null){
                if(textView !=null){
                    textView.setText(onAnimationListener.howToChangeText(interpolatedTime, progressNum,maxNum));
                }
                onAnimationListener.howTiChangeProgressColor(progressPaint,interpolatedTime, progressNum,maxNum);
            }
            postInvalidate();
        }
    }

    private int measureSize(int defaultSize,int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    public void setCircleProgressColor(int color){
        this.progressColor = color;
        if(progressPaint != null){
            progressPaint.setColor(progressColor);
        }
    }

    public void setMaxNum(float maxNum) {
        this.maxNum = maxNum;
    }

    public void setProgressNum(float progressNum, int time) {
        this.progressNum = progressNum;
        anim.setDuration(time);
        this.startAnimation(anim);
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public interface OnAnimationListener {

        String howToChangeText(float interpolatedTime, float updateNum, float maxNum);

        void howTiChangeProgressColor(Paint paint, float interpolatedTime, float updateNum, float maxNum);
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }
}
