package sparespark.sketchnotes.notes.notedetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.secret.cardHexColors
import sparespark.sketchnotes.core.view.setCardBackgroundColor

class ColorListAdapter(
    private val itemList: List<String> = cardHexColors(),
    private val listener: (String) -> Unit,
) : RecyclerView.Adapter<ColorListAdapter.ColorViewHolder>() {

    class ColorViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var card: CardView = root.findViewById(R.id.card_view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val item = itemList[position]
        holder.card.setCardBackgroundColor(item)
        holder.itemView.setOnClickListener {
            listener(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        )
    }

    override fun getItemCount(): Int = itemList.size

}
