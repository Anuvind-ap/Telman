package com.example.telman.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.telman.R

class ChatStatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_stats)

        val statsText: TextView = findViewById(R.id.chatStatsText)
        statsText.text = "Stats will be displayed here (mock data)."
    }

}