package sparespark.sketchnotes.data.db.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


const val CURRENT_USER_ID = 0

@Entity(
    tableName = "current_user"
)
data class RoomUser(
    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "token")
    val token: String
) {
    @PrimaryKey(autoGenerate = false)
    var id: Int = CURRENT_USER_ID
}
