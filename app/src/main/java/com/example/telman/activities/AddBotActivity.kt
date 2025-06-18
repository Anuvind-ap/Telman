package com.example.telman.activities


import android.app.DownloadManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import androidx.privacysandbox.tools.core.model.Method
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.telman.R
import androidx.appcompat.app.AppCompatActivity
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
            if (token.isEmpty()) return@setOnClickListener
            fetchBotInfo(token)
            saveBot(name, username, token)


        }
    }
    private fun fetchBotInfo(token: String) {
        val url = "https://api.telegram.org/bot$token/getMe"
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val result = response.getJSONObject("result")
                val name = result.getString("first_name")
                val username = result.getString("username")
                saveBot(name, username, token)
            },
            { Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }


    private fun saveBot(name: String, username: String, token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to name,
            "username" to username,
            "token" to token
        )
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .collection("bots").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Bot added", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}



open class AppCompatActivity {
    fun <Bundle> onCreate(savedInstanceState: Bundle?) {

    }

}
