package com.weather.outfit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weather.outfit.data.model.WeatherConditionType
import com.weather.outfit.databinding.ActivityMainBinding
import com.weather.outfit.ui.MainViewModel
import com.weather.outfit.ui.UiState
import com.weather.outfit.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                fetchLocation()
            }
            else -> {
                // Permission denied - use Seoul as default
                viewModel.fetchWeatherByCity("Seoul")
                Toast.makeText(this, "위치 권한이 없어 서울 날씨로 표시합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupUI()
        observeViewModel()
        requestWeather()
    }

    private fun setupUI() {
        // Date display
        val dateFormat = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN)
        binding.tvDate.text = dateFormat.format(Date())

        // Navigation buttons
        binding.btnCloset.setOnClickListener {
            startActivity(Intent(this, ClosetActivity::class.java))
        }

        binding.btnFeedback.setOnClickListener {
            val weather = viewModel.weather.value
            val rec = viewModel.recommendation.value
            val intent = Intent(this, FeedbackActivity::class.java).apply {
                putExtra("temperature", weather?.temperature ?: 0f)
                putExtra("feels_like", weather?.feelsLike ?: 0f)
                putExtra("condition", weather?.weatherCondition ?: "")
                putExtra("warmth_score", rec?.warmthLevel ?: 0)
            }
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            requestWeather()
        }

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            requestWeather()
        }

    }

    private fun observeViewModel() {
        viewModel.weather.observe(this) { weather ->
            if (weather == null) return@observe
            updateWeatherUI(weather.temperature, weather.feelsLike, weather.humidity,
                weather.windSpeed, weather.weatherCondition, weather.weatherDescription,
                weather.cityName, weather.tempMin, weather.tempMax)
        }

        viewModel.recommendation.observe(this) { rec ->
            if (rec == null) return@observe
            updateRecommendationUI(rec.outfitDescription, rec.characterOutfitKey,
                rec.umbrellaRecommended, rec.sunscreenRecommended, rec.weatherTip)
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = true
                }
                is UiState.Success, is UiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.userPreferences.observe(this) { prefs ->
            if (prefs != null) {
                binding.tvGreeting.text = "${DateUtils.getGreeting()}\n${prefs.userName}님!"
                if (prefs.coldSensitivityOffset != 0f) {
                    val adjustText = if (prefs.coldSensitivityOffset < 0)
                        "추위를 잘 타는 편이에요 🧊" else "더위를 잘 타는 편이에요 🔥"
                    binding.tvSensitivity.text = adjustText
                    binding.tvSensitivity.visibility = View.VISIBLE
                } else {
                    binding.tvSensitivity.visibility = View.GONE
                }
            }
        }

        viewModel.todayFeedbackExists.observe(this) { exists ->
            binding.btnFeedback.text = if (exists) "피드백 수정" else "오늘 코디 피드백"
            binding.cardFeedbackPrompt.visibility = if (!exists) View.VISIBLE else View.GONE
        }

        viewModel.feedbackInsight.observe(this) { insight ->
            if (insight.isNotEmpty()) {
                binding.tvFeedbackInsight.text = insight
                binding.tvFeedbackInsight.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (msg != null) {
                viewModel.clearError()
            }
        }

    }

    private fun updateWeatherUI(
        temp: Float, feelsLike: Float, humidity: Int, windSpeed: Float,
        condition: String, description: String, city: String,
        tempMin: Float, tempMax: Float
    ) {
        binding.tvTemperature.text = "${temp.toInt()}°C"
        binding.tvFeelsLike.text = "체감 ${feelsLike.toInt()}°C"
        binding.tvWeatherDesc.text = description.replaceFirstChar { it.uppercase() }
        binding.tvCity.text = city
        binding.tvHumidity.text = "습도 ${humidity}%"
        binding.tvWindSpeed.text = "바람 ${String.format("%.1f", windSpeed)}m/s"
        binding.tvTempRange.text = "최저 ${tempMin.toInt()}° / 최고 ${tempMax.toInt()}°"

        // Set weather icon/background based on condition
        val conditionType = WeatherConditionType.fromCondition(condition)
        updateWeatherBackground(conditionType)
    }

    private fun updateRecommendationUI(
        description: String, outfitKey: String,
        umbrellaNeeded: Boolean, sunscreenNeeded: Boolean,
        tip: String
    ) {
        binding.tvOutfitDescription.text = description

        // Load character illustration
        val drawableId = getCharacterDrawableId(outfitKey)
        Glide.with(this)
            .load(drawableId)
            .fitCenter()
            .placeholder(R.drawable.character_outfit_mild)
            .into(binding.ivCharacter)

        // Accessory icons
        binding.ivUmbrella.visibility = if (umbrellaNeeded) View.VISIBLE else View.GONE
        binding.ivSunscreen.visibility = if (sunscreenNeeded) View.VISIBLE else View.GONE

        // Weather tip
        if (tip.isNotEmpty()) {
            binding.tvWeatherTip.text = tip
            binding.tvWeatherTip.visibility = View.VISIBLE
        } else {
            binding.tvWeatherTip.visibility = View.GONE
        }
    }

    private fun updateWeatherBackground(condition: WeatherConditionType) {
        val backgroundRes = when (condition) {
            WeatherConditionType.CLEAR -> R.drawable.bg_sunny
            WeatherConditionType.PARTLY_CLOUDY -> R.drawable.bg_partly_cloudy
            WeatherConditionType.CLOUDY -> R.drawable.bg_cloudy
            WeatherConditionType.LIGHT_RAIN -> R.drawable.bg_rain
            WeatherConditionType.HEAVY_RAIN -> R.drawable.bg_heavy_rain
            WeatherConditionType.SNOW -> R.drawable.bg_snow
            WeatherConditionType.THUNDERSTORM -> R.drawable.bg_thunderstorm
            WeatherConditionType.FOGGY -> R.drawable.bg_fog
            WeatherConditionType.WINDY -> R.drawable.bg_cloudy
        }
        binding.layoutWeatherHeader.setBackgroundResource(backgroundRes)
    }

    private fun getCharacterDrawableId(outfitKey: String): Int {
        val resName = "character_$outfitKey"
        val resId = resources.getIdentifier(resName, "drawable", packageName)
        return if (resId != 0) resId else R.drawable.character_outfit_mild
    }

    private fun requestWeather() {
        // Try GPS first
        if (hasLocationPermission()) {
            fetchLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        if (!hasLocationPermission()) return

        try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.fetchWeatherByLocation(location.latitude, location.longitude)
                } else {
                    // Fallback to preferences or Seoul
                    viewModel.fetchWeatherByCity("Seoul")
                }
            }.addOnFailureListener {
                viewModel.fetchWeatherByCity("Seoul")
            }
        } catch (e: SecurityException) {
            viewModel.fetchWeatherByCity("Seoul")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecommendationFromCache()
    }
}
