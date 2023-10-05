package sparespark.sketchnotes.core

import com.google.firebase.auth.FirebaseUser
import sparespark.sketchnotes.data.db.note.RoomNote
import sparespark.sketchnotes.data.db.user.RoomUser
import sparespark.sketchnotes.data.model.note.FirebaseNote
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.data.model.shared.IFirebaseUser
import sparespark.sketchnotes.data.model.shared.SharedUser
import sparespark.sketchnotes.data.model.user.User


/*
* local
*
*  */
internal val User.toRoomUser: RoomUser
    get() = RoomUser(
        this.uid,
        this.name,
        this.email,
        this.token
    )
internal val RoomUser.toUser: User
    get() = User(
        uid = this.uid,
        name = this.name,
        email = this.email,
        token = this.token
    )

internal val Note.toRoomNote: RoomNote
    get() = RoomNote(
        this.pos,
        this.content,
        this.title,
        this.creationDate,
        this.hexCardColor
    )

internal val RoomNote.toNote: Note
    get() = Note(
        this.pos,
        this.content,
        this.title,
        this.creationDate,
        this.hexCardColor
    )


internal fun List<RoomNote>.toNoteListFromRoomNote(): List<Note> =
    this.flatMap {
        listOf(it.toNote)
    }


/*
* remote
*
*
* */
internal val FirebaseUser.toUser: User
    get() = User(
        uid = this.uid,
        name = this.displayName ?: "",
        email = this.email ?: ""
    )
internal val FirebaseNote.toNote: Note
    get() = Note(
        this.content ?: "",
        this.title ?: "",
        this.creationDate ?: "",
        this.hexCardColor ?: "",
        this.shareUId ?: "",
        this.shareUToken ?: "",
        this.owner ?: ""
    )
internal val Note.toFirebaseNote: FirebaseNote
    get() = FirebaseNote(
        this.pos,
        this.content,
        this.title,
        this.creationDate,
        this.hexCardColor,
        this.sharedUId,
        this.sharedUToken,
        this.owner
    )
internal val IFirebaseUser.toSharedUser: SharedUser
    get() = SharedUser(
        uid = this.uid ?: "",
        token = this.token ?: "",
    )
