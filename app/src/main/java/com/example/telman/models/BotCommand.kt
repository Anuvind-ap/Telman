package com.example.telman.models

import java.io.Serializable

data class BotCommand(
    val command: String = "",
    val response: String = ""
) : Serializable 