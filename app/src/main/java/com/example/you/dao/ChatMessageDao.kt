package com.example.you.dao

import com.example.you.model.Comments
import com.example.you.model.ChatMessage
import com.example.you.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatMessageDao {

    private val auth= Firebase.auth
    val currentUser=auth.currentUser!!.uid

    fun addChat(message:String,receiver:String,imageUrl:String){
                GlobalScope.launch {
            val currentTime=System.currentTimeMillis()
            val chat=ChatMessage(message,receiver, currentUser,currentTime,imageUrl)
            if (chat != null) {
                val db=FirebaseDatabase.getInstance().getReference("chats")
                db.push().setValue(chat)
            }
        }



    }





}