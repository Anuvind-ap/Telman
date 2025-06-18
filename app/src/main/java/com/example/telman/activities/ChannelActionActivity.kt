package com.example.telman.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.telman.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class ChannelActionActivity : AppCompatActivity() {

    private lateinit var botListView: ListView
    private lateinit var commandListView: ListView
    private lateinit var statusText: TextView

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private lateinit var channelUsername: String
    private lateinit var botMap: MutableMap<String, String> // name -> token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_action)

        channelUsername = intent.getStringExtra("channelUsername") ?: return

        botListView = findViewById(R.id.botList)
        commandListView = findViewById(R.id.commandList)
        statusText = findViewById(R.id.statusText)

        botMap = mutableMapOf()

        loadBots()
    }

    private fun loadBots() {
        db.collection("users").document(uid).collection("bots")
            .get()
            .addOnSuccessListener { snapshot ->
                val botNames = mutableListOf<String>()
                snapshot.documents.forEach { doc ->
                    val name = doc.getString("name") ?: "Unnamed"
                    val token = doc.getString("token") ?: return@forEach
                    botNames.add(name)
                    botMap[name] = token
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, botNames)
                botListView.adapter = adapter

                botListView.setOnItemClickListener { _, _, position, _ ->
                    val selectedBotName = botNames[position]
                    val token = botMap[selectedBotName] ?: return@setOnItemClickListener
                    fetchCommands(token)
                }
            }
    }

    private fun fetchCommands(token: String) {
        statusText.text = "Fetching commands..."
        val url = "https://api.telegram.org/bot$token/getMyCommands"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val commandsJson = response.getJSONArray("result")
                    val commandList = mutableListOf<String>()
                    for (i in 0 until commandsJson.length()) {
                        val obj = commandsJson.getJSONObject(i)
                        commandList.add(obj.getString("command"))
                    }

                    statusText.text = "Select a command"
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commandList)
                    commandListView.adapter = adapter

                    commandListView.setOnItemClickListener { _, _, position, _ ->
                        val command = commandList[position]
                        sendCommandToChannel(token, command)
                    }

                } catch (e: Exception) {
                    statusText.text = "Failed to parse commands."
                }
            },
            {
                statusText.text = "Failed to fetch commands. Bot might not be admin or token is wrong."
            }
        )
        queue.add(request)
    }

    private fun sendCommandToChannel(token: String, command: String) {
        val url = "https://api.telegram.org/bot$token/sendMessage"
        val payload = JSONObject().apply {
            put("chat_id", "@$channelUsername")
            put("text", "/$command")
        }

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.POST, url, payload,
            {
                statusText.text = "Command /$command sent successfully!"
            },
            {
                statusText.text = "Error sending command. Possibly wrong bot or missing admin permissions."
            }
        )
        queue.add(request)
    }
}
