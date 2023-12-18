package com.apploading.bnmallorca.bncore

import android.content.Context
import com.apploading.bnmallorca.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BnApi {
    @GET("/api/v1/tracklist?limit=1")
    suspend fun getLastTrack(): TrackResponse

    @POST("/api/v1/register")
    suspend fun registerDevice(@Body data: RegisterDeviceBody)

    companion object {
        fun build(context: Context): BnApi {
            return Retrofit.Builder()
                .baseUrl(context.getString(R.string.api_url))
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
    val tracks: List<Track>
)

data class RegisterDeviceBody(
    val token: String,
    val type: String
)