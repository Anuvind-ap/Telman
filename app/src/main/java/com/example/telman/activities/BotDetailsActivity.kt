package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.telman.R
import com.example.telman.models.Bot
import com.example.telman.services.BotService
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class BotDetailsActivity : AppCompatActivity() {
    private lateinit var bot: Bot
    private lateinit var botNameText: TextView
    private lateinit var botUsernameText: TextView
    private lateinit var botTokenText: TextView
    private lateinit var startStopButton: Button
    private lateinit var manageCommandsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_details)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Bot Details"

        // Initialize views
        botNameText = findViewById(R.id.botNameText)
        botUsernameText = findViewById(R.id.botUsernameText)
        botTokenText = findViewById(R.id.botTokenText)
        startStopButton = findViewById(R.id.startStopButton)
        manageCommandsButton = findViewById(R.id.manageCommandsButton)

        // Get bot from intent
        try {
            val botId = intent.getStringExtra("botId")
            val botName = intent.getStringExtra("botName")
            val botUsername = intent.getStringExtra("botUsername")
            val botToken = intent.getStringExtra("botToken")

            if (botId == null || botName == null || botUsername == null || botToken == null) {
                throw Exception("Missing bot data")
            }

            bot = Bot(
                id = botId,
                name = botName,
                username = botUsername,
                token = botToken
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Invalid bot data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Display bot details
        botNameText.text = bot.name
        botUsernameText.text = "@${bot.username}"
        botTokenText.text = bot.token

        // Set up start/stop button
        updateStartStopButton()
        startStopButton.setOnClickListener {
            if (isServiceRunning()) {
                stopBotService()
            } else {
                startBotService()
            }
        }

        // Set up manage commands button
        manageCommandsButton.setOnClickListener {
            val intent = Intent(this, ManageCommandsActivity::class.java)
            intent.putExtra("botId", bot.id)
            intent.putExtra("botName", bot.name)
            intent.putExtra("botUsername", bot.username)
            intent.putExtra("botToken", bot.token)
            startActivity(intent)
        }
    }

    private fun updateStartStopButton() {
        startStopButton.text = if (isServiceRunning()) "Stop Bot" else "Start Bot"
    }

    private fun isServiceRunning(): Boolean {
        // Check if service is running
        return false // TODO: Implement service running check
    }

    private fun startBotService() {
        try {
            val serviceIntent = Intent(this, BotService::class.java)
            serviceIntent.putExtra("botId", bot.id)
            serviceIntent.putExtra("botName", bot.name)
            serviceIntent.putExtra("botUsername", bot.username)
            serviceIntent.putExtra("botToken", bot.token)
            startService(serviceIntent)
            updateStartStopButton()
            Toast.makeText(this, "Bot service started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting bot service: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopBotService() {
        try {
            val serviceIntent = Intent(this, BotService::class.java)
            stopService(serviceIntent)
            updateStartStopButton()
            Toast.makeText(this, "Bot service stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping bot service: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun setMyCommands(botToken: String, commands: List<Pair<String, String>>) {
        val url = "https://api.telegram.org/bot$botToken/setMyCommands"
        val commandList = commands.map { mapOf("command" to it.first, "description" to it.second) }
        val params = JSONObject().apply {
            put("commands", JSONArray(commandList))
        }
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response -> Log.d("BotService", "setMyCommands response: $response") },
            { error -> Log.e("BotService", "setMyCommands error: ${error.message}") }
        )
        queue.add(request)
    }
} 