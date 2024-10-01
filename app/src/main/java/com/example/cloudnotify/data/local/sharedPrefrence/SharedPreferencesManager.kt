package com.example.cloudnotify.data.local.sharedPrefrence
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences by lazy {
            EncryptedSharedPreferences.create(
                context,
                KEYS.CLOUD_NOTIFY.key,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

    }

    // Generic method to save a double value
    fun saveValue(key: KEYS, value: Double) {
        sharedPreferences.edit().putString(key.key, value.toString()).apply()
    }
    // Generic method to get a long value
    fun getValue(key: KEYS, defaultValue: Double = 0.0): Double {
        return sharedPreferences.getString(key.key, defaultValue.toString())?.toDouble() ?: defaultValue
    }
// Generic method to save a string value
fun saveValueString(key: KEYS, value: String) {
    sharedPreferences.edit().putString(key.key, value).apply()
}

    // Generic method to get a string value
    fun getValueString(key: KEYS, defaultValue: String = ""): String {
        return sharedPreferences.getString(key.key, defaultValue) ?: defaultValue
    }
    // save GPS latitude
    fun saveGpsLocationLat(lat: Double) {
        saveValue(KEYS.GPS_LOCATION_LAT, lat)
    }
    // get GPS latitude
    fun getGpsLocationLat(): Double {
        return getValue(KEYS.GPS_LOCATION_LAT, 0.0)
    }
    //save GPS longitude
    fun saveGpsLocationLong(long: Double) {
        saveValue(KEYS.GPS_LOCATION_LON, long)
    }
    // get GPS longitude
    fun getGpsLocationLong(): Double {
        return getValue(KEYS.GPS_LOCATION_LON, 0.0)
    }
    // Delete GPS latitude and longitude
    fun deleteGpsLocation() {
        sharedPreferences.edit().remove(KEYS.GPS_LOCATION_LAT.key).apply()
        sharedPreferences.edit().remove(KEYS.GPS_LOCATION_LON.key).apply()
    }


    fun saveLocationSource(value: String) {
        saveValueString(KEYS.LOCATION_SOURCE, value)
    }

    fun getLocationSource(defaultValue: String = ""): String {
        return getValueString(KEYS.LOCATION_SOURCE, defaultValue)
    }

    fun saveUnit(value: String) {
        saveValueString(KEYS.UNIT, value)
    }

    fun getUnit(defaultValue: String = ""): String {
        return getValueString(KEYS.UNIT, defaultValue)
    }

    fun saveLanguage(value: String) {
        saveValueString(KEYS.LANGUAGE, value)
    }

    fun getLanguage(defaultValue: String = ""): String {
        return getValueString(KEYS.LANGUAGE, defaultValue)
    }

    fun saveTheme(value: String) {
        saveValueString(KEYS.THEME, value)
    }

    fun getTheme(defaultValue: String = ""): String {
        return getValueString(KEYS.THEME, defaultValue)
    }
}