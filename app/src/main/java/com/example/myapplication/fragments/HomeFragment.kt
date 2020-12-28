package com.example.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.activities.ChatActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.adapters.OnlineAdapter
import com.example.myapplication.adapters.RecentAdapter
import com.example.myapplication.app.App
import com.example.myapplication.models.User
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.util.*

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), OnlineAdapter.OnClickItem, RecentAdapter.OnClickItem {

    private lateinit var act:Activity
    private lateinit var allUsers:MutableList<User>
    private lateinit var recentAdapter: RecentAdapter
    private lateinit var onlineAdapter: OnlineAdapter
    private lateinit var app:App
    private lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        act = activity as Activity
        app = (act.application as App)
        user = app.getUser()
        allUsers = (act.application as App).getAllUsers()
        allUsers.remove(user)
        (act as MainActivity).setSupportActionBar(root.homeToolbar)

        if (user.image != ""){
            root.profileImage.setImageURI(Uri.parse(user.image))
        }else{
            root.profileImage.setImageResource(R.drawable.ic_person)
        }

        root.search_button.setOnClickListener {
            root.etSearch.visibility = View.VISIBLE
        }

        root.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        recentAdapter = RecentAdapter(act, allUsers, this)
        root.rvOnline.layoutManager = LinearLayoutManager(act, LinearLayoutManager.HORIZONTAL, false)
        onlineAdapter = OnlineAdapter(act, app.getConnectedUsers().distinct(), this)
        root.rvRecent.layoutManager = LinearLayoutManager(act)
        root.rvRecent.adapter = recentAdapter
        root.rvOnline.adapter = onlineAdapter
        for (i in 0..10){
            Handler().postDelayed({
                onlineAdapter = OnlineAdapter(act, app.getConnectedUsers().distinct(), this)
                root.rvOnline.adapter = onlineAdapter
            }, 2000)
        }
        return root
    }

    override fun onOnlineClick(position: Int) {
        val onlineUsers = app.getConnectedUsers()

        val i: Intent?
        when (onlineUsers[position].id) {
            "createGroup" -> { }
            user.id -> {
                Toast.makeText(act, "That is you", Toast.LENGTH_SHORT).show()
            }
            else -> {
                i = Intent(act, ChatActivity::class.java)
                i.putExtra("userChat", onlineUsers[position])
                startActivity(i)
            }
        }
    }

    override fun onRecentClick(position: Int) {
        val i = Intent(act, ChatActivity::class.java)
        i.putExtra("userChat", allUsers[position])
        startActivity(i)
    }

    private fun filter(s: String){
        val filteredList = mutableListOf<User>()
        for (item in allUsers){
            if (item.name.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))){
                filteredList.add(item)
            }
        }
        recentAdapter.filterList(filteredList)
    }

}