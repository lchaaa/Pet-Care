package com.example.health

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

//산책시작 끝
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val pathPoints = mutableListOf<LatLng>()
    private lateinit var timerTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var dateTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        FirebaseApp.initializeApp(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 이전 액티비티로부터 전달받은 경로 데이터
        val receivedPathPoints: ArrayList<LatLng>? =
            intent.getParcelableArrayListExtra("pathPoints")
        if (receivedPathPoints != null) {
            pathPoints.addAll(receivedPathPoints)
        }

        // 타이머 정보 가져오기
        val secondsElapsed = intent.getIntExtra("secondsElapsed", 0)
        timerTextView = findViewById(R.id.timerTextView)
        dateTimeTextView = findViewById(R.id.dateTimeTextView)

        // 타이머 정보를 시, 분, 초 형식으로 표시
        val hours = secondsElapsed / 3600
        val minutes = (secondsElapsed % 3600) / 60
        val seconds = secondsElapsed % 60
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // 거리를 표시할 TextView 초기화
        distanceTextView = findViewById(R.id.distanceTextView)
        val currentDateTime = getCurrentDateTime()
        dateTimeTextView.text = currentDateTime

        //저장
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            captureMapAndUploadToFirebase()
            Toast.makeText(this, "산책 일지에 저장합니다.", Toast.LENGTH_SHORT).show()
        }

        //홈
        val moreButton = findViewById<Button>(R.id.moreButton)
        moreButton.setOnClickListener {
            val nextIntent = Intent(this, Start::class.java)
            startActivity(nextIntent)
        }
    }

    //현재 날짜
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd a hh:mm", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap  //경로를 Polyline 표시
        drawPolyline()
        calculateAndDisplayDistance() // 경로 거리 계산 및 표시
    }

    private fun drawPolyline() {
        //굵기,색
        val polylineOptions = PolylineOptions().width(20f).color(Color.BLUE)
        for (point in pathPoints) {
            polylineOptions.add(point)
        }

        mMap.addPolyline(polylineOptions)

        // 경로의 첫 번째 위치로 이동
        if (pathPoints.isNotEmpty()) {
            val startPoint = pathPoints[0]
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))
        }
    }

    // 경로 거리 계산 및 표시
    private fun calculateAndDisplayDistance() {
        var totalDistance = 0.0

        for (i in 0 until pathPoints.size - 1) {
            val startPoint = pathPoints[i]
            val endPoint = pathPoints[i + 1]
            val distance = calculateHaversineDistance(startPoint, endPoint)
            totalDistance += distance
        }

        val df = DecimalFormat("#.##") // 소수점 두 자리까지 표시
        val formattedDistance = df.format(totalDistance)

        distanceTextView.text = "이동 거리: $formattedDistance km"
    }

    //두 지점 간의 Haversine 공식 사용 거리 계산
    private fun calculateHaversineDistance(start: LatLng, end: LatLng): Double {
        val radiusOfEarth = 6371.0 // 지구 반지름 (단위: km)
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2).let { it * it }
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radiusOfEarth * c
    }

    //Google Map의 현재 상태를 캡처하고 Firebase Storage에 업로드
    private fun captureMapAndUploadToFirebase() {
        mMap.snapshot { bitmap ->
            val storageRef = Firebase.storage.reference
            val imagesRef = storageRef.child("path_images")
            val imageName = "path_${System.currentTimeMillis()}.jpg"
            val imageRef = imagesRef.child(imageName)
            val baos = ByteArrayOutputStream()
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { downloadUrlTask ->
                if (downloadUrlTask.isSuccessful) {
                    val downloadUrl = downloadUrlTask.result.toString()
                    sendDataToFirebase(downloadUrl)
                } else {
                    Log.e(TAG, "Failed to upload image")
                }
            }
        }
    }

    //Firebase Firestore에 사용자의 산책 정보를 업로드
    private fun sendDataToFirebase(imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val user = MyApplication.auth.currentUser
        val currentTime = timerTextView.text.toString()
        val currentDistance = distanceTextView.text.toString()
        val currentDateTime = dateTimeTextView.text.toString()

        user?.let { currentUser ->
            // Firebase에 값을 업데이트
            db.collection("users").document(currentUser.uid)
                .update(
                    "walks", FieldValue.arrayUnion(
                        mapOf(
                            "walkTime" to currentTime,
                            "walkDistance" to currentDistance,
                            "walkDateTime" to currentDateTime,
                            "walkImagePath" to imageUrl
                        )
                    )
                )
                .addOnSuccessListener {
                    Log.d(TAG, "Data updated successfully")

                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating data", e)

                }
        }
    }
    companion object {
        const val TAG = "MapActivity"
    }
}
