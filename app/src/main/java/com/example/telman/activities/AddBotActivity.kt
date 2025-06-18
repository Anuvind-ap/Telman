package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.telman.R
import com.example.telman.models.Bot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddBotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bot)
        val nameInput = findViewById<EditText>(R.id.botNameInput)
        val usernameInput = findViewById<EditText>(R.id.botUsernameInput)
        val tokenInput = findViewById<EditText>(R.id.botTokenInput)
        val addButton = findViewById<Button>(R.id.saveBotButton)

        addButton.setOnClickListener {
            val token = tokenInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            
            if (token.isEmpty()) {
                Toast.makeText(this, "Please enter a bot token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (name.isEmpty() || username.isEmpty()) {
                // If name or username is empty, fetch from Telegram API
                fetchBotInfo(token)
            } else {
                // If all fields are filled, save directly
                saveBot(name, username, token)
            }
        }
    }

    private fun fetchBotInfo(token: String) {
        val url = "https://api.telegram.org/bot$token/getMe"
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val result = response.getJSONObject("result")
                    val name = result.getString("first_name")
                    val username = result.getString("username")
                    saveBot(name, username, token)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing bot info: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Invalid token: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    private fun saveBot(name: String, username: String, token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val botRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("bots")
            .document() // This creates a new document with auto-generated ID

        val bot = Bot(
            id = botRef.id,
            name = name,
            username = username,
            token = token
        )

        botRef.set(bot)
            .addOnSuccessListener {
                Toast.makeText(this, "Bot added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add bot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
