package com.example.useditemsapp.chatdetail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.useditemsapp.DBKey.Companion.DB_CHAT
import com.example.useditemsapp.DBKey.Companion.DB_USERS
import com.example.useditemsapp.R
import com.example.useditemsapp.chatlist.ChatListAdapter
import com.example.useditemsapp.chatlist.ChatListItem
import com.example.useditemsapp.databinding.FragmentChatlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatRoomActivity:AppCompatActivity() {
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private lateinit var chatDB: DatabaseReference   // ChatListFragment의 chatDB와 다른것
    private val chatting = mutableListOf<ChatItem>()

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val chatRecyclerView: RecyclerView by lazy {
        findViewById(R.id.chatRecyclerView)
    }
    private val chattingEditText: EditText by lazy {
        findViewById(R.id.chattingEditText)
    }
    private val sendButton: Button by lazy {
        findViewById(R.id.sendButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        // 리사이클러뷰 초기화
        chatRoomAdapter = ChatRoomAdapter()
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatRoomAdapter

        // chatDB 초기화
        val chatKey = intent.getLongExtra("chatKey", -1).toString()
        chatDB = Firebase.database.reference.child(DB_CHAT).child(chatKey)

        // 전송 버튼 눌렀을때
        sendButton.setOnClickListener {

            val chatItem = ChatItem(
                sendID = auth.currentUser!!.uid,
                message = chattingEditText.text.toString()
            )

            chatDB.push().setValue(chatItem)
            chattingEditText.text.clear()
        }

        chatDB.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem ?: return

                chatting.add(chatItem)
                chatRoomAdapter.submitList(chatting)
                chatRoomAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}