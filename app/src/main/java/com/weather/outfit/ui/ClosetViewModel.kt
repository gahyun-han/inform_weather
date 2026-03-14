package com.weather.outfit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.weather.outfit.api.NaverShoppingApi
import com.weather.outfit.api.NaverShoppingItem
import com.weather.outfit.api.getNaverCatalogQuery
import com.weather.outfit.api.isUnderwear
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.ClothingCategory
import com.weather.outfit.data.model.ClothingItem
import com.weather.outfit.data.repository.ClothingRepository
import com.weather.outfit.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClosetViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ClothingRepository(AppDatabase.getInstance(application).clothingDao())

    // ===== MY CLOSET =====
    val allClothingItems: LiveData<List<ClothingItem>> = repo.getAllClothingItems()

    private val _selectedCategory = MutableLiveData<ClothingCategory?>(null)
    val selectedCategory: LiveData<ClothingCategory?> = _selectedCategory

    private val _filteredItems = MutableLiveData<List<ClothingItem>>(emptyList())
    val filteredItems: LiveData<List<ClothingItem>> = _filteredItems

    private val _operationStatus = MutableLiveData<String?>()
    val operationStatus: LiveData<String?> = _operationStatus

    private val _clothingCount = MutableLiveData<Int>(0)
    val clothingCount: LiveData<Int> = _clothingCount

    // ===== NAVER SHOPPING CATALOG =====
    private val _catalogItems = MutableLiveData<List<NaverShoppingItem>>(emptyList())
    val catalogItems: LiveData<List<NaverShoppingItem>> = _catalogItems

    private val _catalogLoading = MutableLiveData<Boolean>(false)
    val catalogLoading: LiveData<Boolean> = _catalogLoading

    private val _catalogError = MutableLiveData<String?>()
    val catalogError: LiveData<String?> = _catalogError

    private var catalogGender = "female"
    private var catalogCategoryKey = "ALL"
    private var catalogStart = 1
    private val catalogPageSize = 20
    var catalogLoaded = false
        private set

    init {
        allClothingItems.observeForever { items ->
            applyFilter(items)
        }
        loadCount()
    }

    // ===== MY CLOSET FUNCTIONS =====

    fun setFilter(category: ClothingCategory?) {
        _selectedCategory.value = category
        applyFilter(allClothingItems.value ?: emptyList())
    }

    private fun applyFilter(items: List<ClothingItem>) {
        val category = _selectedCategory.value
        _filteredItems.value = if (category == null) items else items.filter { it.category == category }
    }

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

    fun addFromNaverItem(
        item: NaverShoppingItem,
        name: String,
        category: ClothingCategory
    ) {
        viewModelScope.launch {
            val imagePath = withContext(Dispatchers.IO) {
                try {
                    val fileName = "naver_${item.productId}_${System.currentTimeMillis()}"
                    ImageUtils.downloadImageFromUrl(getApplication(), item.image, fileName) ?: ""
                } catch (e: Exception) {
                    ""
                }
            }
            val clothingItem = ClothingItem(
                name = name,
                category = category,
                warmthLevel = 3,
                minTemp = -10,
                maxTemp = 35,
                imagePath = imagePath
            )
            repo.addClothingItem(clothingItem)
            _operationStatus.value = "\"$name\"이(가) 내 옷장에 추가되었어요 👕"
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
            if (item.imagePath.isNotEmpty()) {
                ImageUtils.deleteImage(item.imagePath)
            }
            _operationStatus.value = "\"${item.name}\"이(가) 옷장에서 제거되었어요"
            loadCount()
        }
    }

    fun deleteAllClothingItems() {
        viewModelScope.launch {
            val items = repo.getAllClothingItemsSync()
            items.forEach { if (it.imagePath.isNotEmpty()) ImageUtils.deleteImage(it.imagePath) }
            repo.deleteAll()
            _operationStatus.value = "옷장을 비웠어요"
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

    // ===== CATALOG FUNCTIONS =====

    /**
     * Start a fresh catalog search with the given gender and category.
     */
    fun searchCatalog(gender: String, categoryKey: String) {
        catalogGender = gender
        catalogCategoryKey = categoryKey
        catalogStart = 1
        _catalogItems.value = emptyList()
        _catalogError.value = null
        loadCatalogPage()
    }

    /**
     * Load the next page of current catalog results.
     */
    fun loadMoreCatalog() {
        catalogStart += catalogPageSize
        loadCatalogPage()
    }

    private fun loadCatalogPage() {
        if (NaverShoppingApi.CLIENT_ID == "YOUR_NAVER_CLIENT_ID" ||
            NaverShoppingApi.CLIENT_SECRET == "YOUR_NAVER_CLIENT_SECRET") {
            _catalogError.value = "API 키가 설정되지 않았어요\nlocal.properties에 NAVER_CLIENT_ID와\nNAVER_CLIENT_SECRET을 모두 입력해주세요"
            return
        }
        viewModelScope.launch {
            _catalogLoading.value = true
            try {
                val query = getNaverCatalogQuery(catalogGender, catalogCategoryKey)
                val response = NaverShoppingApi.service.searchShop(
                    clientId = NaverShoppingApi.CLIENT_ID,
                    clientSecret = NaverShoppingApi.CLIENT_SECRET,
                    query = query,
                    display = catalogPageSize,
                    start = catalogStart
                )
                val filtered = response.items.filterNot { it.isUnderwear() }
                val existing = if (catalogStart == 1) emptyList() else (_catalogItems.value ?: emptyList())
                _catalogItems.value = existing + filtered
                catalogLoaded = true
            } catch (e: Exception) {
                _catalogError.value = "상품을 불러오지 못했어요\n잠시 후 다시 시도해주세요"
            } finally {
                _catalogLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        allClothingItems.removeObserver { }
    }
}
