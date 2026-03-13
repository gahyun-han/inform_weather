package com.weather.outfit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ClothingItem
import com.weather.outfit.data.repository.ClothingRepository
import com.weather.outfit.util.ImageUtils
import kotlinx.coroutines.launch

class ClosetViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ClothingRepository(AppDatabase.getInstance(application).clothingDao())

    val allClothingItems: LiveData<List<ClothingItem>> = repo.getAllClothingItems()

    private val _selectedCategory = MutableLiveData<ClothingCategory?>(null)
    val selectedCategory: LiveData<ClothingCategory?> = _selectedCategory

    private val _filteredItems = MutableLiveData<List<ClothingItem>>(emptyList())
    val filteredItems: LiveData<List<ClothingItem>> = _filteredItems

    private val _operationStatus = MutableLiveData<String?>()
    val operationStatus: LiveData<String?> = _operationStatus

    private val _clothingCount = MutableLiveData<Int>(0)
    val clothingCount: LiveData<Int> = _clothingCount

    init {
        allClothingItems.observeForever { items ->
            applyFilter(items)
        }
        loadCount()
    }

    fun setFilter(category: ClothingCategory?) {
        _selectedCategory.value = category
        applyFilter(allClothingItems.value ?: emptyList())
    }

    private fun applyFilter(items: List<ClothingItem>) {
        val category = _selectedCategory.value
        _filteredItems.value = if (category == null) items else items.filter { it.category == category }
    }

    /**
     * Adds a new clothing item.
     * If imageUri is provided, copies the image to app storage first.
     */
    fun addClothingItem(
        name: String,
        category: ClothingCategory,
        warmthLevel: Int,
        minTemp: Int,
        maxTemp: Int,
        colorTag: String,
        notes: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            val imagePath = if (imageUri != null) {
                val fileName = "clothing_${System.currentTimeMillis()}"
                ImageUtils.copyImageToStorage(getApplication(), imageUri, fileName) ?: ""
            } else {
                ""
            }

            val item = ClothingItem(
                name = name,
                category = category,
                warmthLevel = warmthLevel,
                minTemp = minTemp,
                maxTemp = maxTemp,
                colorTag = colorTag,
                notes = notes,
                imagePath = imagePath
            )
            repo.addClothingItem(item)
            _operationStatus.value = "\"$name\"이(가) 옷장에 추가되었어요 👕"
            loadCount()
        }
    }

    fun updateClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            repo.updateClothingItem(item)
            _operationStatus.value = "\"${item.name}\" 정보가 업데이트되었어요"
        }
    }

    fun deleteClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            repo.removeClothingItem(item.id)
            // Delete the image file
            if (item.imagePath.isNotEmpty()) {
                ImageUtils.deleteImage(item.imagePath)
            }
            _operationStatus.value = "\"${item.name}\"이(가) 옷장에서 제거되었어요"
            loadCount()
        }
    }

    private fun loadCount() {
        viewModelScope.launch {
            _clothingCount.value = repo.getCount()
        }
    }

    fun clearStatus() {
        _operationStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        allClothingItems.removeObserver { }
    }
}
