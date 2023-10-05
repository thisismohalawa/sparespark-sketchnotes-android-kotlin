package sparespark.sketchnotes.login.buildlogic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import sparespark.sketchnotes.data.db.NoteDatabase
import sparespark.sketchnotes.data.implementation.LoginRepositoryImpl
import sparespark.sketchnotes.data.repository.LoginRepository
import sparespark.sketchnotes.login.viewmodel.LoginViewModelFactory

/*
* Custom written dependency injection class,
* which extends viewModel.
*
*
* */
class LoginInjector(
    app: Application
) : AndroidViewModel(app) {

    private fun getUserRepository(): LoginRepository =
        LoginRepositoryImpl(userDao = NoteDatabase.getInstance(getApplication()).userDao())

    fun provideUserViewModelFactory(): LoginViewModelFactory =
        LoginViewModelFactory(getUserRepository())
}
