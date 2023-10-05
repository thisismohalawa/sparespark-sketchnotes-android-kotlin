package sparespark.sketchnotes.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import sparespark.sketchnotes.data.repository.LoginRepository

class LoginViewModelFactory(
    private val repo: LoginRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repo, Dispatchers.Main) as T
    }
}
