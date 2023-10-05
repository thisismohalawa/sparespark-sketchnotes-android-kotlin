package sparespark.sketchnotes.data.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT EXISTS(SELECT * FROM current_user)")
    suspend fun isSignedIn(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: RoomUser): Long

    @Query("select * from current_user where id = $CURRENT_USER_ID")
    suspend fun getUser(): RoomUser

    @Query("select email from current_user where id = $CURRENT_USER_ID")
    suspend fun getEmailAddress(): String

    @Query("select name from current_user where id = $CURRENT_USER_ID")
    suspend fun getUserName(): String

    @Query("select uid from current_user where id = $CURRENT_USER_ID")
    suspend fun getUserId(): String

    @Query("DELETE FROM current_user")
    suspend fun clearUser()

}
