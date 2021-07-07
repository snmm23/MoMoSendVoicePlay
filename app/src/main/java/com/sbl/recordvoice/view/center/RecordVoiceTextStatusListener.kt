package com.sbl.recordvoice.view.center


/**
 * sunbolin 2021/7/7
 */
interface RecordVoiceTextStatusListener {

    fun setStatusTextColor(color: Int)

    fun setStatusTextTime(time: Int)

    fun setStatusTextFirst(text: String?)

    fun setStatusTextShort(text: String?)

    fun showStatus()

    fun hideStatus()
}