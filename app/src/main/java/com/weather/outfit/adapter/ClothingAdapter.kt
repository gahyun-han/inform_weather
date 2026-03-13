package com.weather.outfit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.weather.outfit.R
import com.weather.outfit.data.model.ClothingItem
import com.weather.outfit.databinding.ItemClothingBinding
import java.io.File

class ClothingAdapter(
    private val onItemClick: (ClothingItem) -> Unit,
    private val onItemLongClick: (ClothingItem) -> Unit
) : ListAdapter<ClothingItem, ClothingAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothingItem) {
            binding.tvClothingName.text = item.name
            binding.tvCategory.text = item.category.koreanName
            binding.tvWarmthStars.text = "★".repeat(item.warmthLevel) + "☆".repeat(5 - item.warmthLevel)
            binding.tvTempRange.text = "${item.minTemp}°~${item.maxTemp}°"

            // Load image
            if (item.imagePath.isNotEmpty() && File(item.imagePath).exists()) {
                Glide.with(binding.root.context)
                    .load(File(item.imagePath))
                    .placeholder(R.drawable.ic_clothing_placeholder)
                    .centerCrop()
                    .into(binding.ivClothingThumbnail)
            } else {
                binding.ivClothingThumbnail.setImageResource(R.drawable.ic_clothing_placeholder)
            }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ClothingItem>() {
            override fun areItemsTheSame(old: ClothingItem, new: ClothingItem) = old.id == new.id
            override fun areContentsTheSame(old: ClothingItem, new: ClothingItem) = old == new
        }
    }
}
