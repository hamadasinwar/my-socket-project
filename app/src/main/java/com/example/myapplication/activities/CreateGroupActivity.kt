package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.CreateGroupAdapter
import com.example.myapplication.app.App
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_group.*
import org.json.JSONArray
import java.util.*

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var app:App
    private lateinit var user: User
    private lateinit var data:MutableList<User>
    private lateinit var db:FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        db = FirebaseFirestore.getInstance()
        app = this.application as App
        user = app.getUser()
        data = app.getAllUsers().distinct() as MutableList

        val adapter = CreateGroupAdapter(this, data)
        rvGroup.layoutManager = LinearLayoutManager(this)
        rvGroup.adapter = adapter

        btn_create_group.setOnClickListener {
            when {
                etGroupName.text.isEmpty() -> {
                    showSnackBar("Please Enter Group Name!", it)
                }
                adapter.checkData() < 2 -> {
                    showSnackBar("You must pick tow users or more", it)
                }
                else -> {
                    btn_create_group.startAnimation()
                    val jArray = JSONArray()
                    jArray.put(user.id)
                    for (item in adapter.getData()){
                        jArray.put(item.id)
                    }
                    val groupData = hashMapOf(
                        "id" to "${UUID.randomUUID()}",
                        "name" to etGroupName.text.toString(),
                        "users" to "$jArray"
                    )
                    db.collection("Groups").add(groupData).addOnSuccessListener {
                        app.addGroup(Group(groupData["id"]!!, groupData["name"]!!, adapter.getData()))
                        val i = Intent(this, MainActivity::class.java)
                        i.putExtra("page", 1)
                        startActivity(i)
                    }.addOnFailureListener { exception ->
                        Log.e("hmd", "${exception.message}")
                    }
                }
            }
        }
    }
    private fun showSnackBar(s: String, v:View){
        Snackbar.make(v, s , Snackbar.LENGTH_LONG).show()
    }
}