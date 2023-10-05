package sparespark.sketchnotes.data.implementation

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import sparespark.sketchnotes.core.DATABASE_REF_NAME
import sparespark.sketchnotes.core.DATABASE_URL
import sparespark.sketchnotes.core.NOTES_REF_NAME
import sparespark.sketchnotes.core.awaitTaskCompletable
import sparespark.sketchnotes.core.awaitTaskResult
import sparespark.sketchnotes.core.launchAWithContextScope
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.core.toFirebaseNote
import sparespark.sketchnotes.core.toNote
import sparespark.sketchnotes.core.toNoteListFromRoomNote
import sparespark.sketchnotes.core.toRoomNote
import sparespark.sketchnotes.data.db.note.NoteDao
import sparespark.sketchnotes.data.model.note.FirebaseNote
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.data.remote.firebase.BaseRemoteAuthProvider
import sparespark.sketchnotes.data.repository.NoteRepository

class NoteRepositoryImpl(
    private val dao: NoteDao,
) : BaseRemoteAuthProvider(), NoteRepository {

    override val databaseReference: DatabaseReference?
        get() = if (isSignedAuthUserActive) FirebaseDatabase.getInstance(DATABASE_URL)
            .getReference(DATABASE_REF_NAME).child(NOTES_REF_NAME) else null

    override val signedAuthUserId: String?
        get() = if (isSignedAuthUserActive) signedAuthUser?.uid.toString()
        else null

    /*
    *
    *
    *
    *
    * */
    override suspend fun getNotes(): DataResult<Exception, List<Note>> =
        if (isSignedAuthUserActive) getRemoteNotes() else getLocalNotes()

    override suspend fun getNoteById(noteId: String): DataResult<Exception, Note> =
        if (isSignedAuthUserActive) getRemoteNoteById(noteId) else getLocalNote(noteId)

    override suspend fun updateNote(note: Note): DataResult<Exception, Unit> =
        if (isSignedAuthUserActive) updateRemoteNote(note) else updateLocalNote(note)

    override suspend fun deleteNote(note: Note): DataResult<Exception, Unit> =
        if (isSignedAuthUserActive) deleteRemoteNote(note = note) else deleteLocalNote(
            noteId = note.creationDate
        )


    /*
    * Result
    *
    * */
    private suspend fun resultToNoteList(result: DataSnapshot?): DataResult<Exception, List<Note>> =
        launchAWithContextScope {
            DataResult.build {
                if (result == null) return@build emptyList()
                val noteList = mutableListOf<Note>()
                result.children.forEach { data ->
                    data.getValue(FirebaseNote::class.java)?.let { noteList.add(it.toNote) }
                }
                return@build noteList
            }
        }


    /*
    *  Remote Impl.
    *
    *
    *
    *
    * */
    private suspend fun getRemoteNotes(): DataResult<Exception, List<Note>> =
        DataResult.build {
            val task = awaitTaskResult(databaseReference!!.child(signedAuthUserId!!).get())
            val result = resultToNoteList(task)
            if (result is DataResult.Value) result.value
            else emptyList()
        }

    private suspend fun getRemoteNoteById(createDate: String): DataResult<Exception, Note> =
        DataResult.build {
            val task = awaitTaskResult(
                databaseReference!!.child(signedAuthUserId!!).child(createDate).get()
            )
            task.getValue(FirebaseNote::class.java)?.toNote ?: throw Exception()
        }

    private suspend fun updateRemoteNote(note: Note): DataResult<Exception, Unit> =
        DataResult.build {
            /*
            *
            * update current user note.
            *
            * */
            awaitTaskCompletable(
                databaseReference!!.child(signedAuthUserId!!).child(note.creationDate)
                    .setValue(note.toFirebaseNote)
            )/*
            * note being shared to another user too,
            * note shared user id is exist.
            *
            * */
            if (note.sharedUId?.isNotBlank() == true && note.sharedUId != signedAuthUserId) awaitTaskCompletable(
                databaseReference!!.child(note.sharedUId!!).child(note.creationDate)
                    .setValue(note.toFirebaseNote)
            )
        }

    private suspend fun deleteRemoteNote(note: Note): DataResult<Exception, Unit> =
        DataResult.build {

            awaitTaskCompletable(
                databaseReference!!.child(signedAuthUserId!!).child(note.creationDate).removeValue()
            )/*
            * delete shared memo.
            *
            * */
            if (note.sharedUId?.isNotBlank() == true && note.sharedUId != signedAuthUserId) awaitTaskCompletable(
                databaseReference!!.child(note.sharedUId!!).child(note.creationDate).removeValue()
            )
        }

    /*
    * Local Impl.
    *
    *
    *
    *  */
    private suspend fun getLocalNotes(): DataResult<Exception, List<Note>> = DataResult.build {
        dao.getNotes().toNoteListFromRoomNote()
    }

    private suspend fun getLocalNote(id: String): DataResult<Exception, Note> = DataResult.build {
        dao.getNoteById(id).toNote
    }

    private suspend fun updateLocalNote(note: Note): DataResult<Exception, Unit> =
        DataResult.build {
            dao.insertOrUpdateNote(note.toRoomNote)
            Unit
        }

    private suspend fun deleteLocalNote(noteId: String): DataResult<Exception, Unit> =
        DataResult.build {
            dao.deleteNote(noteId)
            Unit
        }
}
