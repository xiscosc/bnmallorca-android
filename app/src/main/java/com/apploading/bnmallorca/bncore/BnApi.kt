package com.apploading.bnmallorca.bncore

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BnApi {
    @GET("/api/v1/tracklist?limit=1")
    suspend fun getLastTrack(): TrackResponse

    @GET("/api/v1/tracklist")
    suspend fun getLastTracks(
        @Query("limit") limit: Int = 25,
        @Query("filterAds") filterAds: Int = 1,
        @Query("lastTrack") lastTrack: Number? = null
    ): TrackResponse

    @POST("/api/v1/register")
    suspend fun registerDevice(@Body data: RegisterDeviceBody)

    @POST("/api/v1/unregister")
    suspend fun unregisterDevice(@Body data: RegisterDeviceBody)

    companion object {
        fun build(): BnApi {
            val apiUrl = Firebase.remoteConfig.getString("api_url")
            return Retrofit.Builder()
                .baseUrl(apiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BnApi::class.java)
        }
    }

}

data class AlbumArt(
    val size: String,
    val downloadUrl: String
)

data class Track(
    val id: String,
    val name: String,
    val artist: String,
    val timestamp: Number,
    val albumArt: List<AlbumArt>
)

data class TrackResponse(
    val count: Number,
    val lastTrack: Number?,
    val tracks: List<Track>
)

data class RegisterDeviceBody(
    val token: String,
    val type: String
)