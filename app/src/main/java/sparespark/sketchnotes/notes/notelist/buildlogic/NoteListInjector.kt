package sparespark.sketchnotes.notes.notelist.buildlogic

import android.app.Application
import sparespark.sketchnotes.data.remote.provider.token.RemoteTokenProviderImpl
import sparespark.sketchnotes.notes.base.BaseNoteBuildLogic
import sparespark.sketchnotes.notes.notelist.viewmodel.NoteListViewModelFactory

class NoteListInjector(
    app: Application
) : BaseNoteBuildLogic(app) {

    fun provideNoteListViewModelFactory(): NoteListViewModelFactory =
        NoteListViewModelFactory(
            noteRepo = getNoteRepository(),
            remoteTokenProvider = RemoteTokenProviderImpl()
        )
}
