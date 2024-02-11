package com.example.health

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

//산책일지
class CalendarFragment : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var linearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_calendar)
        linearLayout = findViewById(R.id.yourLinearLayout)
        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.imageView)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selectedDate = dateFormat.format(calendar.time) //선택된 날짜

            // 현재 로그인된 사용자의 UID 가져오기
            val currentUser = auth.currentUser
            currentUser?.uid?.let { uid ->
                fetchDataFromFirebase(uid, selectedDate) //선택한 날짜에 걸음정보 확인
            }
        }
    }
    //선택한 날짜에 걸음정보 확인
    private fun fetchDataFromFirebase(uid: String, selectedDate: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val walks = document.get("walks") as? List<Map<String, String>>
                    linearLayout.removeAllViews()

                    walks?.let {
                        if (it.isNotEmpty()) {
                            // 선택한 날짜에 해당하는 걸음 정보만 필터링
                            val selectedWalks = it.filter { walk ->
                                val walkDateTime = walk["walkDateTime"]
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val firebaseDate = dateFormat.format(dateFormat.parse(walkDateTime))
                                firebaseDate == selectedDate //선택한 날짜에 해당하는 정보를 필터링
                            }

                            // 선택한 날짜에 해당하는 걸음 정보가 있을 경우
                            if (selectedWalks.isNotEmpty()) {
                                for (walk in selectedWalks) {
                                    val walkTime = walk["walkTime"]
                                    val walkDistance = walk["walkDistance"]
                                    val walkDateTime = walk["walkDateTime"]
                                    val walksInfoText =
                                        "산책 시간: $walkTime,  $walkDistance,\n날짜: $walkDateTime\n"
                                    val textView = TextView(this)
                                    textView.text = walksInfoText
                                    linearLayout.addView(textView)

                                    // 이미지 URL이 있는지 확인하고 있으면 리스트에 추가
                                    val imageUrl = walk["walkImagePath"]
                                    if (!imageUrl.isNullOrEmpty()) {
                                        val newImageView = ImageView(this)
                                        Glide.with(this)
                                            .load(imageUrl)
                                            .into(newImageView)
                                        val layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            resources.getDimensionPixelSize(R.dimen.image_height) // 원하는 높이로 변경
                                        )
                                        newImageView.layoutParams = layoutParams
                                        linearLayout.addView(newImageView)
                                    }
                                }
                            } else {
                                textView.text = "선택한 날짜에 대한 걸음 데이터가 없습니다."
                                imageView.visibility = View.GONE
                            }
                        } else {
                            textView.text = "걸음 데이터가 없습니다."
                            imageView.visibility = View.GONE
                        }
                    }
                } else {
                    textView.text = "사용자 정보가 없습니다."
                    imageView.visibility = View.GONE
                }

            }
            .addOnFailureListener { exception ->
                textView.text = "정보를 불러오는 데 실패했습니다: $exception"
                imageView.visibility = View.GONE
            }

    }
}
