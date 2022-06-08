package com.example.useditemsapp.mypage

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.useditemsapp.DBKey.Companion.DB_USERS
import com.example.useditemsapp.R
import com.example.useditemsapp.databinding.FragmentMypageBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider

class MyPageFragment:Fragment(R.layout.fragment_mypage) {

    private var binding: FragmentMypageBinding? = null

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val userDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_USERS)
    }

    // 페이스북 로그인 수행하기 위한
    private val callbackManager: CallbackManager by lazy {
        CallbackManager.Factory.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프래그먼트 바인딩
        val fragmentMyPageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMyPageBinding

        // 페이스북 로그인 버튼 구현
        fragmentMyPageBinding.facebookLoginButton.let {
            it.setPermissions("email", "public_profile")
            it.fragment = this
            it.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    // 로그인 accessToken을 가져옴
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    // 그 token으로 로그인 수행
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if(task.isSuccessful) {
                                Log.d("MY TAG", "로그인 성공")
                                successSignIn()
                            } else {
                                Log.d(" MY TAG", "로그인 실패")
                                Toast.makeText(context,"페이스북 로그인이 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                override fun onCancel() { }
                override fun onError(error: FacebookException?) {
                    Log.d("MY TAG", "로그인 실패")
                    Toast.makeText(context,"페이스북 로그인이 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 로그인 버튼 구현
        fragmentMyPageBinding.signInOutButton.setOnClickListener {
            binding?.let { binding ->
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                if (auth.currentUser == null) {   // 로그인
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if(task.isSuccessful) {
                                successSignIn()
                            }
                            else {
                                Toast.makeText(context, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else {   // 로그아웃
                    auth.signOut()
                    binding.emailEditText.text.clear()
                    binding.passwordEditText.text.clear()

                    binding.signInOutButton.text = "로그인"

                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true
                    binding.signInOutButton.isEnabled = false
                    binding.signUpButton.isEnabled = false
                }
            }
        }

        // 회원가입 버튼 구현
        fragmentMyPageBinding.signUpButton.setOnClickListener {
            binding?.let { binding ->
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if(task.isSuccessful) {
                            //Log.d(TAG, "createUserWithEmail:success")
                            showInfoInputPopup()
                            Toast.makeText(context, "회원가입에 성공했습니다. 로그인 버튼을 눌러 로그인 해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            //Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(context, "회원가입에 실패했습니다. 이미 가입한 이메일일 수 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }


            }
        }

        // 이메일란, 패스워드란 둘다 안 비었을때 회원가입, 로그인 버튼 enable
        fragmentMyPageBinding.emailEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }

        fragmentMyPageBinding.passwordEditText.addTextChangedListener{
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }
    }

    // 로그인이 풀렸는지 안풀렸는지 확인
    override fun onStart() {
        super.onStart()

        if(auth.currentUser == null) {   // 로그인이 풀려있을때 -> 로그인 할수있게끔
            binding?.let { binding ->
                binding.emailEditText.text.clear()
                binding.emailEditText.isEnabled = true
                binding.passwordEditText.text.clear()
                binding.passwordEditText.isEnabled = true
                binding.signInOutButton.text = "로그인"
                binding.signInOutButton.isEnabled = false
                binding.signUpButton.isEnabled = false
            }
        }
        else {   // 로그인 되어있을때
            binding?.let { binding ->
                binding.emailEditText.setText(auth.currentUser!!.email)
                binding.emailEditText.isEnabled = false
                binding.passwordEditText.setText("********")
                binding.passwordEditText.isEnabled = false
                binding.signInOutButton.text = "로그아웃"
                binding.signInOutButton.isEnabled = true
                binding.signUpButton.isEnabled = false
            }
        }
    }

    private fun successSignIn() {
        if(auth.currentUser == null) {   // 예외처리
            Toast.makeText(context, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
        }

        binding?.let { binding ->
            binding.emailEditText.isEnabled = false
            binding.passwordEditText.isEnabled = false
            binding.signUpButton.isEnabled = false
            binding.signInOutButton.text = "로그아웃"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // 정보 입력 팝업창
    private fun showInfoInputPopup() {
        val dialogView: View = layoutInflater.inflate(R.layout.info_popup, null)
        val nickName = dialogView.findViewById<EditText>(R.id.nickNameEditText)
        val phoneNum = dialogView.findViewById<EditText>(R.id.phoneNumEditText)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                if(nickName.text.isEmpty() || phoneNum.text.isEmpty())   showInfoInputPopup()
                else {
                    saveInfo(nickName.text.toString(), phoneNum.text.toString())
                }
            }
            .setCancelable(false)   // 뒤로가기키와 배경터치로 대화창 취소 불가능
            .show()
    }

    private fun saveInfo(nickName: String, phoneNum: String) {
        val userID = auth.currentUser?.uid.orEmpty()
        val currentUserDB = userDB.child(userID)   // Users라는 최상위 reference의 child인 userID를 가져와 currentUserDB에 대입 (만약 없으면 자동으로 만들어준다.)
        val user = mutableMapOf<String, Any>()   // map: 키와 값의 쌍으로 입력되는 컬렉션
        user["NickName"] = nickName
        user["phone"] = phoneNum
        currentUserDB.updateChildren(user)
    }

}