package sparespark.sketchnotes.core.view

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import sparespark.sketchnotes.notes.NoteActivity

internal fun Fragment.relaunchCurrentView() {
    findNavController().apply {
        currentDestination?.id?.let {
            this.popBackStack(it, true)
            this.navigate(it)
        }
    }
}

internal fun startListActivity(activity: Activity?) {
    activity?.startActivity(
        Intent(
            activity,
            NoteActivity::class.java
        )
    ).also { activity?.finish() }
}

internal fun attachFragment(
    manager: FragmentManager,
    containerId: Int,
    view: Fragment,
    tag: String
) {
    manager.beginTransaction()
        .replace(containerId, view, tag)
        .commitNowAllowingStateLoss()
}
