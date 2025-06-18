package com.example.telman.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.telman.R
import com.example.telman.models.Bot

class BotDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_details)

        // Get the bot data from the intent
        val bot = intent.getParcelableExtra<Bot>("bot")
        
        if (bot != null) {
            // Set the bot details in the UI
            findViewById<TextView>(R.id.botNameText).text = bot.name
            findViewById<TextView>(R.id.botUsernameText).text = "@${bot.username}"
            findViewById<TextView>(R.id.botTokenText).text = bot.token
        }
    }
} 