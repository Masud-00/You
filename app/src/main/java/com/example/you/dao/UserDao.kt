package com.example.you.dao

import com.example.you.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val db=FirebaseFirestore.getInstance()
 val userCollection=db.collection("user")
    fun adduser(user: User){
        user.let {
            GlobalScope.launch(Dispatchers.IO) {
                 userCollection.document(user.uid).set(it)

            }
        }
    }


    fun getUserById(userId:String): Task<DocumentSnapshot> {
        return userCollection.document(userId).get()
    }
}