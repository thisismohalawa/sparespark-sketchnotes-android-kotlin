package sparespark.sketchnotes.core.result

import android.content.Context
import androidx.annotation.StringRes

sealed class UiResource {

    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiResource()


    fun asString(context: Context?): String? {
        return when (this) {
            is StringResource -> context?.getString(resId, *args)
        }
    }
}
