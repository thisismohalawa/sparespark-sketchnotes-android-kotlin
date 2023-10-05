package sparespark.sketchnotes.data.model.note

data class FirebaseNote(
    val pos: Int? = null,
    val content: String? = "",
    val title: String? = "",
    val creationDate: String? = "",
    val hexCardColor: String? = "",
    val shareUId: String? = "",
    val shareUToken: String? = "",
    val owner: String? = "",
)
