package com.example.cloudnotify.data.model.local

import android.os.Parcel
import android.os.Parcelable

data class LocalLocation(
    var lat: Double = 0.0,
    var lon: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalLocation> {
        override fun createFromParcel(parcel: Parcel): LocalLocation {
            return LocalLocation(parcel)
        }

        override fun newArray(size: Int): Array<LocalLocation?> {
            return arrayOfNulls(size)
        }
    }
}