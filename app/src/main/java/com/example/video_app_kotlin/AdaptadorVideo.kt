package com.example.video_app_kotlin

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class AdaptadorVideo (

    private var context: Context,
    private var videosArrayList: ArrayList<ModeloVideo>?
) : RecyclerView.Adapter<AdaptadorVideo.HolderVideo>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderVideo {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return HolderVideo(view)
    }

    override fun getItemCount(): Int {
        return videosArrayList!!.size
    }

    override fun onBindViewHolder(holder: HolderVideo, position: Int) {
        val modeloVideo = videosArrayList!![position]
        val id : String ?= modeloVideo.id
        val titulo : String ?= modeloVideo.titulo
        val autor : String ?= modeloVideo.autor
        val videoUri : String ?= modeloVideo.videoUri
        val tiempo : String ?= modeloVideo.tiempo

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = tiempo!!.toLong()

        val fecha = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString()

        holder.tvTitulo.text = titulo
        holder.tvAutor.text = autor
        holder.tvTiempo.text = fecha

        setVideo(modeloVideo, holder)

    }


    private fun setVideo (modeloVideo: ModeloVideo, holder: AdaptadorVideo.HolderVideo){
        holder.progressBar.visibility = View.VISIBLE

        val videoUrl : String ?= modeloVideo.videoUri

        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)

        val videoUri = Uri.parse(videoUrl)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.requestFocus()

        holder.videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
        }

        holder.videoView.setOnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    holder.progressBar.visibility = View.INVISIBLE
                    return@setOnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    return@setOnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_END-> {
                    return@setOnInfoListener true
                }

            }
            false
        }

        holder.videoView.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.start()
        }
    }


    class HolderVideo (itemView : View) : RecyclerView.ViewHolder(itemView) {
        var videoView : VideoView = itemView.findViewById(R.id.videoView)
        var tvTitulo : TextView = itemView.findViewById(R.id.tv_TituloVideo)
        var tvAutor : TextView = itemView.findViewById(R.id.tv_AutorVideo)
        var tvTiempo : TextView = itemView.findViewById(R.id.tv_FechaSubida)
        var progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
    }

}