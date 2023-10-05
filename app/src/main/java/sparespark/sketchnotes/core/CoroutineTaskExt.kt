package sparespark.sketchnotes.core

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend fun <T> launchAWithContextScope(
    taskCall: suspend () -> T,
): T = withContext(Dispatchers.IO) {
    taskCall.invoke()
}

internal suspend fun <T> awaitTaskCompletable(task: Task<T>): Unit =
    suspendCoroutine { continuation ->
        task.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(task.exception!!)
            }
        }
    }

internal suspend fun <T> awaitTaskResult(task: Task<T>): T = suspendCoroutine { continuation ->
    task.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result!!)
        } else {
            continuation.resumeWithException(task.exception!!)
        }
    }
}
//https://piotrprus.medium.com/use-kotlin-coroutines-for-firebase-real-time-database-streams-e28d6083709c
internal suspend fun DatabaseReference.awaitsSingleForSharedTeamData(email: String): DataSnapshot? =
    suspendCancellableCoroutine { continuation ->
        val query = this.orderByChild("email").equalTo(email).limitToFirst(1)
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                val exception = when (error.toException()) {
                    is FirebaseException -> error.toException()
                    else -> Exception("The Firebase call for reference $this was cancelled")
                }
                continuation.resumeWithException(exception)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    continuation.resume(snapshot.children.first())
                } catch (exception: Exception) {
                    continuation.resumeWithException(exception)
                }
            }
        }
        continuation.invokeOnCancellation { query.removeEventListener(listener) }
        query.addListenerForSingleValueEvent(listener)
    }
