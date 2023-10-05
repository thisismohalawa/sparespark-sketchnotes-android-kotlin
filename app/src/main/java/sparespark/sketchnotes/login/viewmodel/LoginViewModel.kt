package sparespark.sketchnotes.login.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.SIGN_IN_REQUEST_CODE
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.data.model.LoginResult
import sparespark.sketchnotes.core.view.BaseViewModel
import sparespark.sketchnotes.core.result.SingleLiveData
import sparespark.sketchnotes.core.result.UiResource
import sparespark.sketchnotes.data.model.user.User
import sparespark.sketchnotes.data.repository.LoginRepository
import sparespark.sketchnotes.login.LoginEvent
import kotlin.coroutines.CoroutineContext


class LoginViewModel(
    private val repo: LoginRepository, uiContext: CoroutineContext
) : BaseViewModel<LoginEvent<LoginResult>>(uiContext) {

    // UI binding, auth status
    internal val signInStatusText = MutableLiveData<UiResource>()
    internal val signInStorageText = MutableLiveData<UiResource>()
    internal val signInStorageGif = MutableLiveData<Int>()
    internal val authButtonText = MutableLiveData<UiResource>()
    internal val userRemoteSyncToast = MutableLiveData<UiResource>()

    // control Logic,
    // communicate with view based on what happen in particular state.
    // trigger an event, does not care about any value has been passed in that event.
    internal val authAttempt = MutableLiveData<Unit>()

    // view ->
    // authAttempt.observe(viewLifecycleOwner) { startSignInFlow() }
    // liveData references
    private val userState = SingleLiveData<User?>()

    override fun handleEvent(event: LoginEvent<LoginResult>) {
        showLoading()
        when (event) {
            is LoginEvent.OnStartGetCheckUser -> getCurrentRemoteUser()
            is LoginEvent.OnAuthButtonClick -> onAuthButtonClicked()
            is LoginEvent.OnGoogleSignInResult -> onSignInResult(event.result)
        }
    }

    private fun getCurrentRemoteUser() = launch {
        when (val user = repo.getRemoteAuthUser()) {
            is DataResult.Value -> {
                userState.value = user.value

                if (user.value != null) {
                    showSignedInState()/*
                    *
                    * executed only if user is not signed in,
                    * user argument is not null.
                    *
                    * */
                    updateRemoteUser(user.value)
                } else showSignedOutState()


                hideLoading()
            }

            is DataResult.Error -> {
                showSignedOutState()
                handleError()
            }
        }
    }

    private fun updateRemoteUser(user: User) = launch {
        when (repo.createARemoteFirebaseUser(user)) {
            is DataResult.Error ->  showSyncedToast(isSynced = false)
            is DataResult.Value -> {
                if (repo.updateLocalUser(user) is DataResult.Value) showSyncedToast(
                    isSynced = true
                ) else
                    showSyncedToast(isSynced = false)
            }
        }
    }

    private fun onAuthButtonClicked() = launch {
        if (userState.value == null) authAttempt.value =
            Unit // tell view to begin auth attempt...  userCreationToastState.value = UiResource.StringResource(R.string.sync_success)
        else signOutUser()
    }

    private fun signOutUser() = launch {
        when (repo.signOutCurrentUser()) {
            is DataResult.Value -> {
                userState.value = null
                showSignedOutState()
                hideLoading()
            }

            is DataResult.Error -> handleError()
        }
    }

    private fun onSignInResult(result: LoginResult) = launch {
        if (result.requestCode == SIGN_IN_REQUEST_CODE && result.userToken != null) {

            val createGoogleUserResult = repo.signInGoogleUser(result.userToken)

            if (createGoogleUserResult is DataResult.Value) getCurrentRemoteUser()
            else handleError(R.string.cannot_sign_in)
        } else handleError(R.string.cannot_sign_in)
    }


    private fun showSignedInState() {
        signInStatusText.value = UiResource.StringResource(R.string.signed_in_successfully)
        signInStorageText.value = UiResource.StringResource(R.string.sign_in_storage_info)
        authButtonText.value = UiResource.StringResource(R.string.sign_out_)
        signInStorageGif.value = R.drawable.anm_connect
    }

    private fun showSignedOutState() {
        signInStatusText.value = UiResource.StringResource(R.string.signed_out_title)
        signInStorageText.value = UiResource.StringResource(R.string.sign_out_storage_info)
        authButtonText.value = UiResource.StringResource(R.string.sign_in_)
        signInStorageGif.value = R.drawable.anm_usb
    }

    private fun showSyncedToast(isSynced: Boolean) {
        if (isSynced) userRemoteSyncToast.value = UiResource.StringResource(R.string.sync_success)
        else userRemoteSyncToast.value = UiResource.StringResource(R.string.error_sync)
    }

    private fun handleError(stringRes: Int = R.string.error_try_again) {
        errorState.value = UiResource.StringResource(stringRes)
    }
}
