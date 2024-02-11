package com.example.health

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

//시작페이지
class Start : AppCompatActivity() {
    private fun formatSecondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)
        val button1 = findViewById<CardView>(R.id.button1)
        val button2 = findViewById<CardView>(R.id.button2)
        val button3 = findViewById<CardView>(R.id.button3)
        val cbutton = findViewById<CardView>(R.id.cbutton)
        val mybutton = findViewById<CardView>(R.id.mybutton)

        //산책시작
        button1.setOnClickListener {
            val nextIntent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
        }
        //산책일지
        button2.setOnClickListener {
            val nextIntent = Intent(this, CalendarFragment::class.java)
            startActivity(nextIntent)
        }
        //커뮤니티
        cbutton.setOnClickListener {
            val nextIntent = Intent(this, Read::class.java)
            startActivity(nextIntent)
        }
        //마이페이지
        mybutton.setOnClickListener {
            val nextIntent = Intent(this, UserInfoActivity::class.java)
            startActivity(nextIntent)
        }
        //로그아웃
        button3.setOnClickListener {
            val nextIntent = Intent(this, AuthActivity::class.java)
            nextIntent.putExtra("mode", "logout")
            MyApplication.auth.signOut()
            MyApplication.email = null
            startActivity(nextIntent)
        }

        // Firestore에서 walks 필드 데이터 가져오기
        val db = FirebaseFirestore.getInstance()
        val user = MyApplication.auth.currentUser

        //walks 필드의 걸은 시간 합산 , 총 산책 횟수 가져오기
        user?.let { currentUser ->
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val walks = document["walks"] as? ArrayList<Map<String, Any>>
                        walks?.let { walksList ->
                            var totalWalkTimeSeconds = 0
                            val totalWalks = walksList.size // walks 필드의 개수

                            for (walk in walksList) {
                                val walkTime = walk["walkTime"] as? String

                                walkTime?.let {
                                    // 걸은 시간을 시, 분, 초로 분해하여 초 단위로 합산
                                    val timeParts = it.split(":")
                                    if (timeParts.size == 3) {
                                        val hours = timeParts[0].toInt()
                                        val minutes = timeParts[1].toInt()
                                        val seconds = timeParts[2].toInt()
                                        totalWalkTimeSeconds += hours * 3600 + minutes * 60 + seconds
                                    }
                                }
                            }
                            val totalWalkTimeTextView = findViewById<TextView>(R.id.totalWalkTimeTextView)

                            val formattedTotalWalkTime = formatSecondsToTime(totalWalkTimeSeconds)
                            totalWalkTimeTextView.text = "총 산책 시간: $formattedTotalWalkTime"

                            val totalWalksTextView = findViewById<TextView>(R.id.totalWalksTextView)
                            totalWalksTextView.text = "총 산책 횟수: $totalWalks 회"
                        }
                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }
        }

        //walks 필드의 이름과 애완동물 이름 가져오기
        user?.let { currentUser ->
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document["name"] as? String
                        val petname = document["petname"] as? String
                        val nameTextView = findViewById<TextView>(R.id.nameTextView2)
                        val petnameTextView = findViewById<TextView>(R.id.petNameTextView)

                        name?.let {
                            nameTextView.text = "이름: $it"
                        }

                        petname?.let {
                            petnameTextView.text = "반려견 이름: $it"
                        }

                    } else {
                    }
                }
                .addOnFailureListener { exception ->
                }
        }

    }
}