package com.example.cloudnotify.data.local.sharedPrefrence
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesManager(context: Context) {

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

    // Generic method to save a long value
    fun saveValue(key: KEYS, value: Long) {
        sharedPreferences.edit().putString(key.key, value.toString()).apply()
    }
    // Generic method to get a long value
    fun getValue(key: KEYS, defaultValue: Long = 0L): Long {
        return sharedPreferences.getString(key.key, defaultValue.toString())?.toLong() ?: defaultValue
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
    fun saveGpsLocationLat(lat: Long) {
        saveValue(KEYS.GPS_LOCATION_LAT, lat)
    }
    // get GPS latitude
    fun getGpsLocationLat(): Long {
        return getValue(KEYS.GPS_LOCATION_LAT, 0L)
    }
    //save GPS longitude
    fun saveGpsLocationLong(long: Long) {
        saveValue(KEYS.GPS_LOCATION_LON, long)
    }
    // get GPS longitude
    fun getGpsLocationLong(): Long {
        return getValue(KEYS.GPS_LOCATION_LON, 0L)
    }
    // save map latitude
    fun saveMapLocationLat(lat: Long) {
        saveValue(KEYS.MAP_LOCATION_LAT, lat)
    }
    // get map latitude
    fun getMapLocationLat(): Long {
        return getValue(KEYS.MAP_LOCATION_LAT, 0L)
    }
    // save map longitude
    fun saveMapLocationLong(long: Long) {
        saveValue(KEYS.MAP_LOCATION_LON, long)
    }
    // get map longitude
    fun getMapLocationLong(): Long {
        return getValue(KEYS.MAP_LOCATION_LON, 0L)
    }
    //save search latitude
    fun saveSearchLocationLat(lat: Long) {
        saveValue(KEYS.SEARCH_LOCATION_LAT, lat)
    }
    // get search latitude
    fun getSearchLocationLat(): Long {
        return getValue(KEYS.SEARCH_LOCATION_LAT, 0L)
    }
    // save search longitude
    fun saveSearchLocationLong(long: Long) {
        saveValue(KEYS.SEARCH_LOCATION_LON, long)
    }
    // get search longitude
    fun getSearchLocationLong(): Long {
        return getValue(KEYS.SEARCH_LOCATION_LON, 0L)
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