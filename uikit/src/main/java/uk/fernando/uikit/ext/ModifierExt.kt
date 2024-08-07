package uk.fernando.uikit.ext

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import uk.fernando.uikit.R
import uk.fernando.uikit.event.MultipleEventsCutter
import uk.fernando.uikit.event.get

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.clickableSingle(
    enabled: Boolean = true,
    ripple: Boolean = true,
    onClickLabel: String? = null,
    soundEffectEnabled: Boolean = false,
    soundEffect: Int = R.raw.click,
    role: Role? = null,
    onClick: () -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }
    val soundClick = if (!soundEffectEnabled) null else MediaPlayer.create(LocalContext.current, soundEffect)

    Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            soundClick?.playAudio()
            multipleEventsCutter.processEvent(onClick)
        },
        role = role,
        indication = if (ripple) LocalIndication.current else null,
        interactionSource = remember { MutableInteractionSource() }
    )
}