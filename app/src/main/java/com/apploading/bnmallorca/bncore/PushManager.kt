package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

class PushManager @Inject constructor(@Named("pushSharedPreferences") private val pushPreferences: SharedPreferences) {
    private val pushField = "push_token"

    fun pushInfoIsStored() = getPushToken() !== ""

    suspend fun unregisterDevice() {
        val api = BnApi.build()
        val oldToken = getPushToken()
        if (oldToken !== "") {
            api.unregisterDevice(
                RegisterDeviceBody(
                    token = oldToken,
                    type = "android"
                )
            )
        }
    }

    suspend fun registerDevice(token: String?) {
        val api = BnApi.build()
        if (token !== null) {
            unregisterDevice()
            storePushToken(token)
            api.registerDevice(
                RegisterDeviceBody(
                    token = token,
                    type = "android"
                )
            )
        } else {
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
    }

    private fun storePushToken(token: String) =
        pushPreferences.edit().putString(pushField, token).apply()

    private fun getPushToken(): String {
        return pushPreferences.getString(pushField, "") ?: ""
    }

    companion object {
        private const val PUSH_PREFERENCES = "push_preferences"

        fun getSharedPreferencesForPush(context: Context): SharedPreferences {
            return context.getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)
        }
    }
}