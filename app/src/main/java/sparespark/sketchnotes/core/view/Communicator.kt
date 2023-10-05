package sparespark.sketchnotes.core.view

interface Communicator {
    interface View {
        fun showProgress()
        fun hideProgress()
    }

    interface Action {
        fun showAlertActionDialog(msgResId: Int, actionResId: Int?, action: (() -> Unit)?)
        fun getSentIntentClipData(): String?
    }
}
