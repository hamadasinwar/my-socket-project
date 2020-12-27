package com.example.myapplication.adapters

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupChatActivity
import com.example.myapplication.models.MessageFormat

class GroupChatAdapter(context: Context, resource: Int, objects: List<MessageFormat>):
    ArrayAdapter<MessageFormat>(context, resource, objects) {
    override fun getView(position: Int, cv: View?, parent: ViewGroup): View {

        val convertView: View?
        val message = getItem(position)!!

        when {
            TextUtils.isEmpty(message.message) -> {
                convertView = (context as Activity).layoutInflater.inflate(R.layout.user_connected, parent, false)
                val messageText = convertView.findViewById<TextView>(R.id.message_body)
                val userConnected: String = message.username
                messageText.text = userConnected
            }
            message.id == GroupChatActivity.uniqueId -> {
                convertView = (context as Activity).layoutInflater.inflate(R.layout.my_message, parent, false)
                val messageText = convertView.findViewById<TextView>(R.id.message_body)
                messageText.text = message.message
            }
            else -> {
                convertView = (context as Activity).layoutInflater.inflate(R.layout.their_message, parent, false)
                val messageText = convertView.findViewById<TextView>(R.id.message_body)
                val usernameText = convertView.findViewById<View>(R.id.name) as TextView
                messageText.visibility = View.VISIBLE
                usernameText.visibility = View.VISIBLE
                messageText.text = message.message
                usernameText.text = message.username
            }
        }

        return convertView
    }
}