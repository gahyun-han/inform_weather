package com.weather.outfit

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.weather.outfit.adapter.ClothingAdapter
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ClothingItem
import com.weather.outfit.databinding.ActivityClosetBinding
import com.weather.outfit.databinding.DialogAddClothingBinding
import com.weather.outfit.ui.ClosetViewModel
import com.weather.outfit.util.ImageUtils
import java.io.File

class ClosetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClosetBinding
    private val viewModel: ClosetViewModel by viewModels()
    private lateinit var adapter: ClothingAdapter

    private var pendingImageUri: Uri? = null
    private var tempCameraFile: File? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraFile?.let { pendingImageUri = Uri.fromFile(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { pendingImageUri = it }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            showAddClothingDialog()
        } else {
            Toast.makeText(this, "카메라/갤러리 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClosetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "MY CLOSET"

        setupRecyclerView()
        setupCategoryChips()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ClothingAdapter(
            onItemClick = { item ->
                startActivity(
                    Intent(this, ClothingDetailActivity::class.java)
                        .putExtra("clothing_id", item.id)
                )
            },
            onItemLongClick = { item ->
                showDeleteDialog(item)
            }
        )
        binding.rvClothing.layoutManager = GridLayoutManager(this, 2)
        binding.rvClothing.adapter = adapter
    }

    private fun setupCategoryChips() {
        // "전체" chip
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(null)
        }

        // Category chips
        ClothingCategory.values().forEach { category ->
            val chip = Chip(this).apply {
                text = category.koreanName
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.setFilter(category)
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupFab() {
        binding.fabAddClothing.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredItems.observe(this) { items ->
            adapter.submitList(items)
            binding.tvEmptyCloset.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvClothing.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.clothingCount.observe(this) { count ->
            binding.tvClosetCount.text = "총 ${count}개의 옷"
        }

        viewModel.operationStatus.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }
    }

    private fun checkPermissionsAndShowDialog() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.CAMERA)
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun showAddClothingDialog() {
        pendingImageUri = null
        val dialogBinding = DialogAddClothingBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("새 옷 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                saveNewClothing(dialogBinding)
            }
            .setNegativeButton("취소", null)
            .create()

        dialogBinding.btnTakePhoto.setOnClickListener {
            val photoFile = ImageUtils.createImageFile(this)
            tempCameraFile = photoFile
            val uri = ImageUtils.getFileUri(this, photoFile)
            cameraLauncher.launch(uri)
            dialog.dismiss()
            // Note: re-show dialog after camera returns (simplified flow)
        }

        dialogBinding.btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Warmth level slider label
        dialogBinding.sliderWarmth.addOnChangeListener { _, value, _ ->
            dialogBinding.tvWarmthLabel.text = when (value.toInt()) {
                1 -> "매우 얇음 (민소매/반팔)"
                2 -> "얇음 (얇은 긴팔)"
                3 -> "보통 (자켓/카디건)"
                4 -> "따뜻함 (코트/패딩)"
                5 -> "매우 따뜻함 (두꺼운 패딩)"
                else -> "보통"
            }
        }

        dialog.show()
    }

    private fun saveNewClothing(dialogBinding: DialogAddClothingBinding) {
        val name = dialogBinding.etClothingName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "옷 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryIndex = dialogBinding.spinnerCategory.selectedItemPosition
        val category = ClothingCategory.values()[categoryIndex]
        val warmthLevel = dialogBinding.sliderWarmth.value.toInt()
        val minTemp = dialogBinding.etMinTemp.text.toString().toIntOrNull() ?: -10
        val maxTemp = dialogBinding.etMaxTemp.text.toString().toIntOrNull() ?: 35
        val colorTag = dialogBinding.etColorTag.text.toString().trim()
        val notes = dialogBinding.etNotes.text.toString().trim()

        viewModel.addClothingItem(
            name = name,
            category = category,
            warmthLevel = warmthLevel,
            minTemp = minTemp,
            maxTemp = maxTemp,
            colorTag = colorTag,
            notes = notes,
            imageUri = pendingImageUri
        )
    }

    private fun showDeleteDialog(item: ClothingItem) {
        AlertDialog.Builder(this)
            .setTitle("옷 삭제")
            .setMessage("\"${item.name}\"을(를) 옷장에서 제거할까요?")
            .setPositiveButton("제거") { _, _ ->
                viewModel.deleteClothingItem(item)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
