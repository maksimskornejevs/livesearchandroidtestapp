package com.makskor.livesearchandroidtestapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makskor.livesearchandroidtestapp.databinding.RecyclerviewGifImageHolderBinding
import com.makskor.livesearchandroidtestapp.models.GifImage

class SearchListRecyclerAdapter :
    RecyclerView.Adapter<SearchListRecyclerAdapter.GifImageViewHolder>() {

    private val searchResults: ArrayList<GifImage> = ArrayList<GifImage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerviewGifImageHolderBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.recyclerview_gif_image_holder,
            parent,
            false
        )

        return GifImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: GifImageViewHolder, position: Int) {
        holder.bind(searchResults[position])
    }

    fun setList(gifImages: List<GifImage>) {
        searchResults.clear()
        searchResults.addAll(gifImages)
    }

    inner class GifImageViewHolder(val binding: RecyclerviewGifImageHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(gifImage: GifImage) {
            Glide.with(binding.root).load(gifImage.url).into(binding.ivImage)
        }
    }
}