package com.weather.outfit

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.weather.outfit.adapter.ClothingAdapter
import com.weather.outfit.adapter.NaverShoppingAdapter
import com.weather.outfit.api.NaverShoppingItem
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
    private lateinit var clothingAdapter: ClothingAdapter
    private lateinit var catalogAdapter: NaverShoppingAdapter

    private var isCatalogOpen = false
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

        setupClosetRecyclerView()
        setupCatalogRecyclerView()
        setupCategoryChips()
        setupFabs()
        observeViewModel()

        showClosetSection()
    }

    // ===== SETUP =====

    private fun setupClosetRecyclerView() {
        clothingAdapter = ClothingAdapter(
            onItemClick = { item ->
                startActivity(
                    Intent(this, ClothingDetailActivity::class.java)
                        .putExtra("clothing_id", item.id)
                )
            },
            onItemLongClick = { item ->
                showDeleteDialog(item)
            },
            onDeleteClick = { item ->
                showDeleteDialog(item)
            }
        )
        binding.rvClothing.layoutManager = GridLayoutManager(this, 2)
        binding.rvClothing.adapter = clothingAdapter
    }

    private fun setupCatalogRecyclerView() {
        catalogAdapter = NaverShoppingAdapter { item ->
            val name = item.cleanTitle.take(40)
            val category = detectCategoryFromNaver(item)
            viewModel.addFromNaverItem(item, name, category)
        }
        binding.rvCatalog.layoutManager = GridLayoutManager(this, 2)
        binding.rvCatalog.adapter = catalogAdapter

        binding.btnLoadMore.setOnClickListener {
            viewModel.loadMoreCatalog()
        }

        binding.btnCloseCatalog.setOnClickListener {
            showClosetSection()
        }
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(null)
        }

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

        // Catalog category chips
        val catalogChipMap = mapOf(
            binding.chipCatalogAll to "ALL",
            binding.chipCatalogTop to "TOP",
            binding.chipCatalogBottom to "BOTTOM",
            binding.chipCatalogOuter to "OUTER",
            binding.chipCatalogDress to "DRESS",
            binding.chipCatalogShoes to "SHOES",
            binding.chipCatalogAccessory to "ACCESSORY"
        )
        catalogChipMap.forEach { (chip, key) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val gender = if (binding.genderToggle.checkedButtonId == R.id.btnGenderFemale) "female" else "male"
                    viewModel.searchCatalog(gender, key)
                }
            }
        }

        // Gender toggle
        binding.genderToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val gender = if (checkedId == R.id.btnGenderFemale) "female" else "male"
                val checkedChip = catalogChipMap.entries.firstOrNull { (chip, _) -> chip.isChecked }
                val categoryKey = checkedChip?.value ?: "ALL"
                viewModel.searchCatalog(gender, categoryKey)
            }
        }
    }

    private fun setupFabs() {
        binding.fabAddClothing.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
        binding.fabAddSimilarStyle.setOnClickListener {
            showCatalogSection()
        }
    }

    // ===== SECTION SWITCHING =====

    private fun showClosetSection() {
        isCatalogOpen = false
        binding.sectionCloset.visibility = View.VISIBLE
        binding.sectionCatalog.visibility = View.GONE
        binding.fabContainer.visibility = View.VISIBLE
        supportActionBar?.title = "MY CLOSET"
    }

    private fun showCatalogSection() {
        isCatalogOpen = true
        binding.sectionCloset.visibility = View.GONE
        binding.sectionCatalog.visibility = View.VISIBLE
        binding.fabContainer.visibility = View.GONE
        supportActionBar?.title = "유사스타일 추가"

        if (!viewModel.catalogLoaded) {
            if (binding.genderToggle.checkedButtonId == View.NO_ID) {
                binding.genderToggle.check(R.id.btnGenderFemale)
            } else {
                viewModel.searchCatalog("female", "ALL")
            }
        }
    }

    // ===== OBSERVE =====

    private fun observeViewModel() {
        viewModel.filteredItems.observe(this) { items ->
            clothingAdapter.submitList(items)
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

        viewModel.catalogItems.observe(this) { items ->
            catalogAdapter.submitList(items)
            val isEmpty = items.isEmpty() && viewModel.catalogLoading.value != true
            binding.layoutCatalogEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.btnLoadMore.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.catalogLoading.observe(this) { loading ->
            binding.pbCatalogLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.catalogError.observe(this) { error ->
            if (error != null) {
                binding.layoutCatalogEmpty.visibility = View.VISIBLE
                binding.tvCatalogError.text = error
            }
        }
    }

    // ===== MY CLOSET ACTIONS =====

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
        }

        dialogBinding.btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

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

    // ===== NAVER CATALOG ACTIONS =====

    private fun detectCategoryFromNaver(item: NaverShoppingItem): ClothingCategory {
        val cat2 = item.category2.lowercase()
        val cat3 = item.category3.lowercase()
        return when {
            cat2.contains("신발") || cat3.contains("운동화") || cat3.contains("부츠") || cat3.contains("샌들") -> ClothingCategory.SHOES
            cat2.contains("가방") || cat2.contains("액세서리") || cat2.contains("악세서리") || cat3.contains("모자") || cat3.contains("스카프") -> ClothingCategory.ACCESSORY
            cat3.contains("원피스") || cat3.contains("드레스") -> ClothingCategory.DRESS
            cat3.contains("재킷") || cat3.contains("코트") || cat3.contains("점퍼") || cat3.contains("패딩") || cat3.contains("아우터") -> ClothingCategory.OUTER
            cat3.contains("바지") || cat3.contains("청바지") || cat3.contains("스커트") || cat3.contains("치마") || cat3.contains("레깅스") -> ClothingCategory.BOTTOM
            else -> ClothingCategory.TOP
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_closet, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_closet -> {
                showClearAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("옷장 전체 비우기")
            .setMessage("옷장에 있는 모든 옷(${viewModel.clothingCount.value ?: 0}개)을 삭제할까요?\n이 작업은 되돌릴 수 없어요.")
            .setPositiveButton("전체 삭제") { _, _ ->
                viewModel.deleteAllClothingItems()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isCatalogOpen) {
            showClosetSection()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (isCatalogOpen) {
            showClosetSection()
            return true
        }
        onBackPressed()
        return true
    }
}
