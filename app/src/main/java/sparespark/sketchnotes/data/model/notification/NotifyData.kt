package sparespark.sketchnotes.data.model.notification

data class NotifyData(
    var user: String? = "",
    val title: String? = "",
    val body: String? = "",
    val sent: String? = ""
) {
    constructor(sent: String?) : this(
        null, "New Memo!", "Someone shared a new memo with you.", sent
    )
}
