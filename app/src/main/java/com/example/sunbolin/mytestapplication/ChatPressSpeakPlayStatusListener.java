package com.example.sunbolin.mytestapplication;

public interface ChatPressSpeakPlayStatusListener {
    void startSoundRecording(String log);

    void stopSoundRecording(String log);

    void sendRecord(String log);

    void cancelRecord(String log);
}
