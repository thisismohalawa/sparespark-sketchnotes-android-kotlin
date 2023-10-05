package sparespark.sketchnotes.notes.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import sparespark.sketchnotes.data.db.NoteDatabase
import sparespark.sketchnotes.data.implementation.NoteRepositoryImpl
import sparespark.sketchnotes.data.repository.NoteRepository

open class BaseNoteBuildLogic(
    val app: Application
) : AndroidViewModel(app) {

    protected fun getNoteRepository(): NoteRepository {
        return NoteRepositoryImpl(
            dao = NoteDatabase.getInstance(getApplication()).noteDao()
        )
    }
}
