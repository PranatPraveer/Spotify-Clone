package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.ExoPlayer.isPlaying
import com.example.spotifyclone.ExoPlayer.toSong
import com.example.spotifyclone.R
import com.example.spotifyclone.databinding.FragmentSongBinding
import com.example.spotifyclone.models.Song
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.ui.viewmodels.SongViewModel
import com.example.spotifyclone.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment:Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide:RequestManager

    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song?=null

    private var shouldUpdateSeekbar=true

    private var playbackState:PlaybackStateCompat?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding=FragmentSongBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel= ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()

        binding.ivPlayPauseDetail.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(p2){
                    setCurPlayerTimeToTextView(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar=false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                binding.seekBar.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar=true
                }
            }

        })
        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        binding.ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    private fun updateTitleAndSongImage(song: Song){
        val title = "${song.title} - ${song.subtitle}"
        binding.tvSongName.text=title
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let { result->
                when(result.status){
                    Status.SUCCESS ->{
                        result.data?.let {songs->
                            if(curPlayingSong==null && songs.isNotEmpty()){
                                curPlayingSong=songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else ->Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if(it==null) return@observe
            curPlayingSong=it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState=it
            binding.ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying==true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.progress=it?.position?.toInt() ?:0
        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner){
            if(shouldUpdateSeekbar){
                binding.seekBar.progress=it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            binding.seekBar.max=it.toInt()
            val dateFormat= SimpleDateFormat("mm:ss", Locale.getDefault())
            binding.tvSongDuration.text=dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat= SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvCurTime.text=dateFormat.format(ms)
    }
}