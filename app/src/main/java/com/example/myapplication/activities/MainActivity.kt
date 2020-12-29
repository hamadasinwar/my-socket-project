package com.example.myapplication.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.app.App
import com.example.myapplication.fragments.GroupsFragment
import com.example.myapplication.fragments.HomeFragment
import com.example.myapplication.fragments.ProfileFragment
import com.example.myapplication.models.User
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main2.*
import nl.joery.animatedbottombar.AnimatedBottomBar
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), AnimatedBottomBar.OnTabSelectListener {

    private lateinit var app:App
    private lateinit var user: User
    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val index = intent.getIntExtra("page", -1)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

        if (!(sharedPreference.getBoolean("hasConnection", false))){
            Toast.makeText(this, "no internet connection!", Toast.LENGTH_SHORT).show()
        }

        app = (this.application as App)
        user = app.getUser()
        mSocket = app.getSocket()
        if (index == 1){
            loadFragment(GroupsFragment())
            navigation.selectTabById(R.id.groupPage)
        }else{
            loadFragment(HomeFragment())
        }
        navigation.setOnTabSelectListener(this)

    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.homeContainer, fragment)
                .commit()
            return true
        }
        return false
    }

    override fun onTabSelected(lastIndex: Int, lastTab: AnimatedBottomBar.Tab?, newIndex: Int, newTab: AnimatedBottomBar.Tab) {
        var fragment: Fragment? = null

        when(newIndex){
            0 -> fragment = HomeFragment()
            1 -> fragment = GroupsFragment()
            2 -> fragment = ProfileFragment()
        }

        loadFragment(fragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        val u = JSONObject()
        u.put("id", this.user.id)
        u.put("name", this.user.name)
        u.put("email", this.user.email)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putBoolean("hasConnection", false)
        editor.apply()
        try {
            mSocket.emit("disConnect user", u)
        } catch (e: JSONException) {
            e.printStackTrace()
        mSocket.disconnect()
        }
    }

}