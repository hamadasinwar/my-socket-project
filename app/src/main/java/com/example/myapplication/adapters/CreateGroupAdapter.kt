package com.example.myapplication.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.User
import com.example.myapplication.models.UserGroup
import kotlinx.android.synthetic.main.group_item.view.*

class CreateGroupAdapter(val activity: Activity, private val data:MutableList<User>)
    :RecyclerView.Adapter<CreateGroupAdapter.CreateGroupViewHolder>() {

    private val d = mutableListOf<UserGroup>()

    inner class CreateGroupViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: TextView = item.usernameGroup
        val checkBox: CheckBox = item.cbGroup
        val card: RelativeLayout = item.cardGroup
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateGroupViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.group_item, parent, false)
        return CreateGroupViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CreateGroupViewHolder, position: Int) {
        holder.checkBox.isClickable = false
        d.add(UserGroup(data[position], false))
        holder.name.text = data[position].name
        holder.card.setOnClickListener {
            holder.checkBox.isChecked = !(holder.checkBox.isChecked)
            d[position].isChecked = holder.checkBox.isChecked
        }
    }

    fun checkData():Int{
        var x = 0
        for (item in d){
            if (item.isChecked){
                x++
            }
        }
        return  x
    }
    fun getData():MutableList<User>{
        val da = mutableListOf<User>()
        for (g in d){
            for (u in data){
                if (g.user.id == u.id && g.isChecked){
                    da.add(u)
                }
            }
        }
        return da
    }

}