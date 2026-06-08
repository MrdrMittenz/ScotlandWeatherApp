package com.scotlandweather.app.data.update

import kotlinx.serialization.Serializable

@Serializable
data class VersionManifest(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val changelog: String,
    val minApiLevel: Int = 26
)

data class UpdateResult(
    val isAvailable: Boolean,
    val manifest: VersionManifest? = null,
    val error: String? = null
) {
    companion object {
        fun none() = UpdateResult(false)
        fun available(manifest: VersionManifest) = UpdateResult(true, manifest = manifest)
        fun error(msg: String) = UpdateResult(false, error = msg)
    }
}
