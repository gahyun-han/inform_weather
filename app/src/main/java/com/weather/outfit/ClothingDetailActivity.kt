package com.weather.outfit

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.databinding.ActivityClothingDetailBinding
import kotlinx.coroutines.launch
import java.io.File

class ClothingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClothingDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val clothingId = intent.getLongExtra("clothing_id", -1L)
        if (clothingId == -1L) {
            finish()
            return
        }

        loadClothingItem(clothingId)
    }

    private fun loadClothingItem(id: Long) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ClothingDetailActivity)
            val item = db.clothingDao().getById(id)
            if (item == null) {
                Toast.makeText(this@ClothingDetailActivity, "옷을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            supportActionBar?.title = item.name

            binding.tvClothingName.text = item.name
            binding.tvCategory.text = item.category.koreanName
            binding.tvWarmthLevel.text = "보온성: ${"★".repeat(item.warmthLevel)}${"☆".repeat(5 - item.warmthLevel)}"
            binding.tvTempRange.text = "적정 온도: ${item.minTemp}°C ~ ${item.maxTemp}°C"
            binding.tvColorTag.text = if (item.colorTag.isNotEmpty()) "색상: ${item.colorTag}" else ""
            binding.tvNotes.text = if (item.notes.isNotEmpty()) item.notes else "메모 없음"

            // Load clothing image
            if (item.imagePath.isNotEmpty() && File(item.imagePath).exists()) {
                Glide.with(this@ClothingDetailActivity)
                    .load(File(item.imagePath))
                    .placeholder(R.drawable.ic_clothing_placeholder)
                    .into(binding.ivClothingImage)
            } else {
                binding.ivClothingImage.setImageResource(R.drawable.ic_clothing_placeholder)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
