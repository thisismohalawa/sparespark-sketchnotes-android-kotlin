package sparespark.sketchnotes.data.db.note

import androidx.room.*
import sparespark.sketchnotes.data.db.note.RoomNote


@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY creation_date")
    suspend fun getNotes(): List<RoomNote>

    @Query("SELECT * FROM notes WHERE creation_date = :creationDate")
    suspend fun getNoteById(creationDate: String): RoomNote

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: RoomNote): Long

    @Query("DELETE FROM notes WHERE creation_date = :creationDate")
    suspend fun deleteNote(creationDate: String)
}
