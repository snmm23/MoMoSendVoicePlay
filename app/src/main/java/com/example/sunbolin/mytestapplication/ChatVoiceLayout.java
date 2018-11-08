package com.example.sunbolin.mytestapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;

public class ChatVoiceLayout extends LinearLayout implements ChatVoiceTextStatusListener {

    private ChatPressSpeakPlayView pressSpeakPlayView;
    private TextView statusText;
    private LinearLayout statusLayout;
    private ObjectAnimator showAnimator;
    private ObjectAnimator hideAnimator;
    private VoiceLoadingView loading1, loading2;

    private String first;

    public ChatVoiceLayout(Context context) {
        super(context);
        initView();
    }

    public ChatVoiceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ChatVoiceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        LayoutInflater.from(getContext()).inflate(R.layout.chat_voice_layout, this);
        pressSpeakPlayView = findViewById(R.id.pressSpeakView);
        statusText = findViewById(R.id.statusText);
        statusLayout = findViewById(R.id.statusLayout);
        loading1 = findViewById(R.id.loading1);
        loading2 = findViewById(R.id.loading2);
        pressSpeakPlayView.setTextStatusListener(this);
    }


    public void setPressSpeakPlayStatusListener(ChatPressSpeakPlayStatusListener listener){
        pressSpeakPlayView.setPlayStatusListener(listener);
    }

    @Override
    public void setStatusTextColor(int color) {
        statusText.setTextColor(color);
        loading1.setColor(color);
        loading2.setColor(color);
    }

    @Override
    public void setStatusTextTime(int time) {
        statusText.setText(first + " " + String.format("%02d", time) + "\"");
    }

    @Override
    public void setStatusTextFirst(String text) {
        first = text;
    }

    @Override
    public void setStatusTextShort(String text) {
        statusText.setText(text);
    }

    @Override
    public void showStatus() {
        statusLayout.setVisibility(VISIBLE);
        if(hideAnimator != null){
            hideAnimator.cancel();
            hideAnimator = null;
        }
        showAnimator = ObjectAnimator.ofFloat(statusLayout, "alpha", 0, 1);
        showAnimator.setDuration(1000);
        showAnimator.start();
    }

    @Override
    public void hideStatus() {
        if(showAnimator != null){
            showAnimator.cancel();
            showAnimator = null;
        }
        hideAnimator = ObjectAnimator.ofFloat(statusLayout, "alpha", 1, 0);
        hideAnimator.setDuration(1000);
        hideAnimator.start();
    }
}
