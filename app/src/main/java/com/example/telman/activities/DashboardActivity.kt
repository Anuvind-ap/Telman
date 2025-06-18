package com.example.telman.activities


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.models.Bot
import com.example.telman.adapter.BotAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BotAdapter
    private lateinit var fab: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()
    private val bots = mutableListOf<Bot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        recyclerView = findViewById(R.id.botRecyclerView)
        fab = findViewById(R.id.addBotFab)

        adapter = BotAdapter(bots) { bot -> onDeleteBot(bot) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(this, AddBotActivity::class.java))
        }

        loadBots()
    }

    private fun loadBots() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("bots")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                bots.clear()
                snapshot?.documents?.forEach { doc ->
                    val bot = doc.toObject(Bot::class.java)
                    if (bot != null) {
                        bots.add(bot)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun onDeleteBot(bot: Bot) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).collection("bots").document(bot.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Bot deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete bot: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
