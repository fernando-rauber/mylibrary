package uk.fernando.util.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import uk.fernando.util.event.MultipleEventsCutter
import uk.fernando.util.event.get

@Composable
fun MyIconButton(@DrawableRes icon: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }

    IconButton(onClick = { multipleEventsCutter.processEvent(onClick) }, modifier = modifier) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.White
        )
    }
}