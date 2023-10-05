package sparespark.sketchnotes.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sparespark.sketchnotes.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

internal fun getCalendarDateTime(): String {
    val cal = Calendar.getInstance(TimeZone.getDefault())
    val sdf = SimpleDateFormat(
        "dd MMM yyyy HH:mm:ss Z", Locale.US
    )
    sdf.timeZone = cal.timeZone
    return sdf.format(cal.time)
}

internal fun handlerPostDelayed(millisValue: Long, action: (() -> Unit)? = null) {
    Handler(Looper.getMainLooper()).postDelayed({
        action?.let { it() }
    }, millisValue)
}

internal fun String.toDateFormat(): String = if (this.length > 11) {
    this.substring(0, 11)
} else this


internal fun Context.actionCopyText(content: String) = try {
    if (content.isNotBlank()) {
        val clipboardManager =
            this.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(
            ClipData.newPlainText("", content.ifBlank { "" })
        )
        Toast.makeText(this, this.getString(R.string.copied), Toast.LENGTH_SHORT).show()
    } else Unit
} catch (e: Exception) {
    e.printStackTrace()
}

internal fun Context.actionShareText(content: String) = try {
    if (content.isNotBlank()) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sketchnotes")
        intent.putExtra(Intent.EXTRA_TEXT, content.ifBlank { NOTE_DEF_INPUT })
        this.startActivity(Intent.createChooser(intent, this.getString(R.string.share_content)))
    } else Unit
} catch (e: Exception) {
    e.printStackTrace()
}

internal fun TextView.setUpUrlText(content: String) = try {
    val spanString = SpannableString(content)
    spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
    spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
    spanString.setSpan(StyleSpan(Typeface.ITALIC), 0, spanString.length, 0)
    this.setText(spanString)
} catch (ex: Exception) {
    ex.printStackTrace()
}

internal fun Context.actionOpenUrl(url: String) = try {
    if (URLUtil.isValidUrl(url)) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        this.startActivity(browserIntent)
    } else
        Toast.makeText(this, "Invalid URL.", Toast.LENGTH_SHORT).show()

} catch (ex: Exception) {
    Toast.makeText(this, "Unable to launch Browser.", Toast.LENGTH_SHORT).show()
    ex.printStackTrace()
}

internal fun Context.actionOpenWhatsApp() = try {
    val intent = Intent(
        Intent.ACTION_VIEW, Uri.parse(
            java.lang.String.format(
                "https://api.whatsapp.com/send?phone=%s&text=%s", ADMIN_NUMBER, DEF_MESS
            )
        )
    )
    startActivity(intent)
} catch (e: Exception) {
    Toast.makeText(this, "Unable to launch WhatsApp.", Toast.LENGTH_SHORT).show()
    e.printStackTrace()
}

internal fun Context.actionDisplayConfirmationDialog(
    msgResId: Int, actionResId: Int?,
    action: (() -> Unit)?
) {
    val actionTitle = actionResId ?: R.string.go_on
    AlertDialog.Builder(this).setMessage(getString(msgResId)).setPositiveButton(
        getString(actionTitle)
    ) { _, _ ->
        action?.let { it() }
    }.show()
}
