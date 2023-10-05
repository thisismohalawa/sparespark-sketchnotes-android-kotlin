package sparespark.sketchnotes.notes.notedetail.buildlogic

import android.app.Application
import sparespark.sketchnotes.core.FCM_BASE_URL
import sparespark.sketchnotes.data.db.NoteDatabase
import sparespark.sketchnotes.data.remote.network.APIService
import sparespark.sketchnotes.data.remote.network.Client
import sparespark.sketchnotes.data.remote.provider.team.RemoteTeamProviderImpl
import sparespark.sketchnotes.notes.base.BaseNoteBuildLogic
import sparespark.sketchnotes.notes.notedetail.viewmodel.NoteViewModelFactory

class NoteDetailInjector(
    application: Application,
) : BaseNoteBuildLogic(application) {
    fun provideNoteViewModelFactory(): NoteViewModelFactory = NoteViewModelFactory(
        noteRepo = getNoteRepository(), remoteTeamProvider = RemoteTeamProviderImpl(
            userDao = NoteDatabase.getInstance(getApplication()).userDao(),
            apiService = Client.getRetrofit(FCM_BASE_URL)?.create(APIService::class.java) as APIService
        )
    )
}
