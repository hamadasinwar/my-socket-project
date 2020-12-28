package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.adapters.GroupChatAdapter
import com.example.myapplication.app.App
import com.example.myapplication.models.Group
import com.example.myapplication.models.MessageFormat
import com.example.myapplication.models.User
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

@Suppress("DEPRECATION")
class GroupChatActivity : AppCompatActivity() {

    private lateinit var app:App
    private lateinit var users:MutableList<User>
    private lateinit var group:Group
    private lateinit var ids:String
    private var names = ""
    private var username: String? = null
    private var hasConnection :Boolean? = null
    private lateinit var messageAdapter: GroupChatAdapter
    private lateinit var thread2: Thread
    private var startTyping = false
    private var time = 2
    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        app = (application as App)
        mSocket = app.getSocket()
        user = app.getUser()
        users = mutableListOf()
        ids = ""
        username = user!!.name
        uniqueId = user!!.id
        val groupID = intent.getStringExtra("groupID")
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        for (g in app.getAllGroups()){
            if (g.id == groupID){
                group = g
            }
        }
        for (u in group.users){
            ids += "${u.id},"
            if (u.id != user!!.id){
                users.add(u)
                names += "${u.name}, "
            }
        }
        names = names.subSequence(0, names.length-2).toString()
        title = names

        hasConnection = sharedPreference.getBoolean("hasConnection", false)
        mSocket.connect()
        mSocket.on("group chat message", onNewMessage)
        mSocket.on("on typing", onTyping)
        hasConnection = true

        val messageFormatList: List<MessageFormat> = ArrayList()
        messageAdapter = GroupChatAdapter(this, R.layout.item_message, messageFormatList)
        messageListView.adapter = messageAdapter
        sendButton.setOnClickListener {
            sendMessage()
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
                    onTyping.put("groupChat", group.id)
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
        runOnUiThread{
            val data = args[0] as JSONObject
            val username: String
            val message: String
            val id: String
            val groupChatID: String
            try {
                username = data.getString("username")
                message = data.getString("message")
                id = data.getString("uniqueId")
                groupChatID = data.get("userChat").toString()
                val format = MessageFormat(id, username, message)
                if (ids.contains(user!!.id) && groupChatID == group.id){
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
                val userChatID = data.getString("groupChat")
                if (id == uniqueId) {
                    typingOrNot = false
                } else {
                    if (userChatID == group.id){
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
            jsonObject.put("uniqueId", user!!.id)
            jsonObject.put("userChat", group.id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("group chat message", jsonObject)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            Log.i(TAG, "onDestroy: ")
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
                title = names
                startTyping = false
                time = 2
            }
        }
    }

    companion object{
        var uniqueId: String? = null
        var user:User? = null
        const val TAG = "hmd"
    }

}