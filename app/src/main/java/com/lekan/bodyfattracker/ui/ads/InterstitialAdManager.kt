package com.lekan.bodyfattracker.ui.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback // Correct import

// Test Ad Unit ID for Interstitial Ads. Replace with your actual ID for release.
const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

object InterstitialAdManager {

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoadingAd: Boolean = false
    private var isAdShowing: Boolean = false // To prevent showing ad if one is already showing

    // Optional: Keep track of the last time an ad was shown for frequency capping
    // private var lastAdShowTime: Long = 0
    // private const val AD_SHOW_INTERVAL_MS: Long = 60000 // e.g., 1 minute

    /**
     * Loads an interstitial ad.
     * Call this in advance of when you want to show the ad (e.g., when a screen is created).
     */
    fun loadAd(context: Context, adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID) {
        if (isLoadingAd || mInterstitialAd != null || isAdShowing) {
            return // Ad is already loaded, loading, or showing
        }

        // Optional: Frequency Capping Logic
        // if (System.currentTimeMillis() - lastAdShowTime < AD_SHOW_INTERVAL_MS) {
        //     Log.d("InterstitialAdManager", "Ad show interval not met.")
        //     return
        // }

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isLoadingAd = false
                    // Handle ad load failure (e.g., log the error)
                    // Log.e("InterstitialAdManager", "Ad failed to load: ${adError.message}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoadingAd = false
                    // Ad loaded successfully
                    // Log.d("InterstitialAdManager", "Ad loaded successfully.")
                }
            }
        )
    }

    /**
     * Shows the loaded interstitial ad if it's ready.
     * @param activity The Activity context needed to show the ad.
     * @param onAdDismissed Lambda to be invoked when the ad is dismissed.
     * @param onAdShowFailed Lambda to be invoked if the ad fails to show.
     * @param onAdShowed Lambda to be invoked when the ad is successfully shown.
     */
    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit,
        onAdShowFailed: (() -> Unit)? = null,
        onAdShowed: (() -> Unit)? = null
    ) {
        if (isAdShowing) {
            // Log.d("InterstitialAdManager", "An ad is already showing.")
            onAdShowFailed?.invoke() ?: onAdDismissed() // Invoke a callback if an ad is already up
            return
        }

        if (mInterstitialAd != null && !isLoadingAd) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Called when the ad is dismissed.
                    // Important: An ad can only be shown once. Nullify the ad and load the next one.
                    mInterstitialAd = null
                    isAdShowing = false
                    // lastAdShowTime = System.currentTimeMillis() // Update for frequency capping
                    loadAd(activity.applicationContext) // Preload the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    // Called when an ad fails to show.
                    mInterstitialAd = null
                    isAdShowing = false
                    // Log.e("InterstitialAdManager", "Ad failed to show: ${adError.message}")
                    onAdShowFailed?.invoke() ?: onAdDismissed() // Fallback to onAdDismissed
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when the ad is shown.
                    isAdShowing = true
                    // Log.d("InterstitialAdManager", "Ad showed successfully.")
                    onAdShowed?.invoke()
                    // Don't nullify mInterstitialAd here, it's done in onAdDismissedFullScreenContent
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            // Ad not ready or already shown/failed.
            // Log.d("InterstitialAdManager", "Ad not ready to show. isLoading: $isLoadingAd, mInterstitialAd: $mInterstitialAd")
            // Potentially try to load another one if mInterstitialAd is null and not loading
            if (mInterstitialAd == null && !isLoadingAd) {
                loadAd(activity.applicationContext)
            }
            onAdShowFailed?.invoke() ?: onAdDismissed() // Fallback to onAdDismissed
        }
    }
}
