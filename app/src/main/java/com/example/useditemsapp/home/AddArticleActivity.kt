package com.example.useditemsapp.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.useditemsapp.DBKey.Companion.DB_ARTICLES
import com.example.useditemsapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.DecimalFormat

class AddArticleActivity: AppCompatActivity() {
    private val titleEditText: EditText by lazy {
        findViewById(R.id.titleEditText)
    }
    private val priceEditText: EditText by lazy {
        findViewById(R.id.priceEditText)
    }
    private val itemImageView: ImageView by lazy {
        findViewById(R.id.itemImageView)
    }
    private val imageAddButton: Button by lazy {
        findViewById(R.id.imageAddButton)
    }
    private val uploadButton: Button by lazy {
        findViewById(R.id.uploadButton)
    }
    private val progressBar: ProgressBar by lazy {
        findViewById(R.id.progressBar)
    }

    private var pointNumStr = ""
    private val watcher = object: TextWatcher {   // 가격 단위 표시 리스너
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if(!TextUtils.isEmpty(p0.toString()) && p0.toString()!=pointNumStr) {
                pointNumStr = makeCommaNumber((p0.toString().replace(",","")).toInt())
                priceEditText.setText(pointNumStr)
                priceEditText.setSelection((pointNumStr.length))   // 커서를 오른쪽 끝으로 보냄
            }
        }
        override fun afterTextChanged(p0: Editable?) {}
    }

    private var selectedUri: Uri? = null
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val articleDB = Firebase.database.reference.child(DB_ARTICLES)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        priceEditText.addTextChangedListener(watcher)   // EditText에 가격 단위 표시 리스너 적용

        // 이미지 추가 버튼 구현
        imageAddButton.setOnClickListener {
            when {   // 사진권한팝업창
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // permission이 허용된 경우
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // 교육용 팝업이 필요한 경우
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1010)
                }
            }
        }

        // 등록버튼 누르면 DB에 정보 저장
        uploadButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val price = priceEditText.text.toString()
            val sellerID = auth.currentUser?.uid.orEmpty()

            showProgressBar()

            if(title == "") {
                hideProgressBar()
                Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else if(price == "") {
                hideProgressBar()
                Toast.makeText(this, "가격을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedUri != null) {   // 이미지가 있으면
                    val photoUri = selectedUri ?: return@setOnClickListener   // null처리

                    uploadPhoto(photoUri,   // 이미지와 다른 정보들 저장
                        successHandler = { uri ->
                            uploadArticle(sellerID, title, price, uri)
                        },
                        errorHandler = {
                            Toast.makeText(this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            hideProgressBar()
                        }
                    )
                } else {
                    uploadArticle(sellerID, title, price, "")
                }
            }
        }
    }

    // 가격 단위 표시하기 위한 함수
    private fun makeCommaNumber(input: Int): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("article/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {   // 업로드가 성공적
                    storage.reference.child("article/photo").child(fileName).downloadUrl   // url을 다운로드
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    private fun uploadArticle(sellerID: String, title: String, price: String, imageURL: String) {
        val model = ArticleModel(sellerID, title, System.currentTimeMillis(), "$price 원", imageURL)
        articleDB.push().setValue(model)

        hideProgressBar()

        finish()
    }

    // 권한에 대해서 result가 오면
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1010 ->
                // 승낙결과가 안비었는지 && 패키지매니저가 승낙 되었는지
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContentProvider()
                }

                else -> {
                Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 이미지 선택
    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"   // 모든 image 타입을 가져와라
        startActivityForResult(intent, 2020)
    }

    /* startActivityForResult
     * 요청 코드를 전달
     * onActivityResult
     * 요청 코드를 식별하여 결과 값을 다시 반환 */

    // 선택한 이미지를 uri로 가져오기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK)   return

        when(requestCode) {
            2020 -> {
                val uri = data?.data

                if(uri != null) {
                    itemImageView.setImageURI(uri)
                    selectedUri = uri
                }
                else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 교육용 팝업
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1010)
            }
            .create()
            .show()
    }

    private fun showProgressBar() {
        progressBar.isVisible = true
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progressBar.isVisible = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}