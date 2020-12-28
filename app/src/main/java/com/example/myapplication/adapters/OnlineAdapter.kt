package com.example.myapplication.adapters

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.online_item.view.*

class OnlineAdapter(private val activity: Activity, private val data:List<User>, private val onClickItem:OnClickItem)
    :RecyclerView.Adapter<OnlineAdapter.OnlineViewHolder>(){

    inner class OnlineViewHolder(item: View):RecyclerView.ViewHolder(item){
        val name: TextView = item.onlineName
        val image: ImageView = item.onlineImage
        val card: LinearLayout = item.cardOnline
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.online_item, parent, false)
        return OnlineViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: OnlineViewHolder, position: Int) {
        holder.name.text = data[position].name
        holder.card.setOnClickListener {
            onClickItem.onOnlineClick(position)
        }
        if (data[position].image == ""){
            holder.image.setImageResource(R.drawable.ic_person)
            holder.image.setPadding(30, 30, 30, 30)
        }else{
            Picasso.get().load(Uri.parse(data[position].image)).into(holder.image)
        }
        if (data[position].id == "createGroup"){
            holder.image.setImageResource(R.drawable.ic_person_pin)
        }
    }

    interface OnClickItem{
        fun onOnlineClick(position: Int)
    }

}