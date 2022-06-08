package com.example.useditemsapp

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.useditemsapp.chatlist.ChatListFragment
import com.example.useditemsapp.home.HomeFragment
import com.example.useditemsapp.mypage.MyPageFragment
import com.facebook.CallbackManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : FragmentActivity() {

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val homeFragment = HomeFragment()
        val chatListFragment = ChatListFragment()
        val myPageFragment = MyPageFragment()

        replaceFragment(homeFragment)   // 초기엔 홈화면 보이게

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.chatList ->
                    if(auth.currentUser == null) {   // 로그인 안돼있을때
                        Toast.makeText(this, "로그인을 해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        replaceFragment(chatListFragment)
                    }
                R.id.myPage -> replaceFragment(myPageFragment)
            }
            true
        }
    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()   // supportFragmentManager: fragment를 관리하는 함수
            .apply {
                replace(R.id.fragmentContainer, fragment)   // fragmentContainer에 있는 fragment를 인자로 받아온 fragment로 교체하겠다.
                commit()
            }
    }

}