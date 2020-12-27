package com.example.myapplication.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var id:String, var name:String, var email:String, var gender:String, var age: String,
                var password:String, var image:String):Parcelable