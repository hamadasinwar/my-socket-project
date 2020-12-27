package com.example.myapplication.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.app.App
import com.example.myapplication.models.Group
import kotlinx.android.synthetic.main.recent_message.view.*


class GroupAdapter(val activity: Activity, val data:MutableList<Group>, private val onClickItem: OnClickItem)
    :RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private val user = (activity.application as App).getUser()

    inner class GroupViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.recentReceiver
        val users: TextView = item.recentMessage
        val image: ImageView = item.recentImage
        val card: LinearLayout = item.cardRecent
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.recent_message, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.name.text = data[position].name
        var names = ""
        for (user in data[position].users){
            names += if (user.id == this.user.id){
                "You, "
            }else{
                "${user.name}, "
            }
        }
        names = names.subSequence(0, names.length-2).toString()
        holder.users.text = names
        holder.image.setImageResource(R.drawable.ic_group)
        holder.image.setPadding(30, 30, 30, 30)
        holder.card.setOnClickListener {
            onClickItem.onGroupClick(position)
        }
    }

    interface OnClickItem{
        fun onGroupClick(position: Int)
    }
}