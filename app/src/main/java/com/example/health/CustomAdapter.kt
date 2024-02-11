package com.example.health

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

//리스트뷰 에 표시 어댑터
class CustomAdapter(private val context: Context) : BaseAdapter() {
    data class CustomItem(val title: String?, val content: String?,val name: String?,val timestamp: String?, val imageUrl: String?)
    private val items = mutableListOf<CustomItem>()

    fun addItem(title: String?, content: String?,name: String?,timestamp: String?, imageUrl: String?) {
        val item = CustomItem(title, content, name,timestamp,imageUrl)
        items.add(item)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View
        val holder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.list_item, null)
            holder = ViewHolder()
            holder.titleTextView = itemView.findViewById(R.id.titleTextView)
            holder.contentTextView = itemView.findViewById(R.id.contentTextView)
            holder.nameTextView = itemView.findViewById(R.id.nameTextView)
            holder.timestampTextView = itemView.findViewById(R.id.timestampTextView)
            holder.imageView = itemView.findViewById(R.id.imageView)
            itemView.tag = holder
        } else {
            itemView = convertView
            holder = itemView.tag as ViewHolder
        }
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.contentTextView.text = "내용: ${item.content}"
        holder.nameTextView.text = "이름: ${item.name}"
        holder.timestampTextView.text = "날짜: ${item.timestamp}"

        // 이미지를 Picasso를 사용하여 이미지뷰에 설정합니다.
        if (item.imageUrl != null) {
            Picasso.get().load(item.imageUrl).into(holder.imageView)
        }
        return itemView
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var contentTextView: TextView
        lateinit var nameTextView: TextView
        lateinit var timestampTextView: TextView
        lateinit var imageView: ImageView
    }
}