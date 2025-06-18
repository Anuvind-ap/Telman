package com.example.telman.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.adapter.ChannelAdapter
import com.example.telman.models.Channel
import com.example.telman.R

class ChannelListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    // Dummy channel list (replace with real data when needed)
    private val channels = listOf(
        Channel("1", "My Channel", 1234),
        Channel("2", "Tech News", 2567),
        Channel("3", "Bot Announcements", 980)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        recyclerView = findViewById(R.id.channelRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChannelAdapter(channels)
    }

}