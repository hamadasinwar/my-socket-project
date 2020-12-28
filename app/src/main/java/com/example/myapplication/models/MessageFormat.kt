package com.example.myapplication.models

data class MessageFormat(var id: String, var username:String, var message:String?){
    private var isImage = false
    fun setIsImage(){
        isImage = true
    }
    fun getIsImage():Boolean{
        return isImage
    }
}