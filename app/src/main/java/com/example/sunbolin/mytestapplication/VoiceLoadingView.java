package com.example.sunbolin.mytestapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;

public class VoiceLoadingView extends LinearLayout {

    private int color;
    private View one, two, three, four, five;
    ArrayList<View> views = new ArrayList<>();

    public VoiceLoadingView(Context context) {
        super(context);
        initView(null);
    }

    public VoiceLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public VoiceLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs){
        LayoutInflater.from(getContext()).inflate(R.layout.voice_loading_layout, this);
        if(attrs!= null){
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.VoiceLoadingView);
            color = typedArray.getColor(R.styleable.VoiceLoadingView_vlv_bg_color, getContext().getResources().getColor(R.color.gray_d8));
            typedArray.recycle();
        }else{
            color = getContext().getResources().getColor(R.color.gray_d8);
        }

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        views.add(one);
        views.add(two);
        views.add(three);
        views.add(four);
        views.add(five);

        setColor(color);
        playAnimators();
    }

    public void setColor(int color){
        for (View view : views) {
            GradientDrawable mGroupDrawable= (GradientDrawable) view.getBackground();
            mGroupDrawable.setColor(color);
        }
    }

    private void playAnimators() {
        ArrayList<ObjectAnimator> animators = new ArrayList<>();
        long[] delays = new long[]{400, 200, 0, 200, 400};
        for (int i = 0; i < 5; i ++) {
            ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(views.get(i),
                    "scaleY", 1f, 0.4f, 1f);
            scaleAnim.setDuration(1000);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            animators.add(scaleAnim);
        }
        for (int i = 0; i < animators.size(); i ++) {
            ObjectAnimator animator = animators.get(i);
            animator.start();
        }
    }
}
