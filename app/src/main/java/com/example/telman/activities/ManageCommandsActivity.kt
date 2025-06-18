package com.example.telman.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.adapters.CommandAdapter
import com.example.telman.dialogs.CommandDialog
import com.example.telman.models.Bot
import com.example.telman.models.BotCommand
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class ManageCommandsActivity : AppCompatActivity() {
    private lateinit var bot: Bot
    private lateinit var commandAdapter: CommandAdapter
    private lateinit var commandsRecyclerView: RecyclerView
    private lateinit var addCommandFab: FloatingActionButton
    private lateinit var testButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_commands)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Commands"

        // Initialize views
        commandsRecyclerView = findViewById(R.id.commandsRecyclerView)
        addCommandFab = findViewById(R.id.addCommandFab)
        testButton = findViewById(R.id.testButton)

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

        setupRecyclerView()
        setupAddCommandButton()
        setupTestButton()
        loadCommands()
    }

    private fun setupRecyclerView() {
        commandAdapter = CommandAdapter(
            onDelete = { command: BotCommand -> deleteCommand(command) },
            onEdit = { command: BotCommand -> showEditCommandDialog(command) }
        )
        commandsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ManageCommandsActivity)
            adapter = commandAdapter
        }
    }

    private fun setupAddCommandButton() {
        addCommandFab.setOnClickListener {
            showAddCommandDialog()
        }
    }

    private fun setupTestButton() {
        testButton.setOnClickListener {
            testCommands()
        }
    }

    private fun testCommands() {
        val uid = auth.currentUser?.uid ?: return
        Toast.makeText(this, "Testing commands...", Toast.LENGTH_SHORT).show()
        
        db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .get()
            .addOnSuccessListener { documents ->
                val commandCount = documents.size()
                val commands = documents.mapNotNull { doc ->
                    try {
                        val command = doc.getString("command")
                        val response = doc.getString("response")
                        "$command -> $response"
                    } catch (e: Exception) {
                        null
                    }
                }
                
                val message = "Found $commandCount commands:\n${commands.joinToString("\n")}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error testing commands: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCommands() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .get()
            .addOnSuccessListener { documents ->
                val commands = documents.mapNotNull { doc ->
                    try {
                        BotCommand(
                            command = doc.getString("command") ?: return@mapNotNull null,
                            response = doc.getString("response") ?: return@mapNotNull null
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                commandAdapter.submitList(commands)
                Toast.makeText(this, "Loaded ${commands.size} commands", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading commands: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddCommandDialog() {
        val dialog = CommandDialog(this) { command, response ->
            saveCommand(command, response)
        }
        dialog.show()
    }

    private fun showEditCommandDialog(command: BotCommand) {
        val dialog = CommandDialog(this, command) { _, response ->
            updateCommand(command.command, response)
        }
        dialog.show()
    }

    private fun updateTelegramCommands() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .get()
            .addOnSuccessListener { documents ->
                val commands = documents.mapNotNull { doc ->
                    val command = doc.getString("command") ?: return@mapNotNull null
                    val response = doc.getString("response") ?: "No description"
                    mapOf("command" to command, "description" to response.take(50))
                }
                val url = "https://api.telegram.org/bot${bot.token}/setMyCommands"
                val params = org.json.JSONObject().apply {
                    put("commands", JSONArray(commands))
                }
                val queue = Volley.newRequestQueue(this)
                val request = JsonObjectRequest(
                    Request.Method.POST, url, params,
                    { Toast.makeText(this, "Telegram commands updated", Toast.LENGTH_SHORT).show() },
                    { error -> Toast.makeText(this, "Failed to update Telegram commands: ${error.message}", Toast.LENGTH_SHORT).show() }
                )
                queue.add(request)
            }
    }

    private fun saveCommand(command: String, response: String) {
        val uid = auth.currentUser?.uid ?: return
        val newCommand = BotCommand(
            command = command,
            response = response
        )

        db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .document(command)
            .set(newCommand)
            .addOnSuccessListener {
                Toast.makeText(this, "Command added successfully", Toast.LENGTH_SHORT).show()
                loadCommands()
                updateTelegramCommands()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding command: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCommand(command: String, response: String) {
        val uid = auth.currentUser?.uid ?: return
        val commandRef = db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .document(command)

        val updates = hashMapOf<String, Any>(
            "response" to response
        )

        commandRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Command updated successfully", Toast.LENGTH_SHORT).show()
                loadCommands()
                updateTelegramCommands()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating command: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCommand(command: BotCommand) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("bots").document(bot.id)
            .collection("commands")
            .document(command.command)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Command deleted successfully", Toast.LENGTH_SHORT).show()
                loadCommands()
                updateTelegramCommands()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting command: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 