package com.example.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView


import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.databinding.ListItemBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {
    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseSongAdapter.SongViewHolder, position: Int) {
        val song = songs[position]
        holder.apply {
            tvPrimary?.text = song.title
            tvSecondary?.text = song.subtitle
            glide.load(song.imageUrl).into(ivItemImage!!)

            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}