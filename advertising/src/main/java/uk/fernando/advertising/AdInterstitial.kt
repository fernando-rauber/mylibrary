package uk.fernando.advertising

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.fernando.advertising.enum.AdState

class AdInterstitial(private val activity: Activity,private val unitID: String) {
    private var mInterstitialAd: InterstitialAd? = null
    private val adState = MutableStateFlow(AdState.LOADING)

    init {
        initInterstitialAd()
    }

    private fun initInterstitialAd(){
        InterstitialAd.load(activity, unitID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    adState.value = AdState.FAIL
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    adState.value = AdState.LOADED
                    mInterstitialAd = interstitialAd

                    setFullScreenContentCallback()
                }
            })
    }

    private fun setFullScreenContentCallback() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                adState.value = AdState.DISMISSED
                Log.d(TAG, "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                adState.value = AdState.OPENED
                Log.d(TAG, "Ad showed fullscreen content.")
                mInterstitialAd = null
            }
        }
    }

    fun showAdvert() : Flow<AdState> {
        mInterstitialAd?.show(activity)

        return adState
    }

    companion object{
        private const val TAG = "AdInterstitial"
    }
}