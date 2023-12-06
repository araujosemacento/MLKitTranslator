package com.example.mlkittranslator

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.translation.Translator
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

  private lateinit var sourceLanguage: EditText
  private lateinit var targetLanguage: TextView
  private lateinit var chooseSourceLanguage: MaterialButton
  private lateinit var chooseTargetLanguage: MaterialButton
  private lateinit var translate: MaterialButton

  companion object{

    private const val TAG = "MAIN_TAG"
  }

  private var languageArrayList: ArrayList<ModelLanguage>? = null

  private var sourceLanguageCode = "en"
  private var sourceLanguageTitle = "Inglês"
  private var targetLanguageCode = "es"
  private var targetLanguageTitle = "Espanhol"

  private lateinit var translatorOptions: TranslatorOptions

  private lateinit var translator: com.google.mlkit.nl.translate.Translator

  private lateinit var progressDialog: ProgressDialog

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    sourceLanguage = findViewById(R.id.sourceLanguage)
    targetLanguage = findViewById(R.id.targetLanguage)
    chooseSourceLanguage = findViewById(R.id.chooseSourceLanguage)
    chooseTargetLanguage = findViewById(R.id.chooseTargetLanguage)
    translate = findViewById(R.id.translate)

    progressDialog = ProgressDialog(this)
    progressDialog.setTitle("Aguarde...")
    progressDialog.setCanceledOnTouchOutside(false)

    loadAvailableLanguages()

    chooseSourceLanguage.setOnClickListener{
      sourceLanguageChoose()
    }

    chooseTargetLanguage.setOnClickListener {
      targetLanguageChoose()
    }

    translate.setOnClickListener{
      validateData()
    }

  }

  private var sourceLanguageText = ""
  private fun validateData() {

    sourceLanguageText = sourceLanguage.text.toString().trim()

    Log.d(TAG, "validateData: sourceLanguageText: $sourceLanguageText")

    if (sourceLanguageText.isEmpty()){
      showToast("Escreva um texto a ser traduzido...")
    }
    else{
      startTranslation()
    }
  }

  private fun startTranslation() {
    progressDialog.setMessage("Processando modelo de linguagem...")
    progressDialog.show()

    translatorOptions = TranslatorOptions.Builder()
      .setSourceLanguage(sourceLanguageCode)
      .setTargetLanguage(targetLanguageCode)
      .build()
    translator = Translation.getClient(translatorOptions)

    val downlaodConditions = DownloadConditions.Builder()
      .requireWifi()
      .build()

    translator.downloadModelIfNeeded(downlaodConditions)
      .addOnSuccessListener {
        Log.d(TAG, "StartTranslation: model readt, start translation...")

        progressDialog.setMessage("Traduzindo...")

        translator.translate(sourceLanguageText)
          .addOnSuccessListener { translatedText ->
            Log.d(TAG, "StartTranslation: translatedText: $translatedText")

            progressDialog.dismiss()

            targetLanguage.text = translatedText
          }
          .addOnFailureListener { e ->
            progressDialog.dismiss()
            Log.e(TAG, "StartTranslation: ", e)

            showToast("Falha ao traduzir: ${e.message}")
          }
      }
      .addOnFailureListener{ e ->
        progressDialog.dismiss()
        Log.e(TAG, "startTranslation: ", e)

        showToast("Falha ao traduzir: ${e.message}")
      }
  }

  private fun loadAvailableLanguages(){

    languageArrayList = ArrayList()

    val languageCodeList = TranslateLanguage.getAllLanguages()

    for (languageCode in languageCodeList){

      val languageTitle = Locale(languageCode).displayLanguage

      Log.d(TAG, "loadAvailableLanguages: languageCode: $languageCode")
      Log.d(TAG, "loadAvailableLanguages: languageTitle: $languageTitle")

      val modelLanguage = ModelLanguage(languageCode, languageTitle)

      languageArrayList!!.add(modelLanguage)
    }
  }

  private fun sourceLanguageChoose(){

    val popupMenu = PopupMenu(this, chooseSourceLanguage)

    for (i in languageArrayList!!.indices){
      popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
    }

    popupMenu.show()

    popupMenu.setOnMenuItemClickListener { menuItem ->

      val position = menuItem.itemId

      sourceLanguageCode = languageArrayList!![position].languageCode
      sourceLanguageTitle = languageArrayList!![position].languageTitle

      chooseSourceLanguage.text = sourceLanguageTitle
      sourceLanguage.hint = "Escreva seu texto em $sourceLanguageTitle"

      Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: $sourceLanguageCode")
      Log.d(TAG, "sourceLanguageChoose: sourceLanguageTitle: $sourceLanguageTitle")

      false
    }
  }

  private fun targetLanguageChoose(){
    val popupMenu = PopupMenu(this, chooseTargetLanguage)

    for (i in languageArrayList!!.indices){
      popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
    }

    popupMenu.show()

    popupMenu.setOnMenuItemClickListener { menuItem ->

      val position = menuItem.itemId

      targetLanguageCode = languageArrayList!![position].languageCode
      targetLanguageTitle = languageArrayList!![position].languageTitle

      chooseTargetLanguage.text = targetLanguageTitle

      Log.d(TAG, "targetLanguageChoose: targetLanguageCode: $targetLanguageCode")
      Log.d(TAG, "targetLanguageChoose: targetLanguageTitle: $targetLanguageTitle")

      false
    }
  }

  private fun showToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
  }
}