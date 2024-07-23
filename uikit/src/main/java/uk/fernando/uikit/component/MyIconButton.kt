package uk.fernando.uikit.component

import android.media.MediaPlayer
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import uk.fernando.uikit.R
import uk.fernando.uikit.event.MultipleEventsCutter
import uk.fernando.uikit.event.get
import uk.fernando.uikit.ext.playAudio

@Composable
fun MyIconButton(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    soundEffectEnabled: Boolean = false,
    soundEffect: Int = R.raw.click,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }
    val soundClick = if (!soundEffectEnabled) null else MediaPlayer.create(LocalContext.current, soundEffect)

    IconButton(
        modifier = modifier,
        onClick = {
            soundClick?.playAudio()
            multipleEventsCutter.processEvent(onClick)
        }
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint
        )
    }
}