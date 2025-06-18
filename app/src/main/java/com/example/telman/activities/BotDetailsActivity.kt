package com.example.telman.activities

import android.content.ContentValues.TAG
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot

class BotDetailsActivity : AppCompatActivity() {
    private lateinit var bot: Bot
    private lateinit var botNameText: TextView
    private lateinit var botUsernameText: TextView
    private lateinit var botTokenText: TextView
    private lateinit var startStopButton: Button
    private lateinit var manageCommandsButton: Button
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

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

    private fun processCommand(text: String, chatId: Long, bot: Bot) {
        try {
            Log.d(TAG, "Processing command: $text")

            if (text.startsWith("/")) {
                val command = text.substring(1).trim().lowercase()
                Log.d(TAG, "Processing command: /$command")

                when (command) {
                    "hello" -> {
                        sendMessage(bot.token, chatId, "Hello from ${bot.name}! 👋")
                        return
                    }
                    "start" -> {
                        sendMessage(bot.token, chatId, "Welcome to ${bot.name}! I'm ready to help you.")
                        return
                    }
                    "debug" -> {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid == null) {
                            Log.e(TAG, "No user logged in for /debug")
                            sendMessage(bot.token, chatId, "Debug: No user logged in")
                            return
                        }
                        getAllCommands(uid, bot.id) { commands ->
                            val debugMessage = "Bot ID: ${bot.id}\nUser ID: $uid\nAvailable commands: $commands"
                            sendMessage(bot.token, chatId, debugMessage)
                        }
                        return
                    }
                }

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Log.e(TAG, "No user logged in for custom command")
                    sendMessage(bot.token, chatId, "Error: No user logged in")
                    return
                }

                db.collection("users").document(uid)
                    .collection("bots").document(bot.id)
                    .collection("commands")
                    .document(command)
                    .get()
                    .addOnSuccessListener { doc: DocumentSnapshot ->
                        if (doc.exists()) {
                            val commandResponse = doc.getString("response")
                            if (commandResponse != null) {
                                sendMessage(bot.token, chatId, commandResponse)
                            } else {
                                sendMessage(bot.token, chatId, "Sorry, this command is not configured properly.")
                            }
                        } else {
                            sendMessage(bot.token, chatId, "Sorry, I don't recognize that command.")
                        }
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.e(TAG, "Error fetching command: ${e.message}")
                        sendMessage(bot.token, chatId, "Error fetching command: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing command: ${e.message}")
            sendMessage(bot.token, chatId, "Error processing command: ${e.message}")
        }
    }

    private fun sendMessage(token: String, chatId: Long, text: String) {
        try {
            val url = "https://api.telegram.org/bot$token/sendMessage"
            val params = org.json.JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
            }

            val queue = com.android.volley.toolbox.Volley.newRequestQueue(this)
            val request = com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.POST, url, params,
                { response -> android.util.Log.d(TAG, "Message sent successfully: $text") },
                { error -> android.util.Log.e(TAG, "Error sending message: ${error.message}") }
            )
            queue.add(request)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in sendMessage: ${e.message}")
        }
    }

    private fun getAllCommands(uid: String, botId: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid)
            .collection("bots").document(botId)
            .collection("commands")
            .get()
            .addOnSuccessListener { documents: QuerySnapshot ->
                val commands = documents.mapNotNull { doc: QueryDocumentSnapshot ->
                    doc.getString("command")
                }
                callback(commands.joinToString(", ") { "/$it" })
            }
            .addOnFailureListener { e: Exception ->
                android.util.Log.e(TAG, "Error getting all commands: ${e.message}")
                callback("none found")
            }
    }
} 