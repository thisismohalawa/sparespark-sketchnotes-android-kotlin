package sparespark.sketchnotes.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.login_view.*
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.SIGN_IN_REQUEST_CODE
import sparespark.sketchnotes.core.view.*
import sparespark.sketchnotes.data.model.LoginResult
import sparespark.sketchnotes.login.buildlogic.LoginInjector
import sparespark.sketchnotes.login.viewmodel.LoginViewModel

class LoginView : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.login_view, container, false)

    override fun onStart() {
        super.onStart()/*
        * init login injector class.
        * custom written dependency injection class, which extends viewModel.
        *
        *
        *
        * */
        viewModel = ViewModelProvider(
            owner = this,
            factory = LoginInjector(requireActivity().application).provideUserViewModelFactory()
        )[LoginViewModel::class.java]/*
        *
        * view
        * */
        viewModelObserver()
        setUpViewListeners()
    }

    private fun setUpViewListeners() {
        btn_login.setClickListenerWithViewDelayEnabled {
            viewModel.handleEvent(LoginEvent.OnAuthButtonClick)
        }
    }

    private fun viewModelObserver() {
        with(viewModel) {
            handleEvent(LoginEvent.OnStartGetCheckUser)/*
            * states
            * */
            loading.observe(viewLifecycleOwner) {
                progress_circular.visible(it)
            }
            error.observe(viewLifecycleOwner) {
                activity?.makeToast(it.asString(context))
            }
            signInStatusText.observe(viewLifecycleOwner) {
                txt_login_status.text = it.asString(context)
            }
            signInStorageText.observe(viewLifecycleOwner) {
                txt_storage_info_status.text = it.asString(context)
            }
            signInStorageGif.observe(viewLifecycleOwner) {
                img_storage_info_status.loadAGif(it)
            }
            authButtonText.observe(viewLifecycleOwner) {
                btn_login.text = it.asString(context)
            }
            userRemoteSyncToast.observe(viewLifecycleOwner) {
                activity?.makeToast(it.asString(context))
            }/*
            * Auth
            *
            * */
            authAttempt.observe(viewLifecycleOwner) { startSignInFlow() }
        }
    }

    private fun startSignInFlow() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)/*
        *
        *  +TO DO
        * Enable Google Sign in method
        *
        * Add a support email address to your project in project settings.
        * Open link https://console.firebase.google.com/
        *
        * */
        var userToken: String? = null
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                userToken = account.idToken
                viewModel.handleEvent(
                    LoginEvent.OnGoogleSignInResult(
                        LoginResult(requestCode, userToken)
                    )
                )
            }
        } catch (exception: Exception) {
            exception.message?.let {
                activity?.makeToast(
                    getString(R.string.cannot_sign_in)
                )
            }
        }
    }
}
