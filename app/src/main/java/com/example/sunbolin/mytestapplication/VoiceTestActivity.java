package com.example.sunbolin.mytestapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class VoiceTestActivity extends AppCompatActivity implements ChatPressSpeakPlayStatusListener {

    private TextView playStatusText;
    private ChatVoiceLayout chatVoiceLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_test);
        playStatusText = findViewById(R.id.playStatusText);
        chatVoiceLayout = findViewById(R.id.chatVoiceLayout);
        chatVoiceLayout.setPressSpeakPlayStatusListener(this);
    }

    @Override
    public void startSoundRecording(String log) {
        playStatusText.setText(log);
    }

    @Override
    public void stopSoundRecording(String log) {
        playStatusText.setText(log);
    }

    @Override
    public void sendRecord(String log) {
        playStatusText.setText(log);
    }

    @Override
    public void cancelRecord(String log) {
        playStatusText.setText(log);
    }
}
