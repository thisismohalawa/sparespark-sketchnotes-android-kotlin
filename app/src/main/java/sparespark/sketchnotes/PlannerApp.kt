package sparespark.sketchnotes

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen

class PlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this@PlannerApp)
        AndroidThreeTen.init(this@PlannerApp)
    }
}
