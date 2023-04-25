package uk.fernando.uikit.component

import android.media.MediaPlayer
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.fernando.uikit.R
import uk.fernando.uikit.event.MultipleEventsCutter
import uk.fernando.uikit.event.get
import uk.fernando.uikit.ext.clickableSingle
import uk.fernando.uikit.ext.playAudio

@Composable
fun MyImageButton(
    modifier: Modifier = Modifier,
    @DrawableRes image: Int,
    text: String? = null,
    enabled: Boolean = true,
    textColor: Color = Color.White,
    fontSize: TextUnit = 27.sp,
    soundEffect: Int? = R.raw.click,
    @DrawableRes trailingIcon: Int? = null,
    onClick: () -> Unit,
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }
    val soundClick = if (soundEffect == null) null else MediaPlayer.create(LocalContext.current, soundEffect)

    Box(modifier = modifier
        .height(IntrinsicSize.Min)
        .width(IntrinsicSize.Min)
        .clickableSingle(ripple = false) {
            soundClick?.playAudio()

            multipleEventsCutter.processEvent(onClick)
        }) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
        )

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (trailingIcon != null)
                Icon(
                    modifier = Modifier
                        .size(52.dp)
                        .padding(end = if (text != null) 10.dp else 0.dp)
                        .padding(vertical = if (text != null) 0.dp else 6.dp),
                    painter = painterResource(trailingIcon),
                    contentDescription = null,
                    tint = textColor
                )

            if (text != null)
                Text(
                    text = text,
                    color = if (enabled) textColor else Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
        }
    }
}