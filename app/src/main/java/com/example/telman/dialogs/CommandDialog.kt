package com.example.telman.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.telman.R
import com.example.telman.models.BotCommand

class CommandDialog(
    private val context: Context,
    private val existingCommand: BotCommand? = null,
    private val onSave: (command: String, response: String) -> Unit
) {
    private lateinit var commandEditText: EditText
    private lateinit var responseEditText: EditText

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_command, null)
        commandEditText = dialogView.findViewById(R.id.commandEditText)
        responseEditText = dialogView.findViewById(R.id.responseEditText)

        // Pre-fill fields if editing existing command
        existingCommand?.let {
            commandEditText.setText(it.command)
            responseEditText.setText(it.response)
            commandEditText.isEnabled = false // Don't allow editing command name
        }

        AlertDialog.Builder(context)
            .setTitle(if (existingCommand == null) "Add Command" else "Edit Command")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val command = commandEditText.text.toString().trim()
                val response = responseEditText.text.toString().trim()
                if (command.isNotEmpty() && response.isNotEmpty()) {
                    onSave(command, response)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 