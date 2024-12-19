package com.example.languagetranslatorapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var translateButton: Button
    private lateinit var outputTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var englishHindiTranslator: Translator
    private var isTranslatorReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupTranslator()
    }

    private fun initializeViews() {
        inputEditText = findViewById(R.id.englishInput)
        translateButton = findViewById(R.id.translateButton)
        outputTextView = findViewById(R.id.hindiOutput)
        progressBar = findViewById(R.id.progressBar)

        translateButton.isEnabled = false
    }

    private fun setupTranslator() {
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build()

            englishHindiTranslator = Translation.getClient(options)
            downloadTranslationModel()
        } catch (e: Exception) {
            handleError("Translator initialization failed: ${e.message}")
        }
    }

    private fun downloadTranslationModel() {
        showLoading(true)

        val conditions = DownloadConditions.Builder()
            .build()

        englishHindiTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isTranslatorReady = true
                setupTranslateButton()
                showLoading(false)
                Toast.makeText(this, "Translator is ready!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                handleError("Language model download failed. Please check your internet connection.")
            }
    }

    private fun setupTranslateButton() {
        translateButton.isEnabled = true
        translateButton.setOnClickListener {
            val inputText = inputEditText.text.toString().trim()

            when {
                !isTranslatorReady -> {
                    Toast.makeText(this, "Translator is not ready yet. Please wait.", Toast.LENGTH_SHORT).show()
                }
                inputText.isEmpty() -> {
                    inputEditText.error = "Please enter text to translate"
                }
                else -> {
                    translateText(inputText)
                }
            }
        }
    }

    private fun translateText(text: String) {
        showLoading(true)

        englishHindiTranslator.translate(text)
            .addOnSuccessListener { translatedText ->
                showLoading(false)
                outputTextView.text = translatedText
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                handleError("Translation failed. Please try again.")
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        inputEditText.isEnabled = !show
        translateButton.isEnabled = !show && isTranslatorReady
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        outputTextView.text = "Error: $message"
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            englishHindiTranslator.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
}