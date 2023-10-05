package sparespark.sketchnotes.login

/*
* Login event class represent different events..
* In some cases we want to return LoginResult which is a data wrapper,
* main some data like requestCode, token.
*
* */
sealed class LoginEvent<out T> {
    object OnStartGetCheckUser : LoginEvent<Nothing>()
    object OnAuthButtonClick : LoginEvent<Nothing>()
    data class OnGoogleSignInResult<out LoginResult>(val result: LoginResult) :
        LoginEvent<LoginResult>()
}
