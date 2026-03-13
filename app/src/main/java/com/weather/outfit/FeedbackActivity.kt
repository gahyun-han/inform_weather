package com.weather.outfit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.weather.outfit.data.model.ComfortLevel
import com.weather.outfit.databinding.ActivityFeedbackBinding
import com.weather.outfit.ui.FeedbackViewModel
import com.weather.outfit.util.DateUtils

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private val viewModel: FeedbackViewModel by viewModels()

    // These will be passed in from MainActivity or read from cached weather
    private var currentTemp = 0f
    private var currentFeelsLike = 0f
    private var currentCondition = ""
    private var outfitWarmthScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "오늘 코디 피드백"

        // Get weather data passed from main activity
        currentTemp = intent.getFloatExtra("temperature", 0f)
        currentFeelsLike = intent.getFloatExtra("feels_like", 0f)
        currentCondition = intent.getStringExtra("condition") ?: ""
        outfitWarmthScore = intent.getIntExtra("warmth_score", 0)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvDate.text = DateUtils.formatDisplay(DateUtils.todayString())
        binding.tvTemperatureInfo.text = "오늘 기온: ${currentTemp.toInt()}°C (체감 ${currentFeelsLike.toInt()}°C)"

        // Comfort level buttons
        val comfortButtons = mapOf(
            binding.btnTooCold to ComfortLevel.TOO_COLD,
            binding.btnSlightlyCold to ComfortLevel.SLIGHTLY_COLD,
            binding.btnJustRight to ComfortLevel.JUST_RIGHT,
            binding.btnSlightlyHot to ComfortLevel.SLIGHTLY_HOT,
            binding.btnTooHot to ComfortLevel.TOO_HOT
        )

        var selectedComfort: ComfortLevel? = null

        comfortButtons.forEach { (button, level) ->
            button.setOnClickListener {
                selectedComfort = level
                // Highlight selected button
                comfortButtons.keys.forEach { btn ->
                    btn.isSelected = (btn == button)
                }
                binding.btnSaveFeedback.isEnabled = true
            }
        }

        binding.btnSaveFeedback.setOnClickListener {
            val comfort = selectedComfort
            if (comfort == null) {
                Toast.makeText(this, "오늘 코디가 어떠셨나요?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notes = binding.etFeedbackNotes.text.toString().trim()
            viewModel.saveFeedback(
                comfortLevel = comfort,
                temperature = currentTemp,
                feelsLike = currentFeelsLike,
                weatherCondition = currentCondition,
                clothingItemIds = emptyList(),
                outfitWarmthScore = outfitWarmthScore,
                notes = notes
            )
        }
    }

    private fun observeViewModel() {
        viewModel.todayFeedback.observe(this) { feedback ->
            if (feedback != null) {
                // Pre-select today's existing feedback
                binding.tvExistingFeedback.text = "오늘의 피드백: ${feedback.comfortLevel.koreanName} ${feedback.comfortLevel.emoji}"
                binding.tvExistingFeedback.visibility = View.VISIBLE
            }
        }

        viewModel.sensitivityText.observe(this) { text ->
            binding.tvSensitivityProfile.text = text
        }

        viewModel.insight.observe(this) { insight ->
            if (insight.isNotEmpty()) {
                binding.tvInsight.text = insight
                binding.tvInsight.visibility = View.VISIBLE
            }
        }

        viewModel.savedSuccessfully.observe(this) { saved ->
            if (saved) {
                Toast.makeText(this, "피드백이 저장되었어요! 다음 코디 추천에 반영할게요 😊", Toast.LENGTH_LONG).show()
                viewModel.resetSavedFlag()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
