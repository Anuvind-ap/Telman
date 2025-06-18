package com.example.telman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.models.BotCommand

class CommandAdapter(
    private val onDelete: (BotCommand) -> Unit,
    private val onEdit: (BotCommand) -> Unit
) : ListAdapter<BotCommand, CommandAdapter.CommandViewHolder>(CommandDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_command, parent, false)
        return CommandViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commandText: TextView = itemView.findViewById(R.id.commandText)
        private val responseText: TextView = itemView.findViewById(R.id.responseText)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(command: BotCommand) {
            commandText.text = "/${command.command}"
            responseText.text = command.response

            editButton.setOnClickListener {
                onEdit(command)
            }

            deleteButton.setOnClickListener {
                onDelete(command)
            }
        }
    }

    private class CommandDiffCallback : DiffUtil.ItemCallback<BotCommand>() {
        override fun areItemsTheSame(oldItem: BotCommand, newItem: BotCommand): Boolean {
            return oldItem.command == newItem.command
        }

        override fun areContentsTheSame(oldItem: BotCommand, newItem: BotCommand): Boolean {
            return oldItem == newItem
        }
    }
} 