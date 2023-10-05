package sparespark.sketchnotes.core.view

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.text.*
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import sparespark.sketchnotes.core.NOTE_CARD_HEX_COLOR
import sparespark.sketchnotes.core.NOTE_INPUT_MAX_LEN
import java.util.*

internal fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

internal fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

internal fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

internal fun EditText.setInputMaxLength(max: Int) {
    this.filters = arrayOf(InputFilter.LengthFilter(max))
}

internal fun View.preventDoubleClick() {
    this.isEnabled = false
    this.postDelayed({ this.isEnabled = true }, 2000)
}

internal fun View.setBackgroundColor(hexColor: String) {
    this.setBackgroundColor(Color.parseColor(hexColor))
}

internal fun CardView.setCardBackgroundColor(hexColor: String?) {
    if (hexColor != null && hexColor.startsWith('#')) setCardBackgroundColor(
        Color.parseColor(
            hexColor
        )
    )
    else setCardBackgroundColor(Color.parseColor(NOTE_CARD_HEX_COLOR))
}

internal fun MenuItem.setTitleColor(color: Int) = try {
    val hexColor = Integer.toHexString(color).toUpperCase(Locale.ROOT).substring(2)
    val html = "<font color='#$hexColor'>$title</font>"
    this.title = html.parseAsHtml()
} catch (ex: Exception) {
    ex.printStackTrace()
    Unit
}


@Suppress("DEPRECATION")
private fun String.parseAsHtml(): Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
} else {
    Html.fromHtml(this)
}

internal fun String?.isEmailAddressValid(): Boolean =
    this?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() } == true


internal fun String.isDouble(): Boolean = try {
    this.toDoubleOrNull() != null
} catch (e: Exception) {
    e.printStackTrace()
    false
}

internal fun EditText.beginActionEnabledTextViewWatcher(actionText: TextView) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onTextChanged(text: CharSequence, start: Int, count: Int, after: Int) =
            if (text.isNotEmpty() && text.length < NOTE_INPUT_MAX_LEN) actionText.enable(true)
            else actionText.enable(false)
    })
}

internal fun EditText.beginDoubleValueTextViewWatcher(textView: TextView) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(s: Editable) {
        }

        override fun onTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
            textView.text = text
        }
    })
}

internal fun View.setClickListenerWithViewDelayEnabled(action: () -> Unit) {
    setOnClickListener {
        this.isEnabled = false
        action()
        postDelayed({ isEnabled = true }, 2000)
    }
}

internal fun ImageView.loadAGif(gifRec: Int) = try {
    Glide.with(this.context).asGif().load(gifRec).into(this@loadAGif)
} catch (ex: Exception) {
    ex.printStackTrace()
}


internal fun Activity.makeToast(value: String?) {
    value?.let {
        Toast.makeText(this@makeToast, it, Toast.LENGTH_SHORT).show()
    }
}

internal fun RecyclerView.setNoteRecyclerListBehavioral() {
    this.apply {
        setHasFixedSize(true)
        val sGridLayoutManager = StaggeredGridLayoutManager(
            2, StaggeredGridLayoutManager.VERTICAL
        )
        layoutManager = sGridLayoutManager
    }
}
