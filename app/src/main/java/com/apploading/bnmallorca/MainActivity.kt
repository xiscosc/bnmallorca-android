package com.apploading.bnmallorca

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.service.MediaPlaybackService
import com.apploading.bnmallorca.views.TrackViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var mediaController: ListenableFuture<MediaController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.openLinkInBrowser(R.id.igLogo, R.string.instagram_url)
        this.openLinkInBrowser(R.id.fbLogo, R.string.facebook_url)

        val trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)

        val trackNameView: TextView = findViewById(R.id.trackName)
        val trackArtistView: TextView = findViewById(R.id.trackArtist)
        val albumArtView: ImageView = findViewById(R.id.albumArtPlaceholder)
        val playPauseView: ImageView = findViewById(R.id.playPauseButton)

        Glide.with(this)
            .load(R.drawable.album_placeholder)
            .transform(RoundedCorners(15))
            .into(albumArtView)

        trackViewModel.name.observe(this, Observer { newText ->
            trackNameView.text = newText
        })

        trackViewModel.artist.observe(this, Observer { newText ->
            trackArtistView.text = newText
        })

        trackViewModel.albumArtUrl.observe(this, Observer { newUrl ->
            if (newUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(R.drawable.album_placeholder)
                    .transform(RoundedCorners(15))
                    .into(albumArtView)
            } else {
                Glide.with(this)
                    .load(newUrl)
                    .transform(RoundedCorners(15))
                    .placeholder(R.drawable.album_placeholder)
                    .fallback(R.drawable.album_placeholder)
                    .into(albumArtView)
            }
        })

        trackViewModel.playing.observe(this, Observer { newStatus ->
            val icon = if (newStatus) R.drawable.pause_button else R.drawable.play_button
            playPauseView.setImageResource(icon)
        })

        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        mediaController = MediaController.Builder(this, sessionToken).buildAsync()

        playPauseView.setOnClickListener {
            val controller = mediaController.get()
            if (controller.isPlaying) controller.pause() else controller.play()
        }

        // Update track for the first time
        lifecycleScope.launch {
            TrackManager.updateLastTrackFromApi(this@MainActivity)
        }
    }

    private fun openLinkInBrowser(viewId: Int, urlStringId: Int) {
        val view: ImageView = findViewById(viewId)
        view.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(urlStringId))))
        }
    }
}