package com.hli.widgetdemo.widget.gif

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Movie
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.TlsVersion
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * GIF frame extractor that downloads GIF and extracts frames for widget animation
 */
class GifFrameExtractor(private val context: Context) {

    companion object {
        private const val TAG = "GifFrameExtractor"
        private const val FRAME_CACHE_DIR = "gif_frames"
        private const val MAX_FRAME_DIMENSION = 200
        private const val DEFAULT_MAX_FRAMES = 30
    }

    private val httpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }

    private fun createOkHttpClient(): OkHttpClient {
        // Trust all certificates (for testing only)
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Support multiple TLS versions
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .allEnabledCipherSuites()
            .build()

        val specs = listOf(spec, ConnectionSpec.CLEARTEXT)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectionSpecs(specs)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    data class ExtractionResult(
        val frameFiles: List<File>,
        val frameCount: Int,
        val originalFrameDelay: Int
    )

    /**
     * Download GIF and extract frames
     */
    suspend fun extractFrames(
        gifUrl: String,
        gifId: String,
        maxFrames: Int = DEFAULT_MAX_FRAMES,
        maxDimension: Int = MAX_FRAME_DIMENSION
    ): Result<ExtractionResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting frame extraction for: $gifUrl")

            // Check if already cached
            val cacheDir = getFrameCacheDir(gifId)
            val cachedFrames = getCachedFrames(cacheDir)
            if (cachedFrames.isNotEmpty()) {
                Log.d(TAG, "Using cached frames: ${cachedFrames.size}")
                return@withContext Result.success(
                    ExtractionResult(cachedFrames, cachedFrames.size, 33)
                )
            }

            // Download GIF
            val gifFile = downloadGif(gifUrl, gifId)
            Log.d(TAG, "GIF downloaded: ${gifFile.length()} bytes")

            // Parse and extract frames
            val movie = Movie.decodeFile(gifFile.absolutePath)
                ?: throw IllegalStateException("Failed to decode GIF")

            val duration = movie.duration()
            val width = movie.width()
            val height = movie.height()

            Log.d(TAG, "GIF info: ${width}x${height}, duration=${duration}ms")

            // Calculate frame count and interval
            val estimatedFrameCount = if (duration > 0) {
                (duration / 33).coerceIn(1, maxFrames)
            } else {
                maxFrames
            }

            val frameInterval = if (duration > 0 && estimatedFrameCount > 0) {
                duration / estimatedFrameCount
            } else {
                33
            }

            // Calculate scale for downsampling
            val scale = if (width > maxDimension || height > maxDimension) {
                minOf(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            } else {
                1f
            }

            val scaledWidth = (width * scale).toInt()
            val scaledHeight = (height * scale).toInt()

            Log.d(TAG, "Extracting $estimatedFrameCount frames at ${scaledWidth}x${scaledHeight}")

            // Create cache directory
            cacheDir.mkdirs()

            // Extract frames
            val frameFiles = mutableListOf<File>()
            var time = 0

            for (i in 0 until estimatedFrameCount) {
                movie.setTime(time)

                val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.scale(scale, scale)
                movie.draw(canvas, 0f, 0f)

                val frameFile = File(cacheDir, "frame_${i.toString().padStart(3, '0')}.png")
                FileOutputStream(frameFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, out)
                }
                bitmap.recycle()

                frameFiles.add(frameFile)
                time += frameInterval
            }

            // Clean up temp GIF file
            gifFile.delete()

            Log.d(TAG, "Extracted ${frameFiles.size} frames")

            Result.success(ExtractionResult(frameFiles, frameFiles.size, frameInterval))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract frames", e)
            Result.failure(e)
        }
    }

    private fun downloadGif(url: String, gifId: String): File {
        val tempFile = File(context.cacheDir, "temp_$gifId.gif")

        Log.d(TAG, "Downloading GIF from: $url")

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
            .header("Accept", "*/*")
            .build()

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to download GIF: ${response.code} ${response.message}")
        }

        val body = response.body ?: throw IllegalStateException("Empty response body")

        body.byteStream().use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        Log.d(TAG, "Download complete: ${tempFile.length()} bytes")
        return tempFile
    }

    private fun getFrameCacheDir(gifId: String): File {
        return File(context.filesDir, "$FRAME_CACHE_DIR/$gifId")
    }

    private fun getCachedFrames(cacheDir: File): List<File> {
        if (!cacheDir.exists()) return emptyList()

        return cacheDir.listFiles()
            ?.filter { it.extension == "png" }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    fun clearCache(gifId: String) {
        getFrameCacheDir(gifId).deleteRecursively()
    }

    fun clearAllCache() {
        File(context.filesDir, FRAME_CACHE_DIR).deleteRecursively()
    }
}
