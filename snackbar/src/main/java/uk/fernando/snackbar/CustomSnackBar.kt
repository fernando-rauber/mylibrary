package uk.fernando.snackbar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.CustomSnackBar(
    modifier: Modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(16.dp),
    snackBarSealed: SnackBarSealed?
) {
    val lifecycleScope = rememberCoroutineScope()
    val snackBarController = remember { SnackBarController(lifecycleScope) }
    val scaffoldState = remember { SnackbarHostState() }

    when (snackBarSealed) {
        is SnackBarSealed.Success -> snackBarController.showSnackBar(scaffoldState = scaffoldState, longDuration = snackBarSealed.isLongDuration)
        is SnackBarSealed.Error -> snackBarController.showSnackBar(scaffoldState = scaffoldState, longDuration = snackBarSealed.isLongDuration)
        else -> {}
    }

    DefaultSnackBar(
        snackBarHostState = scaffoldState,
        snackBarSealed = snackBarSealed,
        modifier = modifier
    )
}