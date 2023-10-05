package sparespark.sketchnotes.data.repository

import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.data.model.user.User

interface LoginRepository {
    suspend fun getRemoteAuthUser(): DataResult<Exception, User?>
    suspend fun createARemoteFirebaseUser(user: User): DataResult<Exception, Unit>
    suspend fun signOutCurrentUser(): DataResult<Exception, Unit>
    suspend fun signInGoogleUser(idToken: String): DataResult<Exception, Unit>
    suspend fun updateLocalUser(user: User):DataResult<Exception,Unit>
}
