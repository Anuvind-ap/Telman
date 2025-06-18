package com.example.telman.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bot(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val token: String = ""
) : Parcelable
