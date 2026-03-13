package com.weather.outfit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.weather.outfit.api.PexelsPhoto
import com.weather.outfit.databinding.ItemOutfitImageBinding

class OutfitImageAdapter(
    private val onItemClick: (PexelsPhoto) -> Unit = {}
) : ListAdapter<PexelsPhoto, OutfitImageAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemOutfitImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: PexelsPhoto) {
            Glide.with(binding.root.context)
                .load(photo.src.medium)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.ivOutfitImage)

            binding.root.setOnClickListener { onItemClick(photo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOutfitImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PexelsPhoto>() {
            override fun areItemsTheSame(old: PexelsPhoto, new: PexelsPhoto) = old.id == new.id
            override fun areContentsTheSame(old: PexelsPhoto, new: PexelsPhoto) = old == new
        }
    }
}
