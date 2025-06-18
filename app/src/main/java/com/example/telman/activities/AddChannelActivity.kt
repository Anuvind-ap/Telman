package com.example.telman.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.telman.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddChannelActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_channel)

        titleInput = findViewById(R.id.channelTitleInput)
        usernameInput = findViewById(R.id.channelUsernameInput)
        saveButton = findViewById(R.id.saveChannelButton)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val username = usernameInput.text.toString().trim().removePrefix("@")

            if (title.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Enter channel title and username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "title" to title,
                "username" to username
            )

            db.collection("users").document(uid).collection("channels")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Channel saved", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ChannelListActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
