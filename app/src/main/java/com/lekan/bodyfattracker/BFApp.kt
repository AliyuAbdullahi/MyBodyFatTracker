package com.lekan.bodyfattracker

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BFApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}