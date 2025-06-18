package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.adapters.ChannelAdapter
import com.example.telman.models.Channel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChannelListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelAdapter
    private lateinit var addChannelFab: FloatingActionButton
    private val channels = mutableListOf<Channel>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        recyclerView = findViewById(R.id.channelRecycler)
        addChannelFab = findViewById(R.id.addChannelFab)

        adapter = ChannelAdapter(channels) { channel ->
            val intent = Intent(this, ChannelActionActivity::class.java)
            intent.putExtra("channelUsername", channel.username)
            intent.putExtra("channelTitle", channel.title)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addChannelFab.setOnClickListener {
            startActivity(Intent(this, AddChannelActivity::class.java))
        }

        loadChannels()
    }

    private fun loadChannels() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .collection("channels")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading channels: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                channels.clear()
                snapshot?.forEach { doc ->
                    val channel = doc.toObject(Channel::class.java)
                    channels.add(channel)
                }
                adapter.notifyDataSetChanged()
            }
    }
}
