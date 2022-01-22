package com.example.you.chat


import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.you.notification.NotificationData
import com.example.you.notification.PushNotification
import com.example.you.R
import com.example.you.notification.RetrofitInstance
import com.example.you.chat.adapter.ChatAdapter
import com.example.you.dao.ChatMessageDao
import com.example.you.dao.UserDao
import com.example.you.databinding.ChatBinding
import com.example.you.notification.firebase.FirebaseService
import com.example.you.model.ChatMessage
import com.example.you.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.common.io.Files
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Chat: AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }
    lateinit var auth: FirebaseAuth
    lateinit var binding: ChatBinding
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var userDao: UserDao
    lateinit var mchat: ArrayList<ChatMessage>
    lateinit var imageURL: Uri
    lateinit var receiverUser:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.chat)
        val receiver: Array<out String>? = intent.getStringArrayExtra("receiverId")
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val currentUid = currentUser?.uid
        var topic = ""
        userDao = UserDao()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = receiver!![1]


        receiverUser=receiver[0];


        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null && !TextUtils.isEmpty(task.result)) {
                        FirebaseService.token = task.result
                    }
                }
            }

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$currentUid")

        imageURL=Uri.EMPTY


        chatMessageDao = ChatMessageDao()
        binding.chatSent.setOnClickListener {
            val chatText = binding.chatText.text.toString()
            if(imageURL== Uri.EMPTY) {
                if (chatText.isNotEmpty()) {
                    receiver[0].let { it1 -> chatMessageDao.addChat(chatText, it1,"") }
                    binding.chatText.text.clear()
                    addChatList(receiver[0])
                    addChatList1(receiver[0])

                    topic = "/topics/${receiver[0]}"
                    GlobalScope.launch {
                        NotificationData(
                            UserDao().getUserById(currentUid!!).await()
                                .toObject(User::class.java)?.userName.toString(), chatText
                        ).let { it2 ->
                            PushNotification(it2, topic).also {
                                sendNotification(it)
                            }
                        }
                    }
                }
            }else{
                uploadPost()
                topic = "/topics/${receiver[0]}"
                GlobalScope.launch {
                    NotificationData(
                        UserDao().getUserById(currentUid!!).await()
                            .toObject(User::class.java)?.userName.toString(), "image"
                    ).let { it2 ->
                        PushNotification(it2, topic).also {
                            sendNotification(it)
                        }
                    }
                }
            }
}

        binding.imageSend.setOnClickListener {
            checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
              STORAGE_PERMISSION_CODE
            )
        }


        val receiverUser = User(receiver[0], receiver[1], receiver[2])

        if (currentUid != null) {
            readMessage(receiverUser,currentUid)
        }


    }
    fun readMessage(receiverUser:User,senderId:String){
        mchat= ArrayList<ChatMessage>()
        val db=FirebaseDatabase.getInstance().getReference("chats")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mchat.clear()
                    for(snapshot in dataSnapshot.children){
                        val chat=snapshot.getValue(ChatMessage::class.java)
                        if(chat?.receiver.toString() == senderId && chat?.sender.toString() == receiverUser.uid || chat?.receiver.toString() == receiverUser.uid && chat?.sender.toString() == senderId){
                            if (chat != null) {
                                mchat.add(chat)
                            }

                            val recyclerView= binding.recycleView
                            recyclerView.layoutManager=LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL,false)
                            recyclerView.scrollToPosition(mchat.size-1)
                            val adpater= ChatAdapter(this@Chat,mchat)
                            binding.recycleView.adapter=adpater
                        }
                    }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("tag", "Failed to read value.", error.toException())
            }
        })
    }

    private fun addChatList(receiverId:String){
        val currentUid=auth.currentUser?.uid.toString()
        val db=FirebaseDatabase.getInstance().getReference("chatList").child(currentUid).child(receiverId)
        db.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    db.child("id").setValue(receiverId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("tag", "Failed to read value.", error.toException())
            }

        })

    }
    fun addChatList1(receiverId:String){
        val currentUid=auth.currentUser?.uid.toString()
        val db1=FirebaseDatabase.getInstance().getReference("chatList").child(receiverId).child(currentUid)
        db1.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    db1.child("id").setValue(currentUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("tag", "Failed to read value.", error.toException())
            }

        })
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    //for online and offline

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


    //for image send

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            selectImage()
        }
    }


    private fun selectImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == AppCompatActivity.RESULT_OK) {
            imageURL = data?.data!!

        }
    }


    private fun uploadPost(){
        val text = binding.chatText.text.toString()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("upload Image.....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm__ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val storageReference = FirebaseStorage.getInstance().getReference("chat/$fileName")

        val fileRef = storageReference.child(fileName + '.' + Files.getFileExtension(imageURL.toString()))
        val rr = fileRef.putFile(Uri.parse(imageURL.toString())).addOnSuccessListener {
            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
            binding.chatText.text.clear()


        }.addOnFailureListener {
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
        }

        rr.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                chatMessageDao.addChat(text,receiverUser,downloadUri.toString())
                imageURL= Uri.EMPTY

            }
        }


    }









    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("noti", "Response: okk")

            } else {
                Log.e("noti", response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e("noti", e.toString())
        }
    }
}
