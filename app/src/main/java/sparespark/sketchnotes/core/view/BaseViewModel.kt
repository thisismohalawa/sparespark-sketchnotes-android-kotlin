package sparespark.sketchnotes.core.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import sparespark.sketchnotes.core.result.UiResource
import kotlin.coroutines.CoroutineContext


abstract class BaseViewModel<T>(
    private val uiContext: CoroutineContext
) : ViewModel(), CoroutineScope {

    /*
    * similar behavior of group of functions.
    *
    * T is going to be sealed class,
    * which represent different states depend on user propagate.
    *
    *
    *
    * */
    abstract fun handleEvent(event: T)

    // cancellation
    protected var jobTracker: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = uiContext + jobTracker

    protected val errorState = MutableLiveData<UiResource>()
    val error: LiveData<UiResource> get() = errorState

    private val loadingState = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = loadingState

    protected fun showLoading() {
        loadingState.value = true
    }

    protected fun hideLoading() {
        loadingState.value = false
    }
}
