package com.example.telman.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.telman.models.Channel
import com.example.telman.R

class ChannelAdapter(private val channels: List<Channel>) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {
    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.channelTitle)
        val members: TextView = view.findViewById(R.id.channelMembers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.title.text = channel.title
        holder.members.text = "${channel.membersCount} members"
    }

    override fun getItemCount() = channels.size
}
