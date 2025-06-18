package com.example.telman.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.models.Bot

class BotAdapter(private val onBotClick: (Bot) -> Unit) : ListAdapter<Bot, BotAdapter.BotViewHolder>(BotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bot, parent, false)
        return BotViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotViewHolder, position: Int) {
        val bot = getItem(position)
        holder.bind(bot)
    }

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.botNameTextView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.botUsernameTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBotClick(getItem(position))
                }
            }
        }

        fun bind(bot: Bot) {
            nameTextView.text = bot.name
            usernameTextView.text = "@${bot.username}"
        }
    }

    private class BotDiffCallback : DiffUtil.ItemCallback<Bot>() {
        override fun areItemsTheSame(oldItem: Bot, newItem: Bot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bot, newItem: Bot): Boolean {
            return oldItem == newItem
        }
    }
} 