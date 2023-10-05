package sparespark.sketchnotes.data.model.user

data class User(
    val uid: String,
    val name: String = "",
    val email: String = "",
    var token: String = ""
)