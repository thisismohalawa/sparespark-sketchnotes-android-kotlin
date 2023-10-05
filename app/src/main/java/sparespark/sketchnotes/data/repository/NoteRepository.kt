package sparespark.sketchnotes.data.repository

import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.data.model.note.Note


/*
*
* Frontend doesn't know or care where the data come from.
*
* */
interface NoteRepository {
    suspend fun getNotes(): DataResult<Exception, List<Note>>
    suspend fun getNoteById(noteId: String): DataResult<Exception, Note>
    suspend fun updateNote(note: Note): DataResult<Exception, Unit>
    suspend fun deleteNote(note: Note): DataResult<Exception, Unit>
}
