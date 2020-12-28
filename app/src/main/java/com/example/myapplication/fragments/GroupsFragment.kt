package com.example.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.activities.CreateGroupActivity
import com.example.myapplication.activities.GroupChatActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.adapters.GroupAdapter
import com.example.myapplication.app.App
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_groups.view.*
import org.json.JSONArray

class GroupsFragment : Fragment(), GroupAdapter.OnClickItem {

    private lateinit var act: Activity
    private lateinit var app:App
    private lateinit var groups:MutableList<Group>
    private lateinit var db:FirebaseFirestore
    private lateinit var user: User
    private lateinit var users:MutableList<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_groups, container, false)
        act = activity as MainActivity
        app = act.application as App
        groups = app.getAllGroups()
        user = app.getUser()
        users = app.getAllUsers()
        db = FirebaseFirestore.getInstance()

        if (groups.isEmpty()){
            root.text_no_groups.visibility = View.VISIBLE
        }
        var adapter = GroupAdapter(act, groups, this)
        root.rvGroups.layoutManager = LinearLayoutManager(act)
        root.rvGroups.adapter = adapter

        db.collection("Groups").get().addOnSuccessListener { snapshot ->
            groups = mutableListOf()
            val us = users
            us.add(user)
            for (document in snapshot){
                val json = document.get("users").toString()
                val jArray = JSONArray(json)
                val groupUsers = mutableListOf<User>()
                for (x in 0 until jArray.length()){
                    for (u in us){
                        if (u.id == jArray[x].toString()){
                            groupUsers.add(u)
                        }
                    }
                }
                if (json.contains(user.id)){
                    val g = Group(document.get("id").toString(), document.get("name").toString(), groupUsers.distinct() as MutableList)
                    groups.add(g)
                }
            }
            adapter = GroupAdapter(act, groups, this)
            root.rvGroups.adapter = adapter
        }

        root.btn_add_group.setOnClickListener {
            val i = Intent(act, CreateGroupActivity::class.java)
            startActivity(i)
        }
        return root
    }

    override fun onGroupClick(position: Int) {
        val i = Intent(act, GroupChatActivity::class.java)
        i.putExtra("groupID", groups[position].id)
        startActivity(i)
    }
}