package com.example.health
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

//커뮤니티
class Read : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.read)
        val button1 = findViewById<Button>(R.id.button1)
        listView = findViewById(R.id.listView)
        adapter = CustomAdapter(this)

        // Firestore에서 Community 필드 값 가져오기
        val db = FirebaseFirestore.getInstance()
        db.collection("Community")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.getString("title")
                    val content = document.getString("content")
                    val name = document.getString("name")
                    val timestamp = document.getString("timestamp")
                    val imageUrl = document.getString("imageUrl")
                    adapter.addItem(title, content, name,timestamp,imageUrl)
                }

                // ListView에 CustomAdapter 설정
                listView.adapter = adapter

                // DetailActivity에 값 전달
                listView.setOnItemClickListener { _, _, position, _ ->
                    val selectedItem = adapter.getItem(position) as CustomAdapter.CustomItem
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("content", selectedItem.content)
                    intent.putExtra("name", selectedItem.name)
                    intent.putExtra("timestamp", selectedItem.timestamp)
                    intent.putExtra("imageUrl", selectedItem.imageUrl)

                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
            }

        button1.setOnClickListener {
            val nextIntent = Intent(this, Write::class.java)
            startActivity(nextIntent)
        }

    }
}