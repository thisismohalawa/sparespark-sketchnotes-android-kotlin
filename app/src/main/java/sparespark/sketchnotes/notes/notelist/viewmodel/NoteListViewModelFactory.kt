package sparespark.sketchnotes.notes.notelist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.sketchnotes.data.remote.provider.token.RemoteTokenProvider
import sparespark.sketchnotes.data.repository.NoteRepository

class NoteListViewModelFactory(
    private val noteRepo: NoteRepository,
    private val remoteTokenProvider: RemoteTokenProvider
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteListViewModel(noteRepo, remoteTokenProvider, Dispatchers.Main) as T
    }
}
