package com.weather.outfit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.weather.outfit.data.db.AppDatabase
import com.weather.outfit.data.model.UserPreference
import com.weather.outfit.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already onboarded, go directly to MainActivity
        lifecycleScope.launch {
            val prefs = AppDatabase.getInstance(this@OnboardingActivity)
                .userPreferenceDao()
                .getPreferencesSync()
            if (prefs != null) {
                goToMain()
                return@launch
            }
            // Show onboarding UI (already set by setContentView below)
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnGetStarted.setOnClickListener {
            val userName = binding.etUserName.text.toString().trim()
                .takeIf { it.isNotEmpty() } ?: "사용자"

            val coldSensitivity = when (binding.rgSensitivity.checkedRadioButtonId) {
                R.id.rbVeryCold -> -3f
                R.id.rbSomewhatCold -> -1.5f
                R.id.rbNormal -> 0f
                R.id.rbSomewhatHot -> 1.5f
                R.id.rbVeryHot -> 3f
                else -> 0f
            }

            val characterGender = when (binding.rgCharacter.checkedRadioButtonId) {
                R.id.rbFemale -> "FEMALE"
                R.id.rbMale -> "MALE"
                else -> "FEMALE"
            }

            savePreferencesAndProceed(userName, coldSensitivity, characterGender)
        }

        // Skip button
        binding.tvSkip.setOnClickListener {
            savePreferencesAndProceed("사용자", 0f, "FEMALE")
        }
    }

    private fun savePreferencesAndProceed(
        userName: String,
        sensitivityOffset: Float,
        characterGender: String
    ) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@OnboardingActivity)
            db.userPreferenceDao().insert(
                UserPreference(
                    userName = userName,
                    coldSensitivityOffset = sensitivityOffset,
                    characterGender = characterGender
                )
            )
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
