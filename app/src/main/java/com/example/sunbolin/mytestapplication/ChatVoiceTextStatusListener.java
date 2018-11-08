package com.example.sunbolin.mytestapplication;

public interface ChatVoiceTextStatusListener {
    void setStatusTextColor(int color);

    void setStatusTextTime(int time);

    void setStatusTextFirst(String text);

    void setStatusTextShort(String text);

    void showStatus();

    void hideStatus();
}
