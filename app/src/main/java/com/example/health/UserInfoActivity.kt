package com.example.health

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.health.databinding.ActivityUserInfoBinding
import com.google.firebase.firestore.FirebaseFirestore

//마이페이지
class UserInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserInfoBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editGender: EditText
    private lateinit var editPhoneNumber: EditText
    private lateinit var editPetName: EditText
    private lateinit var editPetGender: EditText
    private lateinit var editPetNumber: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editGender = findViewById(R.id.editGender)
        editPhoneNumber = findViewById(R.id.editPhoneNumber)
        editPetName = findViewById(R.id.editPetName)
        editPetGender = findViewById(R.id.editPetGender)
        editPetNumber = findViewById(R.id.editPetNumber)
        saveButton = findViewById(R.id.saveButton)

        //users 필드에 현재 로그인한 사용자 의 정보 가지고오기
        val currentUser = MyApplication.auth.currentUser
        currentUser?.let { user ->
            val userRef = db.collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userInfo = document.toObject(User::class.java)
                        userInfo?.let {
                            // 기존 정보를 TextView와 EditText에 표시
                            binding.userNameTextView.text = "이름: ${userInfo.name}"
                            binding.userEmailTextView.text = "이메일: ${userInfo.email}"
                            binding.userGenderTextView.text = "성별: ${userInfo.gender}"
                            binding.userPhoneNumberTextView.text = "생년월일: ${userInfo.phoneNumber}"
                            binding.userPetNameTextView.text = "펫 이름: ${userInfo.petname}"
                            binding.userPetGenderTextView.text = "펫 성별: ${userInfo.petgender}"
                            binding.userPetNumberTextView.text = "펫 나이: ${userInfo.petNumber}"

                            // EditText에 기존 정보 설정
                            editName.setText(userInfo.name)
                            editEmail.setText(userInfo.email)
                            editGender.setText(userInfo.gender)
                            editPhoneNumber.setText(userInfo.phoneNumber)
                            editPetName.setText(userInfo.petname)
                            editPetGender.setText(userInfo.petgender)
                            editPetNumber.setText(userInfo.petNumber)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }

        //변경버튼 으로 users 필드 정보 변경
        saveButton.setOnClickListener {
            currentUser?.let { user ->
                val userRef = db.collection("users").document(user.uid)
                val newName = editName.text.toString()
                val newEmail = editEmail.text.toString()
                val newGender = editGender.text.toString()
                val newPhoneNumber = editPhoneNumber.text.toString()
                val newPetName = editPetName.text.toString()
                val newPetGender = editPetGender.text.toString()
                val newPetNumber = editPetNumber.text.toString()

                userRef.update(
                    "name", newName,
                    "email", newEmail,
                    "gender", newGender,
                    "phoneNumber", newPhoneNumber,
                    "petname", newPetName,
                    "petgender", newPetGender,
                    "petNumber", newPetNumber
                )
                    .addOnSuccessListener {
                        editName.text.clear()
                        editEmail.text.clear()
                        editGender.text.clear()
                        editPhoneNumber.text.clear()
                        editPetName.text.clear()
                        editPetGender.text.clear()
                        editPetNumber.text.clear()
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "업데이트 실패", exception)
                    }
            }
        }
    }
}
