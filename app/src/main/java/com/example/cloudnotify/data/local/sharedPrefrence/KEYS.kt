package com.example.cloudnotify.data.local.sharedPrefrence

enum class KEYS(val key: String) {
    CLOUD_NOTIFY("cloud_notify"),
    GPS_LOCATION_LAT("gps_location_lat"),
    GPS_LOCATION_LON("gps_location_lon"),
    MAP_LOCATION_LAT("map_location_lat"),
    MAP_LOCATION_LON("map_location_lon"),
    SEARCH_LOCATION_LAT("search_location_lat"),
    SEARCH_LOCATION_LON("search_location_lon"),
    LOCATION_SOURCE("location_source"),
    UNIT("unit"),
    LANGUAGE("language"),
    THEME("theme");
}
