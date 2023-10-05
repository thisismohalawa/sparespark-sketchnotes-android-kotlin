package sparespark.sketchnotes.notes.notelist

import android.content.Context
import sparespark.sketchnotes.data.model.note.Note

sealed class NoteListEvent {
    object OnDestroy : NoteListEvent()
    object UpdateRemoteToken : NoteListEvent()
    data class OnNoteListViewStart(val context: Context) : NoteListEvent()
    data class OnNoteItemClick(val note: Note) : NoteListEvent()
    data class DeleteNote(val note: Note) : NoteListEvent()
    object GetFilteredNotes : NoteListEvent()
}
