package uk.fernando.advertising.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(@StringRes unitId: Int, modifier: Modifier = Modifier) {
    val adView = rememberAdMobWithLifecycle(unitId)
    AndroidView(
        factory = { adView },
        modifier = modifier
    )
}

@Composable
private fun rememberAdMobWithLifecycle(@StringRes unitId: Int): AdView {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = context.getString(unitId)
        }
    }
    adView.loadAd(AdRequest.Builder().build())

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifecycle, key2 = adView) {
        val lifecycleObserver = getAdLifecycleObserver(adView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return adView
}

private fun getAdLifecycleObserver(adView: AdView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> adView.resume()
            Lifecycle.Event.ON_PAUSE -> adView.pause()
            Lifecycle.Event.ON_DESTROY -> adView.destroy()
            else -> {}
        }
    }