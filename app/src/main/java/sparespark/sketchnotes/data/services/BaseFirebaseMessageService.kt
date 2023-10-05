package sparespark.sketchnotes.data.services

import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import sparespark.sketchnotes.data.notification.memo.MemeNotifyImpl


class BaseFirebaseMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        val sentOwner = remoteMessage.data["sent"]

        if (currentFirebaseUser != null &&
            sentOwner == currentFirebaseUser.uid
        )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNormalNotification(
                    remoteMessage
                )

    }

    private fun sendNormalNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        MemeNotifyImpl(this).showSharedMemeNotification(
            title = title, content = body
        )
    }
}
