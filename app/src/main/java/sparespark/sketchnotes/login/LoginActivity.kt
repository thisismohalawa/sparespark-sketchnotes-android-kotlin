package sparespark.sketchnotes.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.view.attachFragment

private const val LOGIN_VIEW = "LOGIN_VIEW"

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val view = supportFragmentManager.findFragmentByTag(LOGIN_VIEW) as LoginView? ?: LoginView()
        attachFragment(supportFragmentManager, R.id.root_activity_login, view, LOGIN_VIEW)
    }
}
