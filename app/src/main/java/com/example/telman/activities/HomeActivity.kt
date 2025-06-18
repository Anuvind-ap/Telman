package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.telman.LoginActivity
import com.example.telman.R
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var botListButton: Button
    private lateinit var channelListButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        botListButton = findViewById(R.id.botListButton)
        channelListButton = findViewById(R.id.channelListButton)
        logoutButton = findViewById(R.id.logoutButton)

        botListButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        channelListButton.setOnClickListener {
            startActivity(Intent(this, ChannelListActivity::class.java))
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
