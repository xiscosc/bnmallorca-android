package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences

class PushManager {
    companion object {
        private const val PUSH_PREFERENCES = "push_preferences"
        private const val PUSH_FIELD = "push_token"


        suspend fun unregisterDevice(context: Context) {
            val api = BnApi.build()
            val oldToken = getPushToken(context)
            if (oldToken !== "") {
                api.unregisterDevice(
                    RegisterDeviceBody(
                        token = oldToken,
                        type = "android"
                    )
                )
            }
        }

        suspend fun registerDevice(context: Context, token: String?) {
            val api = BnApi.build()
            if (token !== null) {
                unregisterDevice(context)
                storePushToken(token, context)
                api.registerDevice(
                    RegisterDeviceBody(
                        token = token,
                        type = "android"
                    )
                )
            } else {
                // Re-registering the same token
                val oldToken = getPushToken(context)
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

        private fun storePushToken(
            token: String,
            context: Context
        ) {
            val pushPreferences = getSharedPreferencesForPush(context)
            pushPreferences.edit().putString(PUSH_FIELD, token).apply()
        }

        private fun getPushToken(context: Context): String {
            val pushPreferences = getSharedPreferencesForPush(context)
            return pushPreferences.getString(PUSH_FIELD, "") ?: ""
        }

        private fun getSharedPreferencesForPush(context: Context): SharedPreferences {
            return context.getSharedPreferences(PUSH_PREFERENCES, Context.MODE_PRIVATE)
        }
    }
}