package com.example.useditemsapp.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.useditemsapp.DBKey.Companion.CHILD_CHAT
import com.example.useditemsapp.DBKey.Companion.DB_ARTICLES
import com.example.useditemsapp.DBKey.Companion.DB_USERS
import com.example.useditemsapp.R
import com.example.useditemsapp.chatlist.ChatListItem
import com.example.useditemsapp.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment: Fragment(R.layout.fragment_home) {

    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter

    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object: ChildEventListener {   // 등록돼있는 상품목록 보여주는 리스너를 전역에 선언
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)  // DB에 모델을 쓰기위해선 빈생성자(constructor)있어야함
            articleModel ?: return   // null 예외처리

            articleList.add(articleModel)   // null이 아니라면 articleModel에 추가
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}

    }
    
    private var binding: FragmentHomeBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프래그먼트 바인딩
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        // articleDB 초기화
        articleList.clear()   // 뷰가 재사용 될때마다 클리어 해줘야한다. (안그러면 중복)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)

        // userDB 초기화
        userDB = Firebase.database.reference.child(DB_USERS)

        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->
            if(auth.currentUser != null) {   // 로그인 한 상태
                if(auth.currentUser!!.uid != articleModel.sellerID) {   // 클릭인과 판매인이 다를때
                    val chatRoom = ChatListItem(
                        buyerID = auth.currentUser!!.uid,
                        sellerID = articleModel.sellerID,
                        itemTitle = articleModel.title,
                        imageURL = articleModel.imageURL,
                        key = "${System.currentTimeMillis()}"
                    )

                    // 채팅방 생성
                    userDB.child(auth.currentUser!!.uid)   // 내 DB에 추가
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(articleModel.sellerID)   // 셀러 DB에 추가
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(view, "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요.", Snackbar.LENGTH_SHORT)
                        .show()

                }
                else {   // 판매인과 클릭인이 같을때
                    Snackbar.make(view, "내가 올린 게시물 입니다!!", Snackbar.LENGTH_SHORT).show()
                }
            }
            else {   // 로그인 안 한 상태
                Snackbar.make(view, "로그인을 해주세요.", Snackbar.LENGTH_SHORT).show()
            }
        })

        // 리사이클러뷰 초기화(레이아웃 매니저, 어댑터 연결)
        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)   // fragment는 context가 될수없음
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        // +버튼 초기화
        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            if(auth.currentUser != null) {
                val intent = Intent(requireContext(), AddArticleActivity::class.java)
                startActivity(intent)
            }
            else {
                Snackbar.make(view, "로그인 후 사용해주세요.", Snackbar.LENGTH_SHORT).show()
            }
        }

        articleDB.addChildEventListener(listener)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {   // 뷰가 재개되면
        super.onResume()
        articleAdapter.notifyDataSetChanged()   // 변경된 데이터의 셋팅을 보여준다.
    }

    override fun onDestroyView() {   // 뷰가 없어지면
        super.onDestroyView()
        articleDB.removeEventListener(listener)   // 리스너도 없앤다. (중복방지)
    }


}