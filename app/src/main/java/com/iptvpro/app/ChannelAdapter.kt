package com.iptvpro.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onFavoriteClick: (Channel) -> Unit,
    private val isFav: (Channel) -> Boolean
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(DiffCallback()) {

    inner class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo:    ImageView = view.findViewById(R.id.imgLogo)
        val name:    TextView  = view.findViewById(R.id.tvName)
        val group:   TextView  = view.findViewById(R.id.tvGroup)
        val favBtn:  ImageView = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = getItem(position)
        holder.name.text  = channel.name
        holder.group.text = channel.group

        Glide.with(holder.logo.context)
            .load(channel.logo)
            .placeholder(R.drawable.ic_tv_placeholder)
            .error(R.drawable.ic_tv_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.logo)

        // Favorite icon state
        holder.favBtn.setImageResource(
            if (isFav(channel)) R.drawable.ic_star_filled else R.drawable.ic_star
        )

        holder.itemView.setOnClickListener { onChannelClick(channel) }
        holder.favBtn.setOnClickListener {
            onFavoriteClick(channel)
            notifyItemChanged(position)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(a: Channel, b: Channel) = a.id == b.id
        override fun areContentsTheSame(a: Channel, b: Channel) = a == b
    }
}
