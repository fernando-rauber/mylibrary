package uk.fernando.uikit.component

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.fernando.uikit.event.MultipleEventsCutter
import uk.fernando.uikit.event.get

val grey = Color(0xFF9F9F9F)

@Composable
fun MyButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    fontSize: TextUnit = 17.sp,
    isLoading: Boolean = false,
    clickEffect: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    textModifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(4.dp, 0.dp),
    onClick: () -> Unit,
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }
    val context = LocalContext.current

    Button(
        border = borderStroke,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color, disabledContentColor = grey),
        elevation = elevation,
        shape = shape,
        contentPadding = contentPadding,
        onClick = {
            if (!isLoading) {
                if (clickEffect)
                    (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                        .playSoundEffect(AudioManager.FX_KEY_CLICK)

                multipleEventsCutter.processEvent(onClick)
            }
        }
    ) {
        if (isLoading)
            CircularProgressIndicator(
                strokeWidth = 3.dp,
                color = Color.White,
                modifier = Modifier.size(30.dp)
            )
        else
            Text(
                modifier = textModifier,
                text = text,
                textAlign = TextAlign.Center,
                color = if (enabled) textColor else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
    }
}