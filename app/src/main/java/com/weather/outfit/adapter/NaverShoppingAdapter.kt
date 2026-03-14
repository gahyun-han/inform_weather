package com.weather.outfit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.weather.outfit.R
import com.weather.outfit.api.NaverShoppingItem
import com.weather.outfit.databinding.ItemNaverProductBinding

class NaverShoppingAdapter(
    private val onItemClick: (NaverShoppingItem) -> Unit = {}
) : ListAdapter<NaverShoppingItem, NaverShoppingAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemNaverProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NaverShoppingItem) {
            binding.tvProductName.text = item.cleanTitle
            binding.tvProductPrice.text = item.priceFormatted
            binding.tvMallName.text = item.mallName

            Glide.with(binding.root.context)
                .load(item.image)
                .placeholder(R.drawable.ic_clothing_placeholder)
                .centerCrop()
                .into(binding.ivProductImage)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNaverProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NaverShoppingItem>() {
            override fun areItemsTheSame(old: NaverShoppingItem, new: NaverShoppingItem) =
                old.productId == new.productId
            override fun areContentsTheSame(old: NaverShoppingItem, new: NaverShoppingItem) =
                old == new
        }
    }
}
