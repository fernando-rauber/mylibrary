package uk.fernando.uikit.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import uk.fernando.uikit.event.MultipleEventsCutter
import uk.fernando.uikit.event.get

@Composable
fun MyIconButton(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }

    IconButton(onClick = { multipleEventsCutter.processEvent(onClick) }, modifier = modifier) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint
        )
    }
}