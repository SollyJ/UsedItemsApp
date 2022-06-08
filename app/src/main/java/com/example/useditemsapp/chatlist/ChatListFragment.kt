package com.example.useditemsapp.chatlist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.useditemsapp.DBKey
import com.example.useditemsapp.DBKey.Companion.CHILD_CHAT
import com.example.useditemsapp.DBKey.Companion.DB_USERS
import com.example.useditemsapp.R
import com.example.useditemsapp.chatdetail.ChatRoomActivity
import com.example.useditemsapp.databinding.FragmentChatlistBinding
import com.example.useditemsapp.databinding.FragmentHomeBinding
import com.example.useditemsapp.home.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatListFragment:Fragment(R.layout.fragment_chatlist) {

    private lateinit var chatDB: DatabaseReference
    private lateinit var chatListAdapter: ChatListAdapter
    private var binding: FragmentChatlistBinding? = null
    private val chatRoomList = mutableListOf<ChatListItem>()

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 초기화(바인딩)
        val fragmentChatListBinding = FragmentChatlistBinding.bind(view)
        binding = fragmentChatListBinding

        chatListAdapter = ChatListAdapter(onItemClicked = { chatRoom ->
            context?.let {
                // 채팅방으로 이동
                val intent = Intent(it, ChatRoomActivity::class.java)
                intent.putExtra("chatKey", chatRoom.key)
                startActivity(intent)
            }
        })

        chatRoomList.clear()
        fragmentChatListBinding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentChatListBinding.chatListRecyclerView.adapter = chatListAdapter

        // chatDB 초기화
        chatDB = Firebase.database.reference.child(DB_USERS).child(auth.currentUser!!.uid).child(CHILD_CHAT)
        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return
                    chatRoomList.add(model)
                }
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}

        })

        if(auth.currentUser == null) {   // 로그인이 되어있지 않을때 예외처리
            return
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        chatListAdapter.notifyDataSetChanged()
    }
}