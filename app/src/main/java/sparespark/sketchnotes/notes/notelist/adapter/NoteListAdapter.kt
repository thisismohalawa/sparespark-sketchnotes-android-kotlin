package sparespark.sketchnotes.notes.notelist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_info.view.*
import kotlinx.android.synthetic.main.item_note.view.*
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.*
import sparespark.sketchnotes.core.view.setCardBackgroundColor
import sparespark.sketchnotes.core.view.setTitleColor
import sparespark.sketchnotes.core.view.visible
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.notes.notelist.NoteListEvent


private const val ITEM_INFO_STATE = 0
private const val ITEM_NOTE_STATE = 1

@SuppressLint("SetTextI18n")
class NoteListAdapter(
    private val displayInfoItems: Boolean = false,
    val event: MutableLiveData<NoteListEvent> = MutableLiveData(),
) : ListAdapter<Note, RecyclerView.ViewHolder>(NoteDiffUtilCallback()) {

    class NoteViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var content: TextView = root.lbl_content
        var owner: TextView = root.lbl_owner
        var date: TextView = root.lbl_date
        var cardview: CardView = root.content_card_view
        var iconImg: ImageView = root.img_option_menu
    }

    class InfoViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var icon: ImageView = root.icon
        var cardView: CardView = root.info_card_view
        val infoLayout: LinearLayout = root.info_layout
        var infoText: TextView = root.info_text
        var infoImg: ImageView = root.info_img
    }

    private fun itemViewTypeProvider(position: Int): Int =
        if (position == ADD_NEW_NOTE_POS_ID || position == SIGN_POS_ID) ITEM_INFO_STATE
        else ITEM_NOTE_STATE

    private fun isItemViewInfoType(position: Int): Boolean =
        position == ADD_NEW_NOTE_POS_ID || position == SIGN_POS_ID

    override fun getItemViewType(position: Int): Int =
        itemViewTypeProvider(position)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (displayInfoItems && viewType == ITEM_INFO_STATE) return InfoViewHolder(
            inflater.inflate(R.layout.item_info, parent, false)
        )
        return NoteViewHolder(inflater.inflate(R.layout.item_note, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(position)

        if (displayInfoItems && isItemViewInfoType(position)) (holder as InfoViewHolder).setUpInfoViewItemHolder(
            note = note, position = position
        )
        else (holder as NoteViewHolder).setUpNoteViewItemHolder(
            note = note
        )
    }

    private fun InfoViewHolder.setUpInfoViewItemHolder(note: Note, position: Int) {
        when (position) {
            ADD_NEW_NOTE_POS_ID -> icon.visible(true)
            SIGN_POS_ID -> {
                infoLayout.visible(true)
                infoImg.setImageResource(R.drawable.ic_power)
            }
        }

        infoText.text = note.content
        cardView.setCardBackgroundColor(note.hexCardColor)
        itemView.setOnClickListener {
            event.value = NoteListEvent.OnNoteItemClick(
                note = note
            )
        }
    }


    private fun NoteViewHolder.setUpNoteViewItemHolder(note: Note) = try {

        content.text = note.content
        date.text = note.creationDate.toDateFormat()
        owner.text = getSharedInfoTitle(note.owner, this@setUpNoteViewItemHolder.itemView.context)
        cardview.setCardBackgroundColor(note.hexCardColor)

        if (URLUtil.isValidUrl(note.content)) content.setUpUrlText(note.content)

        itemView.setOnClickListener {
            event.value = NoteListEvent.OnNoteItemClick(
                note = note
            )
        }
        iconImg.setOnClickListener {
            val popupMenu = PopupMenu(
                itemView.context, iconImg
            )
            popupMenu.apply {
                inflate(R.menu.menu_note)
                menu.findItem(R.id.delete_menu).setTitleColor(Color.RED)
                itemView.context?.let {
                    setOnMenuItemClickListener { item ->
                        when (item?.itemId) {
                            R.id.view_menu -> event.value =
                                NoteListEvent.OnNoteItemClick(note = note)
                            R.id.copy_menu -> it.actionCopyText(note.content)
                            R.id.share_text_menu -> it.actionShareText(note.content)
                            R.id.open_link_menu -> it.actionOpenUrl(note.content)
                            R.id.delete_menu -> event.value = NoteListEvent.DeleteNote(note = note)
                        }
                        false
                    }
                }
                show()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun getSharedInfoTitle(
        owner: String?, context: Context
    ): String = if (owner.isNullOrBlank()) context.getString(R.string.only_you)
    else context.getString(R.string.shared_by) + "\n" + owner

}
