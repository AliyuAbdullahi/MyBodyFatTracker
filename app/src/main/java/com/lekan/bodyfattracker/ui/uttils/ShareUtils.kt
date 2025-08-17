// ShareUtils.kt
package com.lekan.bodyfattracker.ui.uttils // Or your preferred package

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.createBitmap
import com.lekan.bodyfattracker.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

object ShareUtils {

    fun captureView(view: View): Bitmap {
        // Create a bitmap with the same dimensions as the view
        val bitmap = createBitmap(view.width, view.height)
        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)
        // Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            // Draw the background on the canvas
            bgDrawable.draw(canvas)
        } else {
            // Otherwise, fill the canvas with white
            canvas.drawColor(Color.WHITE)
        }
        // Draw the view on the canvas
        view.draw(canvas)
        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "shared_image.png"): Uri? {
        val imagePath = File(context.cacheDir, "images")
        if (!imagePath.exists()) {
            imagePath.mkdirs()
        }
        val newFile = File(imagePath, fileName)
        return try {
            val fOut = FileOutputStream(newFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut)
            fOut.flush()
            fOut.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", newFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun shareImageWithText(context: Context, text: String, imageUri: Uri?) {
        val shareIntent = Intent(Intent.ACTION_SEND)

        // Always put the text
        shareIntent.putExtra(Intent.EXTRA_TEXT, text)

        if (imageUri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Use a specific image type if you know it, or */* for broader compatibility
            // when also sending text. Let's stick to a specific image type for the stream,
            // but also ensure EXTRA_TEXT is there.
            // Some apps might still only pick up text if type is text/* or image if type is image/*
            // The key is that EXTRA_TEXT is always present.
            // If text is paramount, and image secondary, some might use text/plain
            // and hope the image is picked up.
            // If image is paramount, image/png is good.
            // For a mix, "*/*" is a common suggestion, but let's test a slightly different emphasis.

            // Option A: Emphasize image, but text is still there.
            shareIntent.type = "image/png"


            // Option B: More generic when both are present (try this if Option A doesn't work well)
            // shareIntent.type = "*/*"

        } else {
            // Only text is being sent
            shareIntent.type = "text/plain"
        }

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    }
}

class CaptureController {
    var viewToCapture: View? = null // Made public for easier access in LaunchedEffect
        private set
    private val captureRequest = MutableSharedFlow<Unit>(replay = 0)
    private val bitmapResult = MutableSharedFlow<Bitmap>(replay = 0)

    fun assignView(view: View) {
        viewToCapture = view
    }

    suspend fun capture(): Bitmap {
        captureRequest.emit(Unit)
        return bitmapResult.first()
    }

    suspend fun onBitmapCaptured(bitmap: Bitmap) {
        bitmapResult.emit(bitmap)
    }

    val onCaptureRequested = captureRequest
}
