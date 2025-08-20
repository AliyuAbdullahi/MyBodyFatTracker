package com.lekan.bodyfattracker.ui.education

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.lekan.bodyfattracker.R

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YoutubePlayerScreen(
    videoId: String,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create and configure the YouTubePlayerView
    val youtubePlayerView = remember {
        YouTubePlayerView(context).apply {
//            id = com.pierfrancescosoffritti.androidyoutubeplayer.core.R.id.youtube_player_view // Important for state saving
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            enableAutomaticInitialization = false // We'll initialize manually
        }
    }

    // Add the view to the lifecycle
    DisposableEffect(lifecycleOwner, youtubePlayerView) {
        lifecycleOwner.lifecycle.addObserver(youtubePlayerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youtubePlayerView)
            // youtubePlayerView.release() // Important: Release the player
        }
    }

    // Back handler to navigate up
    BackHandler(onBack = onNavigateUp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.video_player)) }, // Consider passing video title here
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        AndroidView(
            factory = {
                // Initialize the player here
                // This ensures initialization happens after the view is part of the composition
                // and avoids issues if remember {} block is re-executed without the view being ready.
                val iFramePlayerOptions = IFramePlayerOptions.Builder()
                    .controls(1) // Show controls
                    .autoplay(1) // Autoplay
                    .build()
                youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }, true, iFramePlayerOptions) // true for handleFullScreen

                youtubePlayerView
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRelease = { playerView ->
                playerView.release() // Release when the composable is disposed
            }
        )
    }
}
