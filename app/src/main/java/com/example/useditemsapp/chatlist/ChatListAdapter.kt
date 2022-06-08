package com.example.useditemsapp.chatlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.useditemsapp.databinding.ItemArticleBinding
import com.example.useditemsapp.databinding.ItemChatListBinding
import com.example.useditemsapp.home.ArticleAdapter
import com.example.useditemsapp.home.ArticleModel
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter (val onItemClicked: (ChatListItem) -> (Unit)): ListAdapter<ChatListItem, ChatListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemChatListBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(chatListItem: ChatListItem) {
            if(chatListItem.imageURL.isNotEmpty()) {
                Glide.with(binding.itemImageView)
                    .load(chatListItem.imageURL)
                    .into(binding.itemImageView)
            }
            binding.chatRoomTitleTextView.text = chatListItem.itemTitle
            //binding.lastMsgTextView.text = chatListItem.msg

            binding.root.setOnClickListener {
                onItemClicked(chatListItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}