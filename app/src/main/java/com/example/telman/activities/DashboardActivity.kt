package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.adapters.BotAdapter
import com.example.telman.models.Bot
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var addBotFab: FloatingActionButton
    private lateinit var botAdapter: BotAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Bots"

        // Initialize views
        recyclerView = findViewById(R.id.botsRecyclerView)
        addBotFab = findViewById(R.id.addBotFab)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        botAdapter = BotAdapter { bot ->
            val intent = Intent(this, BotDetailsActivity::class.java)
            intent.putExtra("botId", bot.id)
            intent.putExtra("botName", bot.name)
            intent.putExtra("botUsername", bot.username)
            intent.putExtra("botToken", bot.token)
            startActivity(intent)
        }
        recyclerView.adapter = botAdapter

        // Set up FAB
        addBotFab.setOnClickListener {
            startActivity(Intent(this, AddBotActivity::class.java))
        }

        // Load bots
        loadBots()
    }

    private fun loadBots() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("bots")
            .get()
            .addOnSuccessListener { documents ->
                val bots = documents.mapNotNull { doc ->
                    try {
                        Bot(
                            id = doc.id,
                            name = doc.getString("name") ?: return@mapNotNull null,
                            username = doc.getString("username") ?: return@mapNotNull null,
                            token = doc.getString("token") ?: return@mapNotNull null
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                botAdapter.submitList(bots)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading bots: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadBots()
    }
}
