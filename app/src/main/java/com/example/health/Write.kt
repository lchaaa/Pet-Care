package com.example.health

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.HashMap
import java.util.Locale
import java.util.UUID

//커뮤니티 글쓰기
class Write : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var sendButton: Button
    private lateinit var aa: Button
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    // 선택한 이미지 URI를 가져온 후 처리
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.write)
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        nameEditText = findViewById(R.id.nameEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        sendButton = findViewById(R.id.sendButton)

        //사진버튼 클릭 갤러리
        selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        // Firestore에 보내기
        sendButton.setOnClickListener {
            Toast.makeText(this, "글 쓰기 완료.", Toast.LENGTH_SHORT).show()
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val name = nameEditText.text.toString()
            val currentDate = Calendar.getInstance().time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // 현재 날짜 및 시간 가져오기
            val formattedDate = sdf.format(currentDate)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg") // 이미지 파일 이름을 고유하게 생성

            // 이미지를 업로드한 후 다운로드 URL을 가져옴
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString() // 이미지 파일의 다운로드 URL
                        val data = hashMapOf(
                            "title" to title,
                            "content" to content,
                            "name" to name,
                            "imageUrl" to imageUrl,
                            "timestamp" to formattedDate
                        )
                        db.collection("Community")
                            .add(data)
                            .addOnSuccessListener { documentReference ->
                                val documentId = documentReference.id
                            }
                            .addOnFailureListener { e ->
                            }
                    }
                }
                .addOnFailureListener { e ->
                }
        }
    }
}

