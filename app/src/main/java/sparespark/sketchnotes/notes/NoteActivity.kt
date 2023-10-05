package sparespark.sketchnotes.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_note.progress_circular
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.actionCopyText
import sparespark.sketchnotes.core.actionDisplayConfirmationDialog
import sparespark.sketchnotes.core.actionOpenUrl
import sparespark.sketchnotes.core.actionOpenWhatsApp
import sparespark.sketchnotes.core.actionShareText
import sparespark.sketchnotes.core.handlerPostDelayed
import sparespark.sketchnotes.core.view.Communicator
import sparespark.sketchnotes.core.view.visible


class NoteActivity : AppCompatActivity(), Communicator.View, Communicator.Action {

    private lateinit var navController: NavController

    companion object {
        private var doubleBackToExitPressedOnce = false
        private var currentNavDestination = ""
        var isClipDataReceived = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.findNavController()


        onBackPressedDispatcher.addCallback(this@NoteActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (this@NoteActivity::navController.isInitialized) currentNavDestination =
                        navController.currentDestination?.label.toString()

                    when {
                        doubleBackToExitPressedOnce -> {
                            finish()
                            return
                        }

                        currentNavDestination == "NoteListView" -> {
                            doubleBackToExitPressedOnce = true
                            handlerPostDelayed(2000) {
                                doubleBackToExitPressedOnce = false
                            }
                        }

                        else -> finish()
                    }
                }
            })

    }

    override fun showProgress() {
        progress_circular.visible(true)
    }

    override fun hideProgress() {
        progress_circular.visible(false)
    }

    override fun showAlertActionDialog(msgResId: Int, actionResId: Int?, action: (() -> Unit)?) {
        this@NoteActivity.actionDisplayConfirmationDialog(
            msgResId, actionResId, action
        )
    }

    override fun getSentIntentClipData(): String? = try {
        if (Intent.ACTION_SEND == intent.action &&
            !isClipDataReceived &&
            intent.clipData != null &&
            intent.clipData!!.itemCount > 0
        ) intent.clipData?.getItemAt(
            0
        )?.coerceToText(this@NoteActivity).toString() else null
    } catch (e: Exception) {
        null
    }

    override fun onStop() {
        isClipDataReceived = false
        super.onStop()
    }
}
