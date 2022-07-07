package uk.fernando.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * If a snackBar is visible and the user triggers a second snackBar to show, it will remove
 * the first one and show the second. Likewise with a third, fourth, ect...
 *
 * If a mechanism like this is not used, snackBar get added to the Scaffolds "queue", and will
 * show one after another.
 *
 */
class SnackBarController constructor(private val scope: CoroutineScope) {

    private var snackBarJob: Job? = null

    init {
        cancelActiveJob()
    }

    fun getScope() = scope

    fun showSnackBar(
        scaffoldState: SnackbarHostState,
        message: String = "",
        longDuration: Boolean
    ) {
        if (snackBarJob == null) {
            snackBarJob = scope.launch {
                scaffoldState.showSnackbar(
                    message = message,
                    duration = if (longDuration) SnackbarDuration.Long else SnackbarDuration.Short
                )
                cancelActiveJob()
            }
        } else {
            cancelActiveJob()
            snackBarJob = scope.launch {
                scaffoldState.showSnackbar(
                    message = message,
                    duration = if (longDuration) SnackbarDuration.Long else SnackbarDuration.Short
                )
                cancelActiveJob()
            }
        }
    }

    private fun cancelActiveJob() {
        snackBarJob?.let { job ->
            job.cancel()
            snackBarJob = Job()
        }
    }
}
