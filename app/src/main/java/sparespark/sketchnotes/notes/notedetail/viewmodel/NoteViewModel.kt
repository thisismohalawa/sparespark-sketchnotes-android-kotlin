package sparespark.sketchnotes.notes.notedetail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.getCalendarDateTime
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.core.result.SingleLiveData
import sparespark.sketchnotes.core.result.UiResource
import sparespark.sketchnotes.core.secret.INVALID_ADDRESS
import sparespark.sketchnotes.core.secret.MATCHED_ADDRESS
import sparespark.sketchnotes.core.secret.SIGN_IN_NOT_COMPLETED
import sparespark.sketchnotes.core.view.BaseViewModel
import sparespark.sketchnotes.core.view.isEmailAddressValid
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.data.model.notification.NotifyData
import sparespark.sketchnotes.data.model.notification.NotifySender
import sparespark.sketchnotes.data.model.shared.SharedUser
import sparespark.sketchnotes.data.remote.provider.team.RemoteTeamProvider
import sparespark.sketchnotes.data.repository.NoteRepository
import sparespark.sketchnotes.notes.notedetail.NoteDetailEvent
import kotlin.coroutines.CoroutineContext

class NoteViewModel(
    private val noteRepo: NoteRepository,
    private val remoteTeamProvider: RemoteTeamProvider,
    uiContext: CoroutineContext,
) : BaseViewModel<NoteDetailEvent>(uiContext) {

    companion object {
        private var isFirstTimeSharing = false
    }

    // communicate with view based on what happen in particular state.
    internal val bottomSheetViewState = MutableLiveData<Int>()
    internal val textSharedNoteState = MutableLiveData<UiResource>()
    internal val iconSharedNoteState = MutableLiveData<Int>()
    internal val noteItemSharedState = MutableLiveData<Boolean>()

    // action
    internal val actionLoginAttempt = MutableLiveData<Unit>()
    internal val actionInviteAttempt = MutableLiveData<Unit>()
    internal val actionSharingProcessAttempt = MutableLiveData<Unit>()

    private val noteState = MutableLiveData<Note>()
    val note: MutableLiveData<Note> get() = noteState

    private val updatedState = SingleLiveData<Boolean>()
    val updated: LiveData<Boolean> get() = updatedState

    private val deletedState = SingleLiveData<Boolean>()
    val deleted: LiveData<Boolean> get() = deletedState

    override fun handleEvent(event: NoteDetailEvent) {
        when (event) {
            is NoteDetailEvent.OnDestroy -> jobTracker.cancel()
            is NoteDetailEvent.UpdateBottomSheetToHideState -> updateBottomSheetToHideState()
            is NoteDetailEvent.UpdateBottomSheetToExpandState -> updateBottomSheetToExpandState()
            is NoteDetailEvent.OnNoteViewStartGetArgs -> onStart(event.argsNote)
            is NoteDetailEvent.UpdateNoteCardColor -> updateNoteCardHexColor(event.hexColor)
            is NoteDetailEvent.OnUpdateNoteClicked -> updateNote(
                event.content, event.title, event.email
            )

            is NoteDetailEvent.DeleteNote -> deleteNote()
            is NoteDetailEvent.OnAddEmailIconClicked -> onSharedEmailIconClicked(event.email)
        }
    }

    fun isBottomSheetAtExpandingState(): Boolean =
        bottomSheetViewState.value != BottomSheetBehavior.STATE_HIDDEN

    private fun isANewNote(): Boolean = noteState.value?.creationDate.isNullOrEmpty()

    private fun isSharedNote(): Boolean = !noteState.value?.sharedUId.isNullOrEmpty()

    private fun String.isSharedProcessNotComplete(): Boolean =
        this.isEmailAddressValid() && !isSharedNote()

    private fun updateBottomSheetToExpandState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun updateBottomSheetToHideState() {
        bottomSheetViewState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun updateNoteCardHexColor(hex: String) {
        noteState.value?.hexCardColor = hex
    }

    private fun onStart(argsNote: Note) {
        noteState.value = argsNote
        updateBottomSheetToExpandState()

        if (isSharedNote()) {
            updateTextSharedState(isShared = true)
            updateIconSharedState(isShared = true)
            updateNoteSharedState(isShared = true)
        } else {
            updateTextSharedState(isShared = false)
            updateIconSharedState(isShared = false)
            updateNoteSharedState(isShared = false)
        }
    }

    private fun updateNote(content: String, title: String, email: String) = launch {
        if (email.isSharedProcessNotComplete()) {
            actionSharingProcessAttempt.value = Unit
            return@launch
        }
        showLoading()
        if (isANewNote()) noteState.value?.creationDate = getCalendarDateTime()
        when (noteRepo.updateNote(
            noteState.value!!.copy(
                content = content, title = title
            )
        )) {
            is DataResult.Error -> handleError(R.string.cannot_update_note)
            is DataResult.Value -> {
                updatedState.value = true
                hideLoading()

                if (isSharedNote() && isFirstTimeSharing) sendSharedNoteNotification()

            }
        }
    }

    private fun deleteNote() = launch {
        if (!isANewNote()) {
            showLoading()
            when (noteState.value?.let {
                noteRepo.deleteNote(note = it)
            }) {
                is DataResult.Error -> handleError(R.string.cannot_update_note)
                is DataResult.Value -> {
                    deletedState.value = true
                    hideLoading()
                }

                else -> {}
            }
        } else deletedState.value = true
    }

    private fun onSharedEmailIconClicked(email: String) = launch {
        if (isSharedNote()) {
            cancelSharing()
            return@launch
        }
        showLoading()
        when (val result = remoteTeamProvider.getSharedTeamDetails(email = email)) {
            is DataResult.Error -> when (result.error.message) {
                INVALID_ADDRESS -> handleError(R.string.invalid_email)
                SIGN_IN_NOT_COMPLETED -> actionLoginAttempt.value = Unit
                MATCHED_ADDRESS -> handleError(R.string.cannot_user_current_email)
                else -> handleError()
            }

            is DataResult.Value -> if (result.value == null) {
                actionInviteAttempt.value = Unit
            } else if (result.value.uid?.isNotBlank() == true) {
                isFirstTimeSharing = true
                updateTextSharedState(isShared = true)
                updateIconSharedState(isShared = true)
                updateTargetSharedUserDetails(result.value)
            } else handleError(R.string.could_not_get_shared_user_data)
        }
        hideLoading()

    }

    private fun sendSharedNoteNotification() = launch {
        val notifyData = NotifyData(
            sent = noteState.value?.sharedUId
        )
        val sender = NotifySender(
            data = notifyData, to = noteState.value?.sharedUToken ?: ""
        )
        when (remoteTeamProvider.sendSharedMemoNotifyRequest(sender)) {
            is DataResult.Value -> Unit
            is DataResult.Error -> Log.d("sendNotify", "sendSharedNoteNotification error occurred!")
        }
    }

    private fun handleError(stringRes: Int = R.string.error_try_again) {
        errorState.value = UiResource.StringResource(stringRes)
    }

    private fun updateTextSharedState(isShared: Boolean) {
        if (isShared) textSharedNoteState.value = UiResource.StringResource(R.string.shared)
        else textSharedNoteState.value = UiResource.StringResource(R.string.only_you)
    }

    private fun updateIconSharedState(isShared: Boolean) {
        if (isShared) iconSharedNoteState.value = R.drawable.ic_shared
        else iconSharedNoteState.value = R.drawable.ic_add_group
    }

    private fun updateNoteSharedState(isShared: Boolean) {
        noteItemSharedState.value = isShared
    }

    private suspend fun updateTargetSharedUserDetails(sharedUser: SharedUser) {
        noteState.value?.sharedUId = sharedUser.uid
        noteState.value?.sharedUToken = sharedUser.token
        noteState.value?.owner = when (val result = remoteTeamProvider.getOwnerUserName()) {
            is DataResult.Value -> result.value
            else -> "Your Buddy!"
        }
    }

    private fun cancelSharing() {
        isFirstTimeSharing = false
        noteState.value?.sharedUId = null
        updateTextSharedState(isShared = false)
        updateIconSharedState(isShared = false)
    }
}
