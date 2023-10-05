package sparespark.sketchnotes.data.implementation

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.sketchnotes.core.DATABASE_REF_NAME
import sparespark.sketchnotes.core.USERS_REF_NAME
import sparespark.sketchnotes.core.awaitTaskCompletable
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.core.toRoomUser
import sparespark.sketchnotes.core.toUser
import sparespark.sketchnotes.data.db.user.UserDao
import sparespark.sketchnotes.data.model.user.User
import sparespark.sketchnotes.data.repository.LoginRepository

class LoginRepositoryImpl(
    private val userDao: UserDao, private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : LoginRepository {

    private suspend fun isSignedIn(): DataResult<Exception, Boolean> = DataResult.build {
        userDao.isSignedIn()
    }

    override suspend fun getRemoteAuthUser(): DataResult<Exception, User?> = DataResult.build {
        auth.currentUser?.toUser
    }

    override suspend fun createARemoteFirebaseUser(user: User): DataResult<Exception, Unit> =
        DataResult.build {
            /*
            * Update only first time if user isn't signedIn,
            * user argument must be not null.
            *
            *
            * */

            val signResult = when (val result = isSignedIn()) {
                is DataResult.Value -> result.value
                else -> false
            }


            if (!signResult) {

                val databaseReference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference(DATABASE_REF_NAME)
                        .child(USERS_REF_NAME).child(user.uid)

                awaitTaskCompletable(
                    databaseReference.setValue(user)
                )
            }
        }

    override suspend fun signOutCurrentUser(): DataResult<Exception, Unit> = DataResult.build {
        auth.signOut()
        userDao.clearUser()
    }

    override suspend fun signInGoogleUser(idToken: String): DataResult<Exception, Unit> =
        DataResult.build {
            /*
            * request credential from google, give it to firebase auth.
            * */
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            awaitTaskCompletable(auth.signInWithCredential(credential))
        }

    override suspend fun updateLocalUser(user: User): DataResult<Exception, Unit> =
        DataResult.build {
            userDao.upsert(user.toRoomUser)
        }
}
