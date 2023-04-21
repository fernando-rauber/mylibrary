package uk.fernando.uikit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyAnimatedVisibility(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        exit = fadeOut(),
        enter = fadeIn()
    ) {
        content()
    }
}