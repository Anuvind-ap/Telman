package com.example.telman.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.R
import com.example.telman.activities.BotDetailsActivity
import com.example.telman.models.Bot

class BotAdapter(private val bots: List<Bot>, private val onDelete: (Bot) -> Unit) : RecyclerView.Adapter<BotAdapter.BotViewHolder>() {

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.botName)
        val username: TextView = view.findViewById(R.id.botUsername)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bot, parent, false)
        return BotViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotViewHolder, position: Int) {
        val bot = bots[position]
        holder.name.text = bot.name
        holder.username.text = "@${bot.username}"
        holder.deleteBtn.setOnClickListener {
            onDelete(bot)
        }
        
        // Add click listener to the entire item
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BotDetailsActivity::class.java).apply {
                putExtra("bot", bot)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = bots.size
}
