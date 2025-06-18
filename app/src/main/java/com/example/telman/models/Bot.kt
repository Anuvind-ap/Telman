package com.example.telman.models

import java.io.Serializable

data class Bot(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val token: String = ""
) : Serializable
