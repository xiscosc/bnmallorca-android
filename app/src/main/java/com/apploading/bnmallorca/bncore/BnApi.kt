package com.apploading.bnmallorca.bncore

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

    @GET("/api/v1/schedule")
    suspend fun getSchedule(): ScheduleResponse

    @POST("/api/v1/unregister")
    suspend fun unregisterDevice(@Body data: RegisterDeviceBody)

    companion object {
        fun build(settings: BnMallorcaSettings): BnApi {
            return Retrofit.Builder()
                .baseUrl(settings.apiEndpoint)
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

data class Show(
    val name: String,
    val artist: String,
    val time: String,
    val online: Boolean,
    val podcastLink: String,
    val albumArt: List<AlbumArt>
)

data class Day(
    val numberOfTheWeek: Number,
    val shows: List<Show>
)

data class ScheduleResponse(
    val days: List<Day>
)