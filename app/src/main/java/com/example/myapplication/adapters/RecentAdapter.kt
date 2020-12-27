package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.User
import kotlinx.android.synthetic.main.recent_message.view.*

class RecentAdapter(private val activity: Activity, private var data:MutableList<User>, private val onClickItem: OnClickItem)
    :RecyclerView.Adapter<RecentAdapter.RecentViewHolder>() {

    inner class RecentViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.recentReceiver
        val message: TextView = item.recentMessage
        val image: ImageView = item.recentImage
        val card: LinearLayout = item.cardRecent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.recent_message, parent, false)
        return RecentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.name.text = data[position].name
        holder.message.text = "say hi to ${data[position].name}"
        holder.image.setImageResource(R.mipmap.ic_launcher)
        holder.card.setOnClickListener {
            onClickItem.onRecentClick(position)
        }
        holder.image.setImageResource(R.drawable.ic_person)
        holder.image.setPadding(30, 30, 30, 30)
    }

    interface OnClickItem{
        fun onRecentClick(position: Int)
    }

    fun filterList(filteredList:MutableList<User>){
        data = filteredList
        notifyDataSetChanged()
    }
}