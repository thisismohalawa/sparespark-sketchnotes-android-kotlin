package sparespark.sketchnotes.notes.notelist.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.UPDATE_REQUEST_CODE
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.core.result.SingleLiveData
import sparespark.sketchnotes.core.result.UiResource
import sparespark.sketchnotes.core.secret.localInfoNotesStaticItems
import sparespark.sketchnotes.core.view.BaseViewModel
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.data.remote.provider.token.RemoteTokenProvider
import sparespark.sketchnotes.data.repository.NoteRepository
import sparespark.sketchnotes.notes.notelist.NoteListEvent
import kotlin.coroutines.CoroutineContext


/*
* Viewmodel in the middle, handle both behavior and state,
* state being data and behavior represent actions we do when some event happens.
* Viewmodel use observer pattern to communicate with the view,
* and ask model for data.
*
*
*
* */

class NoteListViewModel(
    private val noteRepo: NoteRepository,
    private val remoteTokenProvider: RemoteTokenProvider,
    uiContext: CoroutineContext
) : BaseViewModel<NoteListEvent>(uiContext) {

    // UI Binding
    internal val noteItemClicked = SingleLiveData<Note>()

    private val noteListState = MutableLiveData<List<Note>?>()
    val noteList: MutableLiveData<List<Note>?> get() = noteListState

    private val deletedState = SingleLiveData<Boolean>()
    val deleted: LiveData<Boolean> get() = deletedState

    override fun handleEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.OnDestroy -> jobTracker.cancel()
            is NoteListEvent.UpdateRemoteToken -> updateRemoteToken()
            is NoteListEvent.OnNoteListViewStart -> onNoteListViewStart(event.context)
            is NoteListEvent.OnNoteItemClick -> noteItemClick(event.note)
            is NoteListEvent.DeleteNote -> deleteNote(event.note)
            is NoteListEvent.GetFilteredNotes -> getNotes()

        }
    }

    private fun updateRemoteToken() = launch {
        when (remoteTokenProvider.updateToken()) {
            is DataResult.Error -> handleError(R.string.error_updating_token)
            else -> Unit
        }
    }

    private fun onNoteListViewStart(context: Context?) = launch {
        context?.let {
            getTotalNotesWithInfoItem(it)
            checkForAppUpdates(it)
        }
    }

    private fun getTotalNotesWithInfoItem(
        context: Context,
    ) = launch {
        showLoading()
        when (val notesResult = noteRepo.getNotes()) {
            is DataResult.Error -> handleError(R.string.error_while_loading)
            is DataResult.Value -> {
                val newList = localInfoNotesStaticItems(context).toMutableList()
                for (note in notesResult.value) newList.add(note)
                noteListState.value = newList
                hideLoading()
            }
        }
    }

    private fun deleteNote(note: Note) = launch {
        if (note.creationDate != "") {
            showLoading()
            when (noteRepo.deleteNote(note)) {
                is DataResult.Error -> handleError(R.string.cannot_update_note)
                is DataResult.Value -> {
                    deletedState.value = true
                    hideLoading()
                }

            }
        }
    }

    private fun getNotes() = launch {
        showLoading()
        when (val notesResult = noteRepo.getNotes()) {
            is DataResult.Error -> handleError(R.string.error_while_loading)
            is DataResult.Value -> {
                noteListState.value = notesResult.value
                hideLoading()
            }
        }
    }

    private fun checkForAppUpdates(context: Context) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        it, AppUpdateType.IMMEDIATE, context as Activity, UPDATE_REQUEST_CODE
                    )

                } catch (ex: Exception) {
                    handleError(R.string.error_while_app_updating)
                }
            }

        }.addOnFailureListener {
            handleError(R.string.error_while_app_updating)
        }
    }


    private fun handleError(stringRes: Int = R.string.error_try_again) {
        errorState.value = UiResource.StringResource(stringRes)
    }

    private fun noteItemClick(note: Note) {
        noteItemClicked.value = note
    }
}
