package uk.fernando.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val green = Color(0xFF5eb14f)
val red = Color(0xFFd94443)

@Composable
fun DefaultSnackBar(
    snackBarHostState: SnackbarHostState,
    snackBarSealed: SnackBarSealed?,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackBarHostState,
        snackbar = {
            when (snackBarSealed) {
                is SnackBarSealed.Success -> CreateSnackBar(green, if (snackBarSealed.messageID != null) stringResource(id = snackBarSealed.messageID) else snackBarSealed.messageText ?: "")
                is SnackBarSealed.Error -> CreateSnackBar(red, if (snackBarSealed.messageID != null) stringResource(id = snackBarSealed.messageID) else snackBarSealed.messageText ?: "")
                else -> {}
            }
        },
        modifier = modifier
    )
}

@Composable
private fun CreateSnackBar(backgroundColor: Color, message: String) {
    Snackbar(containerColor = backgroundColor) {

        Box(modifier = Modifier.fillMaxWidth()) {

            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

