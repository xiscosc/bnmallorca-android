package com.apploading.bnmallorca.bncore

import ErrorNotifier
import com.apploading.bnmallorca.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RemoteSettingsManager @Inject constructor() {
    private val emptyConfig = BnMallorcaSettings("", "http://localhost",
        "", "", "", "",
        "", "", "", "", "",
        "", "", "")

    fun getSettings(): BnMallorcaSettings {
        return try {
            val configString = Firebase.remoteConfig.getString("configuration")
            Gson().fromJson(configString, BnMallorcaSettings::class.java)
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                ErrorNotifier.emitError("Failed to fetch settings")
            }
            return emptyConfig
        }
    }

    fun setupSettings() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }

}

data class BnMallorcaSettings(
    @SerializedName("streaming_url") val streamingUrl: String,
    @SerializedName("api_endpoint") val apiEndpoint: String,
    @SerializedName("services_url") val servicesUrl: String,
    @SerializedName("app_download_url") val appDownloadUrl: String,
    @SerializedName("instagram_web_url") val instagramWebUrl: String,
    @SerializedName("instagram_app_url") val instagramAppUrl: String,
    @SerializedName("facebook_web_url") val facebookWebUrl: String,
    @SerializedName("facebook_app_url") val facebookAppUrl: String,
    @SerializedName("mail") val mail: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("address_display") val addressDisplay: String?,
    @SerializedName("address_geo_link") val addressGeoLink: String?,
    @SerializedName("services_banner_text") val servicesBannerText: String?,
    @SerializedName("share_song_string") val shareSongString: String?
)