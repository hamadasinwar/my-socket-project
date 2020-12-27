package com.example.myapplication.app

import android.app.Application
import com.example.myapplication.models.Group
import com.example.myapplication.models.User
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket

class App:Application(){

    private var mSocket: Socket? = IO.socket("http://192.168.1.22:4040")
    private lateinit var user : User
    private val allUsers = mutableListOf<User>()
    private val connectedUsers = mutableListOf<User>()
    private val allGroups = mutableListOf<Group>()

    fun getSocket(): Socket {
        return mSocket!!
    }

    fun getUser(): User{
        return user
    }

    fun setUser(u: User){
        this.user = User(u.id, u.name, u.email, u.gender, u.age, "", u.image)
    }

    fun updateUser(img:String){
        user.image = img
    }

    fun addUser(user: User){
        allUsers.add(user)
    }

    fun addConnectedUser(user: User){
        if (connectedUsers.isEmpty()){
            connectedUsers.add(User("createGroup", "Online Users", "", "", "", "", ""))
        }
        connectedUsers.add(user)
    }

    fun getAllUsers():MutableList<User>{
        return allUsers
    }

    fun getConnectedUsers():MutableList<User>{
        return connectedUsers
    }

    fun addGroup(group: Group){
        allGroups.add(group)
    }

    fun getAllGroups():MutableList<Group>{
        return allGroups
    }

}