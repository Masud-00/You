package com.example.you.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.you.dao.UserDao
import com.example.you.databinding.ChatlistBinding
import com.example.you.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.widget.Toast
import com.example.you.R
import com.example.you.chat.adapter.ChatUserListAdapter
import com.example.you.notification.firebase.FirebaseService
import com.example.you.model.ChatList

import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.firestore.QuerySnapshot

import com.google.android.gms.tasks.OnSuccessListener


class ChatUserList : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: ChatlistBinding
    lateinit var userList: ArrayList<ChatList>
    lateinit var mUser:ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.chatlist)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Messages"
        auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid.toString()

        userList = ArrayList()




        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        val db=FirebaseDatabase.getInstance().getReference("chatList").child(currentUid)
        db.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for(snapshot in dataSnapshot.children){
                    val chatlist=snapshot.getValue(ChatList::class.java)
                    if (chatlist != null) {
                        userList.add(chatlist)
                    }
                }
                chatList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("tag", "Failed to read value.", error.toException())
            }

        })


        val recyclerView= binding.recycleView
        recyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
    }

    fun chatList(){


        mUser= ArrayList()
        UserDao().userCollection.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { queryDocumentSnapshots ->
                mUser.clear()
                if (!queryDocumentSnapshots.isEmpty) {
                    val list = queryDocumentSnapshots.documents
                    for (d in list) {
                        val c: User? = d.toObject(User::class.java)
                        for(user in userList){
                            if(c?.uid==user.id){
                                mUser.add(c)
                            }
                        }
                    }
                    val adapter= ChatUserListAdapter(this,mUser)
                    binding.recycleView.adapter=adapter


                } else {

                    Toast.makeText(
                        this,
                        "No data found in Database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .addOnFailureListener(OnFailureListener {
                Toast.makeText(this, "Fail to get the data.", Toast.LENGTH_SHORT)
                    .show()
            })


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun status(status:String){
        val currentUid = auth.currentUser?.uid.toString()
        var hashmap   = HashMap<String,Any>()
        hashmap["status"] = status
        UserDao().userCollection.document(currentUid).update(hashmap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        status("offline")
    }


}

