package com.example.health
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

//댓글 어댑터
class CommentAdapter(private val context: Context) : BaseAdapter() {
    private val comments = ArrayList<String>()
    fun setComments(comments: List<String>) {
        this.comments.clear()
        this.comments.addAll(comments)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return comments.size
    }

    override fun getItem(position: Int): Any {
        return comments[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val comment = comments[position]
        val commentView = TextView(context)
        commentView.text = comment
        return commentView
    }
}
