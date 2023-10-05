package sparespark.sketchnotes.data.model.note

import java.io.Serializable


data class Note(
    val pos: Int?,
    val content: String,
    val title: String,
    var creationDate: String,
    var hexCardColor: String,
    var sharedUId: String? = null,
    var sharedUToken: String? = null,
    var owner: String? = null,

    ) : Serializable {

    constructor(
        /*
        * Main note without any position ids
        *
        * */
        content: String,
        title: String,
        creationDate: String,
        hexCardColor: String,
        sharedUId: String?,
        sharedUToken: String?,
        owner: String?,
    ) : this(
        null, content, title, creationDate, hexCardColor, sharedUId, sharedUToken, owner
    )

    constructor(
        pos: Int?,
        content: String,
        hexCardColor: String,
    ) : this(
        pos, content, "", "", hexCardColor
    )
}
