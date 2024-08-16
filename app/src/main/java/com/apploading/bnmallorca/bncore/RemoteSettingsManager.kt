package com.apploading.bnmallorca.bncore

import com.apploading.bnmallorca.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class RemoteSettingsManager {
    companion object {
        fun getSettings(): BnMallorcaSettings {
            val configString = Firebase.remoteConfig.getString("configuration")
            return Gson().fromJson(configString, BnMallorcaSettings::class.java)
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
}

data class BnMallorcaSettings(
    @SerializedName("streaming_url") val streamingUrl: String,
    @SerializedName("api_endpoint") val apiEndpoint: String,
    @SerializedName("services_url") val servicesUrl: String,
    @SerializedName("app_download_url") val appDownloadUrl: String,
    @SerializedName("instagram_web_url") val instagramWebUrl: String,
    @SerializedName("instagram_app_url") val instagramAppUrl: String,
    @SerializedName("facebook_web_url") val facebookWebUrl: String,
    @SerializedName("facebook_app_url") val facebookAppUrl: String
)