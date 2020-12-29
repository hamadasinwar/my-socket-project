package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.myapplication.activities.ChatActivity
import com.example.myapplication.R
import com.example.myapplication.models.MessageFormat
import com.squareup.picasso.Picasso
import java.util.*

@SuppressLint("CutPasteId")
class MessageAdapter(context: Context, resource: Int, objects: List<MessageFormat>, private val onClickItem: OnClickItem)
    :ArrayAdapter<MessageFormat>(context, resource, objects) {

    private var profileImage = ""
    override fun getView(position: Int, cv: View?, parent: ViewGroup): View {

        val convertView: View?
        val message = getItem(position)!!

        if (message.getIsImage()){
            when (message.id) {
                ChatActivity.uniqueId -> {
                    convertView = (context as Activity).layoutInflater.inflate(R.layout.my_image, parent, false)
                    val image = convertView.findViewById<ImageView>(R.id.message_body)
                    val card = convertView.findViewById<CardView>(R.id.card)
                    image.setImageBitmap(decodeImage(message.message!!))
                    card.setOnClickListener {
                        onClickItem.onImageClick(decodeImage(message.message!!))
                    }
                }
                else -> {
                    convertView = (context as Activity).layoutInflater.inflate(R.layout.thier_image, parent, false)
                    val image = convertView.findViewById<ImageView>(R.id.message_body)
                    val avatar = convertView.findViewById<ImageView>(R.id.avatar)
                    val usernameText = convertView.findViewById<View>(R.id.name) as TextView
                    image.setImageBitmap(decodeImage(message.message!!))
                    usernameText.text = message.username
                    Picasso.get().load(Uri.parse(profileImage)).into(avatar)
                }
            }
        }else{
            when {
                TextUtils.isEmpty(message.message) -> {
                    convertView = (context as Activity).layoutInflater.inflate(R.layout.user_connected, parent, false)
                    val messageText = convertView.findViewById<TextView>(R.id.message_body)
                    val userConnected: String = message.username
                    messageText.text = userConnected
                }
                message.id == ChatActivity.uniqueId -> {
                    convertView = (context as Activity).layoutInflater.inflate(R.layout.my_message, parent, false)
                    val messageText = convertView.findViewById<TextView>(R.id.message_body)
                    messageText.text = message.message
                }
                else -> {
                    convertView = (context as Activity).layoutInflater.inflate(R.layout.their_message, parent, false)
                    val messageText = convertView.findViewById<TextView>(R.id.message_body)
                    val avatar = convertView.findViewById<ImageView>(R.id.avatar)
                    val usernameText = convertView.findViewById<View>(R.id.name) as TextView
                    messageText.visibility = View.VISIBLE
                    usernameText.visibility = View.VISIBLE
                    messageText.text = message.message
                    usernameText.text = message.username
                    Picasso.get().load(Uri.parse(profileImage)).into(avatar)
                }
            }
        }

        return convertView
    }
    fun setImage(img:String){
        profileImage = img
    }
    private fun decodeImage(data: String): Bitmap?{
        val b: ByteArray = Base64.getDecoder().decode(data)
        return BitmapFactory.decodeByteArray(b, 0, b.size)
    }

    interface OnClickItem{
        fun onImageClick(map: Bitmap?)
    }
}