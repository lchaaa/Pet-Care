package com.example.health


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

//산채시작
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val pathPoints = mutableListOf<LatLng>()
    private var isTracking = false
    private var locationCallback: LocationCallback? = null
    private var isTimerRunning = false
    private var secondsElapsed = 0
    private var timer: CountDownTimer? = null
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        // 위치 업데이트 콜백 설정
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    onLocationChanged(location)
                }
            }
        }
        //타이머 0초로 설정
        updateTimer(0)
    }

    //현재 위치를 가져와 지도를 해당 위치로 이동
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        getLastLocation()
    }

    //위치 권한이 부여되었는지 확인
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    //마지막으로 알려진 위치를 가져와 지도를 해당 위치로 이동
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    // 버튼 클릭 시 외부 카메라 앱 실행
    fun openCamera(view: View) {
        val packageName = "com.sec.android.app.camera"
        val pm: PackageManager = packageManager
        val intent: Intent? = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(intent)
        } else {
        }
    }

    // 시작 버튼 클릭 이벤트
    fun onStartButtonClicked(view: View) {
        isTracking = true
        startLocationUpdates()
        startTimer()  //타이머시작
    }

    // 끝 버튼 클릭 이벤트
    fun onStopButtonClicked(view: View) {
        isTracking = false
        stopLocationUpdates()
        stopTimer()
        // 경로 데이터 및 타이머 정보를 MapActivity로 전달
        val intent = Intent(this, MapActivity::class.java)
        intent.putParcelableArrayListExtra("pathPoints", ArrayList(pathPoints))
        // 타이머 정보를 전달
        intent.putExtra("secondsElapsed", secondsElapsed)
        startActivity(intent)
    }

    //타이머 정지 버튼 클릭 이벤트
    fun onStopTimerButtonClicked(view: View) {
        if (isTimerRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    // 타이머 시작
    private fun startTimer() {
        if (!isTimerRunning) {
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    secondsElapsed++
                    updateTimer(secondsElapsed)
                }
                override fun onFinish() {
                }
            }
            timer?.start()
            isTimerRunning = true
        }
    }

    // 타이머 정지
    private fun stopTimer() {
        timer?.cancel()
        isTimerRunning = false
    }

    // 타이머 업데이트
    private fun updateTimer(seconds: Int) {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        val timerText = String.format("%02d:%02d:%02d", hours, minutes, secs)
        val timerTextView = findViewById<TextView>(R.id.timerTextView)
        timerTextView.text = timerText
    }

    //현재 위치를 pathPoints 리스트에 추가
    private fun onLocationChanged(location: Location) {
        if (isTracking) {
            val latLng = LatLng(location.latitude, location.longitude)
            pathPoints.add(latLng)
        }
    }

    // 위치 업데이트 시작
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest()
            .setInterval(5000) // 위치 업데이트 간격(ms)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationCallback?.let {
                fusedLocationClient.requestLocationUpdates(locationRequest, it, null)
            }
        }
    }

    // 위치 업데이트 중지
    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }
}
