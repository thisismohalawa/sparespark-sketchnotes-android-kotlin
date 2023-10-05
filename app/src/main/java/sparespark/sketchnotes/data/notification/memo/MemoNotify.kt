package sparespark.sketchnotes.data.notification.memo

import sparespark.sketchnotes.core.result.DataResult

interface MemoNotify {
    fun showSharedMemeNotification(title: String?, content: String?):DataResult<Exception,Unit>
}
