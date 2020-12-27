package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.app.App
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private lateinit var i:Intent
    private lateinit var user:User
    private var email: String? = null
    private var hasConnection :Boolean? = null
    private lateinit var mSocket: Socket
    private lateinit var app:App
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var users:MutableList<User>

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        app = (this.application as App)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser
        email = currentUser?.email
        mSocket = app.getSocket()
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        (this.application as App).setUser(User("", "", "", "", "", "", ""))

        if (email.isNullOrEmpty()){
            i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }else{
            i = Intent(this, MainActivity::class.java)
            loadData()
        }
    }

    private fun loadData(){
        db.collection("Users").get().addOnSuccessListener { doc ->
            for (item in doc){
                user = User(item.get("id").toString(), item.get("name").toString(),item.get("email").toString() ,item.get("gender").toString(),item.get("age").toString(),"",item.get("image").toString())
                (this.application as App).addUser(user)
                if (item.get("email").toString() == email){
                    app.setUser(user)
                }
            }
            users = app.getAllUsers()
            db.collection("Groups").get().addOnSuccessListener { snapshot ->
                for (document in snapshot){
                    val json = document.get("users").toString()
                    val jArray = JSONArray(json)
                    val groupUsers = mutableListOf<User>()
                    for (x in 0 until jArray.length()){
                        for (u in users){
                            if (u.id == jArray[x].toString()){
                                groupUsers.add(u)
                            }
                        }
                    }
                    if (json.contains(user.id)){
                        app.addGroup(Group(document.get("id").toString(), document.get("name").toString(), groupUsers))
                    }
                }
                connectSocket()
            }
        }
    }

    private fun connectSocket(){
        mSocket.connect()
        mSocket.on("connect user", onNewUser)
        val u = JSONObject()
        u.put("id", app.getUser().id)
        u.put("name", app.getUser().name)
        u.put("email", app.getUser().email)
        try {
            mSocket.emit("connect user", u)
        } catch (e: JSONException) {
            Log.e("hmd", "${e.message}")
        }
        editor.putBoolean("hasConnection", true)
        hasConnection = true
        editor.commit()
        startActivity(i)
        finish()
    }

    private var onNewUser = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val length = args.size
            if (length == 0) {
                return@Runnable
            }
            val u = args[0].toString()
            var id: String
            var name: String
            var email: String
            try {
                val array = JSONArray(u)
                for (i in 0 until array.length()){
                    id = (array[i] as JSONObject).getString("id")
                    name = (array[i] as JSONObject).getString("name")
                    email = (array[i] as JSONObject).getString("email")
                    val connectUser = User(id, name, email, "", "", "", "")
                    app.addConnectedUser(connectUser)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
    }
}