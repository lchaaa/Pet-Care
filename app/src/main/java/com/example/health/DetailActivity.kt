package com.example.health

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso

//커뮤니티 디테일
class DetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var timestampTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var commentListView: ListView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var commentsCollection: CollectionReference
    private lateinit var userName: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        nameTextView = findViewById(R.id.nameTextView)
        timestampTextView = findViewById(R.id.timestampTextView)
        imageView = findViewById(R.id.imageView)
        commentListView = findViewById(R.id.commentListView)
        commentAdapter = CommentAdapter(this)
        commentListView.adapter = commentAdapter

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance()
        commentsCollection = firestore.collection("comments")

        // Intent에서 데이터 가져오기
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val name = intent.getStringExtra("name")
        val timestamp = intent.getStringExtra("timestamp")
        val imageUrl = intent.getStringExtra("imageUrl")

        // 가져온 데이터를 TextView와 ImageView에 설정
        titleTextView.text = "제목: $title"
        contentTextView.text = "내용: $content"
        nameTextView.text = "이름: $name"
        timestampTextView.text = "시간: $timestamp"

        // 이미지를 Picasso를 사용하여 ImageView에 설정
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(imageView)
        }

        // 게시물에 대한 댓글 가져오기
        if (title != null) {
            loadCommentsForPost(title)
        }

        // 사용자 정보 가져오기
        val currentUser = MyApplication.auth.currentUser
        currentUser?.let { user ->
            val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        user?.let {
                            userName = user.name ?: "DefaultName" // 여기서 사용자 이름 가져오기
                        }
                    }
                }
        }

        // 댓글을 Firestore에 추가
        findViewById<Button>(R.id.submitCommentButton).setOnClickListener {
            val commentText = findViewById<EditText>(R.id.commentEditText).text.toString()
            if (title != null) {
                addCommentToFirestore(title, commentText)
            }
            findViewById<EditText>(R.id.commentEditText).text.clear()
        }
    }

    //게시물에 대한 댓글을 Firestore에서 가져와서 화면에 표시
    private fun loadCommentsForPost(postTitle: String) {
        commentsCollection.document(postTitle)
            .collection("comments")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                val comments = ArrayList<String>()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val comment = doc.getString("text")
                        comment?.let {
                            comments.add(it)
                        }
                    }
                }
                commentAdapter.setComments(comments)
            }
    }

    // 댓글을 Firestore에 추가
    private fun addCommentToFirestore(postTitle: String, commentText: String) {
        val commentData = hashMapOf(
            "text" to "$userName: $commentText"
        )
        commentsCollection.document(postTitle)
            .collection("comments")
            .add(commentData)
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { e ->
            }
    }
}
