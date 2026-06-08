package com.scotlandweather.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object UpdateChecker {
    private const val MANIFEST_URL = "https://github.com/MrdrMittenz/ScotlandWeatherApp/releases/latest/download/version.json"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun check(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(MANIFEST_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext UpdateResult.error("HTTP ${response.code}")

            val body = response.body?.string() ?: return@withContext UpdateResult.error("Empty response")
            val manifest = json.decodeFromString<VersionManifest>(body)

            val currentCode = try {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
            } catch (_: Exception) { 1 }

            if (manifest.versionCode > currentCode) {
                UpdateResult.available(manifest)
            } else {
                UpdateResult.none()
            }
        } catch (e: Exception) {
            UpdateResult.error(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadAndInstall(context: Context, manifest: VersionManifest): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(manifest.downloadUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext "Download failed: HTTP ${response.code}"

            val apkFile = File(context.cacheDir, "update-${manifest.versionName}.apk")
            response.body?.byteStream()?.use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            null
        } catch (e: Exception) {
            e.message ?: "Install failed"
        }
    }
}
