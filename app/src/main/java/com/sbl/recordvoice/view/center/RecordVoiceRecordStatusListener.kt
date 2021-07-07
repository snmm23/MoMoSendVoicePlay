package com.sbl.recordvoice.view.center


/**
 * sunbolin 2021/7/7
 */
interface RecordVoiceRecordStatusListener {

    fun startSoundRecording(log: String?)

    fun stopSoundRecording(log: String?)

    fun sendRecord(log: String?)

    fun cancelRecord(log: String?)
}