package com.example.spotifyclone.ui
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.ExoPlayer.isPlaying
import com.example.spotifyclone.ExoPlayer.toSong
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.databinding.FragmentHomeBinding
import com.example.spotifyclone.models.Song
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.utils.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var vpSong: ViewPager2
    private lateinit var ivCurSongImage: ImageView
    private lateinit var ivPlayPause: ImageView
    private var navHostFragment: Fragment? =null


    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState:PlaybackStateCompat?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        ivPlayPause=findViewById(R.id.ivPlayPause)
        vpSong= findViewById(R.id.vpSong)
        ivCurSongImage=findViewById(R.id.ivCurSongImage)
        navHostFragment=supportFragmentManager.findFragmentById(R.id.navHostFragment)
        vpSong.adapter = swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying==true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }
                else{
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })
        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it,true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment?.findNavController()?.navigate(
                R.id.globalActionToSongFragment
            )
        }

        navHostFragment?.findNavController()?.addOnDestinationChangedListener{ _, destination, _ ->
            when(destination.id){
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }

    private fun hideBottomBar(){
        ivCurSongImage.isVisible=false
        vpSong.isVisible=false
        ivPlayPause.isVisible=false
    }
    private fun showBottomBar(){
        ivCurSongImage.isVisible=true
        vpSong.isVisible=true
        ivPlayPause.isVisible=true
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()) {
                                glide.load((curPlayingSong ?: songs[0]).imageUrl).into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if(it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this){
            playbackState=it
            ivPlayPause.setImageResource(
                if(playbackState?.isPlaying==true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandeled()?.let {result->
                when(result.status){
                    Status.ERROR -> Snackbar.make(findViewById(com.google.android.material.R.id.coordinator),result.message?: "An error occured", Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    mainViewModel.networkError.observe(this){
        it?.getContentIfNotHandeled()?.let {result->
            when(result.status){
                Status.ERROR -> Snackbar.make(findViewById(com.google.android.material.R.id.coordinator),result.message?: "An error occured", Snackbar.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }
    }
}
