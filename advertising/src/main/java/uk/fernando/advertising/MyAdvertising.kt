package uk.fernando.advertising

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

object MyAdvertising {


    fun setDeviceID(deviceID: List<String>) {
        val configs = RequestConfiguration.Builder().setTestDeviceIds(deviceID)
//        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
//        .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()

        MobileAds.setRequestConfiguration(configs)
    }

    fun initialize(context: Context) {
        MobileAds.initialize(context) { }
    }
}