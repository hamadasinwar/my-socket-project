package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.R
import com.example.myapplication.adapters.MessageAdapter
import com.example.myapplication.app.App
import com.example.myapplication.models.MessageFormat
import com.example.myapplication.models.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.image_send.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity(), MessageAdapter.OnClickItem {

    private var username: String? = null
    private var hasConnection :Boolean? = null
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var thread2: Thread
    private var startTyping = false
    private var time = 2
    private lateinit var mSocket: Socket
    private lateinit var app : App
    private lateinit var user: User
    private lateinit var userChat:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app = (this.application as App)
        mSocket = app.getSocket()
        user = app.getUser()
        userChat = intent.getParcelableExtra("userChat")!!
        username = user.name
        uniqueId = user.id
        title = userChat.name
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        hasConnection = sharedPreference.getBoolean("hasConnection", false)
        mSocket.connect()
        mSocket.on("chat message", onNewMessage)
        mSocket.on("on typing", onTyping)
        mSocket.on("image message", onNewImage)
        hasConnection = true

        val messageFormatList: List<MessageFormat> = ArrayList()
        messageAdapter = MessageAdapter(this, R.layout.item_message, messageFormatList, this)
        messageListView.adapter = messageAdapter
        messageAdapter.setImage(userChat.image)

        sendButton.setOnClickListener {
            sendMessage()
        }

        sendImage.setOnClickListener {
            ImagePicker.with(this).compress(1024)
                .maxResultSize(1080, 1080)
                .start { resultCode, data ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            val fileUri = data?.data
                            val v = layoutInflater.inflate(R.layout.image_send, null)
                            v.imageSend.setImageURI(fileUri)
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Send Image?")
                            builder.setView(v)
                            builder.setPositiveButton("Yes"){_, _ ->
                                sendImage(encodeImage(v.imageSend))
                            }
                            builder.setNegativeButton("Cancel"){_, _ ->}
                            val alertDialog: AlertDialog = builder.create()
                            alertDialog.setCancelable(false)
                            alertDialog.show()
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

        }

        onTypeButtonEnable()
        editor.apply()
    }

    private fun onTypeButtonEnable() {
        textField.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", true)
                    onTyping.put("username", username)
                    onTyping.put("uniqueId", uniqueId)
                    onTyping.put("userChat", userChat.id)
                    mSocket.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                sendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private var onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            val username: String
            val message: String
            val id: String
            val userChatID: String
            try {
                username = data.getString("username")
                message = data.getString("message")
                id = data.getString("uniqueId")
                userChatID = data.get("userChat").toString()
                val format = MessageFormat(id, username, message)

                if ((userChatID == user.id || id == user.id) && userChatID != id) {
                    messageAdapter.add(format)
                }
            } catch (e: Exception) {
                Log.e("hmd", "Error: ${e.message}")
            }
        }
    }
    private var onTyping = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            try {
                var typingOrNot = data.getBoolean("typing")
                val userName = data.getString("username") + " is Typing....."
                val id = data.getString("uniqueId")
                val userChatID = data.getString("userChat")
                if (id == uniqueId) {
                    typingOrNot = false
                } else {
                    if (userChatID == user.id){
                        title = userName
                    }
                }
                if (typingOrNot) {
                    if (!startTyping) {
                        startTyping = true
                        thread2 = Thread{
                            while (time > 0) {
                                synchronized(this) {
                                    try {
                                        Thread.sleep(1000)
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    }
                                    time--
                                }
                                handler2.sendEmptyMessage(0)
                            }
                        }
                        thread2.start()
                    } else {
                        time = 2
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    private var onNewImage = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            val username: String
            val image: String
            val id: String
            val userChatID: String
            try {
                username = data.getString("username")
                image = data.getString("image")
                id = data.getString("uniqueId")
                userChatID = data.get("userChat").toString()
                val format = MessageFormat(id, username, image)
                format.setIsImage()

                if ((userChatID == user.id || id == user.id) && userChatID != id) {
                    messageAdapter.add(format)
                }
            } catch (e: Exception) {
                Log.e("hmd", "Error: ${e.message}")
            }
        }
    }

    private fun sendMessage() {
        val message = textField.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(message)) {
            return
        }
        textField.setText("")
        val jsonObject = JSONObject()
        try {
            jsonObject.put("message", message)
            jsonObject.put("username", username)
            jsonObject.put("uniqueId", uniqueId)
            jsonObject.put("userChat", userChat.id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("chat message", jsonObject)
    }

    private fun sendImage(path: String){
        val sendData = JSONObject()
        try {
            sendData.put("image", path)
            sendData.put("username", username)
            sendData.put("uniqueId", uniqueId)
            sendData.put("userChat", userChat.id)
            mSocket.emit("image message", sendData)
        } catch (e: JSONException) {
            Log.e("hmd", "Error: ${e.message}")
        }
    }

    private fun encodeImage(image:ImageView):String{
        val bm = image.drawable.toBitmap()
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.getEncoder().encodeToString(b)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            val userId = JSONObject()
            try {
                userId.put("username", "$username DisConnected")
                mSocket.emit("connect user", userId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            mSocket.disconnect()
            mSocket.off("chat message", onNewMessage)
            mSocket.off("on typing", onTyping)
            username = ""
            messageAdapter.clear()
        } else {
            Log.i(TAG, "onDestroy: is rotating.....")
        }
    }

    private var handler2: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (time == 0) {
                title = userChat.name
                startTyping = false
                time = 2
            }
        }
    }

    companion object{
        var uniqueId: String? = null
        const val TAG = "hmd"
    }

    override fun onImageClick(map: Bitmap?) {
        val imageView = ImageView(this)
        imageView.setImageBitmap(map)
        imageView.setPadding(10, 10, 10, 10)
        val builder = AlertDialog.Builder(this)
        builder.setView(imageView)
        builder.setPositiveButton("Exit"){_, _ -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}