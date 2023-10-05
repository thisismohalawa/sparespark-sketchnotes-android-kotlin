package sparespark.sketchnotes.data.db.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "notes",
    indices = [Index("creation_date")]
)
data class RoomNote(
    @ColumnInfo(name = "pos")
    val pos: Int?,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "title")
    val title: String,

    @PrimaryKey
    @ColumnInfo(name = "creation_date")
    val creationDate: String,

    @ColumnInfo(name = "hex_card_color")
    var hexCardColor: String
)
