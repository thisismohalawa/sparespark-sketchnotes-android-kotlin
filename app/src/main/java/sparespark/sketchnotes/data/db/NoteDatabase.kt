package sparespark.sketchnotes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sparespark.sketchnotes.data.db.note.NoteDao
import sparespark.sketchnotes.data.db.note.RoomNote
import sparespark.sketchnotes.data.db.user.UserDao
import sparespark.sketchnotes.data.db.user.RoomUser

private const val DATABASE = "sketchnotes"

@Database(
    entities = [
        RoomNote::class,
        RoomUser::class
    ], version = 4, exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var instance: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): NoteDatabase {
            return Room.databaseBuilder(context, NoteDatabase::class.java, DATABASE)
                .build()
        }
    }
}
