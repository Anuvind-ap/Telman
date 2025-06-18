package com.example.telman.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.telman.R
import com.example.telman.activities.BotDetailsActivity
import com.example.telman.models.Bot
import com.example.telman.models.BotCommand
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class BotService : Service() {
    private val TAG = "BotService"
    private var timer: Timer? = null
    private val db = FirebaseFirestore.getInstance()
    private var currentBot: Bot? = null
    private var lastUpdateId: Long = 0
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "BotServiceChannel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        try {
            // Get bot data from intent
            val botId = intent?.getStringExtra("botId")
            val botName = intent?.getStringExtra("botName")
            val botUsername = intent?.getStringExtra("botUsername")
            val botToken = intent?.getStringExtra("botToken")

            if (botId != null && botName != null && botUsername != null && botToken != null) {
                Log.d(TAG, "Creating bot from intent data: $botName")
                currentBot = Bot(
                    id = botId,
                    name = botName,
                    username = botUsername,
                    token = botToken
                )
                startForegroundWithNotification(currentBot!!)
                startBotPolling()
            } else {
                Log.e(TAG, "Missing bot data in intent")
                stopSelf()
                return START_NOT_STICKY
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val name = "Bot Service Channel"
                val descriptionText = "Channel for Bot Service notifications"
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel: ${e.message}")
            }
        }
    }

    private fun startForegroundWithNotification(bot: Bot) {
        try {
            val notificationIntent = Intent(this, BotDetailsActivity::class.java).apply {
                putExtra("botId", bot.id)
                putExtra("botName", bot.name)
                putExtra("botUsername", bot.username)
                putExtra("botToken", bot.token)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bot Service Running")
                .setContentText("Listening for commands for ${bot.name}")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Foreground service started for bot: ${bot.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground: ${e.message}")
            stopSelf()
        }
    }

    private fun startBotPolling() {
        try {
            timer?.cancel()
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        checkForNewMessages()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in polling task: ${e.message}")
                    }
                }
            }, 0, 3000) // Check every 3 seconds
            Log.d(TAG, "Bot polling started for bot: ${currentBot?.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting polling: ${e.message}")
            stopSelf()
        }
    }

    private fun checkForNewMessages() {
        currentBot?.let { bot ->
            try {
                val url = "https://api.telegram.org/bot${bot.token}/getUpdates?offset=${lastUpdateId + 1}&limit=1"
                val queue = Volley.newRequestQueue(this)
                val request = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        try {
                            val updates = response.getJSONArray("result")
                            if (updates.length() > 0) {
                                val update = updates.getJSONObject(0)
                                lastUpdateId = update.getLong("update_id")
                                
                                if (update.has("message")) {
                                    val message = update.getJSONObject("message")
                                    if (message.has("text")) {
                                        val text = message.getString("text")
                                        val chatId = message.getJSONObject("chat").getLong("id")
                                        Log.d(TAG, "Received message: $text from chat: $chatId")
                                        processCommand(text, chatId, bot)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing updates: ${e.message}")
                        }
                    },
                    { error ->
                        Log.e(TAG, "Error fetching updates: ${error.message}")
                    }
                )
                queue.add(request)
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkForNewMessages: ${e.message}")
            }
        } ?: run {
            Log.e(TAG, "No bot available for polling")
            stopSelf()
        }
    }

    private fun processCommand(text: String, chatId: Long, bot: Bot) {
        try {
            Log.d(TAG, "Processing command: $text")
            
            // Check if it's a command (starts with /)
            if (text.startsWith("/")) {
                val command = text.substring(1) // Remove the / prefix
                Log.d(TAG, "Processing command: /$command")
                
                // First check for built-in commands
                when (command.lowercase()) {
                    "hello" -> {
                        Log.d(TAG, "Processing built-in /hello command")
                        sendMessage(bot.token, chatId, "Hello from ${bot.name}! 👋")
                        return
                    }
                    "start" -> {
                        Log.d(TAG, "Processing built-in /start command")
                        sendMessage(bot.token, chatId, "Welcome to ${bot.name}! I'm ready to help you.")
                        return
                    }
                    "debug" -> {
                        Log.d(TAG, "Processing debug command")
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            getAllCommands(uid, bot.id) { commands ->
                                val debugMessage = "Bot ID: ${bot.id}\nUser ID: $uid\nAvailable commands: $commands"
                                sendMessage(bot.token, chatId, debugMessage)
                            }
                        } else {
                            sendMessage(bot.token, chatId, "Debug: No user logged in")
                        }
                        return
                    }
                }

                // Then check for custom commands in Firestore
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    Log.d(TAG, "Checking for custom command: $command")
                    Log.d(TAG, "User ID: $uid, Bot ID: ${bot.id}")
                    
                    db.collection("users").document(uid)
                        .collection("bots").document(bot.id)
                        .collection("commands")
                        .document(command)
                        .get()
                        .addOnSuccessListener { document ->
                            Log.d(TAG, "Firestore response for command '$command': exists=${document.exists()}")
                            if (document.exists()) {
                                try {
                                    val commandResponse = document.getString("response")
                                    Log.d(TAG, "Command response: $commandResponse")
                                    if (commandResponse != null) {
                                        Log.d(TAG, "Found custom command response: $commandResponse")
                                        sendMessage(bot.token, chatId, commandResponse)
                                    } else {
                                        Log.e(TAG, "Command response is null")
                                        sendMessage(bot.token, chatId, "Sorry, this command is not configured properly.")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing command response: ${e.message}")
                                    sendMessage(bot.token, chatId, "Sorry, there was an error processing this command.")
                                }
                            } else {
                                Log.d(TAG, "Command not found: $command")
                                // Let's also try to get all commands to debug
                                getAllCommands(uid, bot.id) { commands ->
                                    Log.d(TAG, "Available commands: $commands")
                                    sendMessage(bot.token, chatId, "Sorry, I don't recognize that command. Available commands: $commands")
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching command: ${e.message}")
                            sendMessage(bot.token, chatId, "Sorry, there was an error processing your command.")
                        }
                } else {
                    Log.e(TAG, "No user logged in")
                    sendMessage(bot.token, chatId, "Sorry, there was an authentication error.")
                }
            } else {
                Log.d(TAG, "Message is not a command: $text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing command: ${e.message}")
            try {
                sendMessage(bot.token, chatId, "Sorry, there was an error processing your command.")
            } catch (sendError: Exception) {
                Log.e(TAG, "Error sending error message: ${sendError.message}")
            }
        }
    }

    private fun getAllCommands(uid: String, botId: String, callback: (String) -> Unit) {
        db.collection("users").document(uid)
            .collection("bots").document(botId)
            .collection("commands")
            .get()
            .addOnSuccessListener { documents ->
                val commands = documents.mapNotNull { doc ->
                    doc.getString("command")
                }
                callback(commands.joinToString(", ") { "/$it" })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting all commands: ${e.message}")
                callback("none found")
            }
    }

    private fun sendMessage(token: String, chatId: Long, text: String) {
        try {
            val url = "https://api.telegram.org/bot$token/sendMessage"
            val params = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
            }

            val queue = Volley.newRequestQueue(this)
            val request = JsonObjectRequest(
                Request.Method.POST, url, params,
                { response ->
                    Log.d(TAG, "Message sent successfully: $text")
                },
                { error ->
                    Log.e(TAG, "Error sending message: ${error.message}")
                }
            )
            queue.add(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessage: ${e.message}")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service being destroyed")
        try {
            timer?.cancel()
            timer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy: ${e.message}")
        }
        super.onDestroy()
    }
} 