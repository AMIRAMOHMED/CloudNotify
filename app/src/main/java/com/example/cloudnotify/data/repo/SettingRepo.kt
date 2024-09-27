package com.example.cloudnotify.data.repo

import android.app.Application
import com.example.cloudnotify.data.local.sharedPrefrence.SharedPreferencesManager

class SettingRepo (
    application: Application
){

    private val sharedPreferencesManager = SharedPreferencesManager(application)



    // save unit
    fun saveUnit(unit: String) {
        sharedPreferencesManager.saveUnit(unit)
    }



    // save language
    fun saveLanguage(language: String) {
        sharedPreferencesManager.saveLanguage(language)
    }



}