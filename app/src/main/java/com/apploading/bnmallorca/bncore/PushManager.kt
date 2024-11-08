package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Named

class PushManager @Inject constructor(
    @Named("pushSharedPreferences") private val pushPreferences: SharedPreferences,
    private val remoteSettingsManager: RemoteSettingsManager
) {
    private val pushField = "push_token"
    fun pushInfoIsStored() = getPushToken() !== ""

    suspend fun unregisterDevice() {
        try {
            val api = BnApi.build(this.remoteSettingsManager.getSettings())
            val oldToken = getPushToken()
            Log.d(TAG, "Unregistering device")
            if (oldToken !== "") {
                api.unregisterDevice(
                    RegisterDeviceBody(
                        token = oldToken,
                        type = "android"
                    )
                )
                Log.d(TAG, "Device unregistered")
            }
        } catch (e: Error) {
            Log.e(TAG, "API Connection error")
        }
    }

    suspend fun registerDevice(token: String?) {
        Log.d(TAG, "Registering device")
        val api = BnApi.build(this.remoteSettingsManager.getSettings())
        try {
            if (token !== null) {
                unregisterDevice()
                storePushToken(token)
                api.registerDevice(
                    RegisterDeviceBody(
                        token = token,
                        type = "android"
                    )
                )
                Log.d(TAG, "Registering device with new token")
            } else {
                Log.d(TAG, "Re-Registering device")
                // Re-registering the same token
                val oldToken = getPushToken()
                if (oldToken !== "") {
                    api.registerDevice(
                        RegisterDeviceBody(
                            token = oldToken,
                            type = "android"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "API Connection error")
        }
    }

    private fun storePushToken(token: String) =
        pushPreferences.edit().putString(pushField, token).apply()

    private fun getPushToken(): String {
        return pushPreferences.getString(pushField, "") ?: ""
    }

    companion object {
        private const val PUSH_PREFERENCES = "push_preferences"
        private const val TAG = "PushManager"

        fun getSharedPreferencesForPush(context: Context): SharedPreferences {
            return context.getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)
        }
    }
}