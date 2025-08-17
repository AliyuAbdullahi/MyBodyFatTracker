// AdViewComposable.kt (or in a common ui utils file)
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdmobBanner(modifier: Modifier = Modifier, adUnitId: String = "ca-app-pub-3940256099942544/6300978111") {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER) // Or other AdSize like AdSize.LARGE_BANNER, AdSize.FULL_BANNER
                // Or AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
                // Use test ad unit ID during development
                // Replace with your actual ad unit ID for release
                this.adUnitId = adUnitId // "ca-app-pub-3940256099942544/6300978111" is a test ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}