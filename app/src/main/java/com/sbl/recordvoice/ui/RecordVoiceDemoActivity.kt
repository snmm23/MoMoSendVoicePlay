package com.sbl.recordvoice.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sbl.recordvoice.R
import com.sbl.recordvoice.view.RecordVoiceLayout
import com.sbl.recordvoice.view.center.RecordVoiceRecordStatusListener


/**
 * sunbolin 2021/7/7
 */
class RecordVoiceDemoActivity : AppCompatActivity(), RecordVoiceRecordStatusListener {


    private lateinit var playStatusText: TextView
    private lateinit var recordVoiceLayout: RecordVoiceLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_voice_demo)

        playStatusText = findViewById(R.id.playStatusText)
        recordVoiceLayout = findViewById(R.id.recordVoiceLayout)
        recordVoiceLayout.setRecordStatusListener(this)
    }


    override fun startSoundRecording(log: String?) {
        playStatusText.text = log
    }


    override fun stopSoundRecording(log: String?) {
        playStatusText.text = log
    }


    override fun sendRecord(log: String?) {
        playStatusText.text = log
    }


    override fun cancelRecord(log: String?) {
        playStatusText.text = log
    }
}