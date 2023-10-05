package sparespark.sketchnotes.notes.notedetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.sketchnotes.data.remote.provider.team.RemoteTeamProvider
import sparespark.sketchnotes.data.repository.NoteRepository

class NoteViewModelFactory(
    private val noteRepo: NoteRepository,
    private val remoteTeamProvider: RemoteTeamProvider
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(noteRepo, remoteTeamProvider, Dispatchers.Main) as T
    }
}
