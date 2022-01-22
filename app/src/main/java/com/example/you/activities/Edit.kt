package com.example.you.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.you.MainActivity
import com.example.you.R
import com.example.you.dao.UserDao
import com.example.you.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*


class Edit: AppCompatActivity() {
    private lateinit var imageURL: Uri

    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Edit"


            imageURL= Uri.EMPTY
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        findViewById<ImageView>(R.id.userImage).setOnClickListener {
            selectImage()
        }
        findViewById<Button>(R.id.Done).setOnClickListener {
            uploadImage()

        }
        val db = currentUser?.uid?.let { Firebase.firestore.collection("user").document(it) }
        db?.get()?.addOnCompleteListener {
            val name = it.result?.data?.get("userName")
            findViewById<TextInputEditText>(R.id.name).setText(name.toString())
            val userImage=it.result?.data?.get("userImage")
            val userImg=  findViewById<ImageView>(R.id.userImage)
            Glide.with(userImg).load(userImage).override(500,500).circleCrop().into(userImg)

        }


//        val db= FirebaseDatabase.getInstance().getReference("User")
//        db.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for(snapshot in dataSnapshot.children){
//                    val chat=snapshot.getValue(User::class.java)
//                  if(chat?.uid.equals(currentUser?.uid)){
//                      val name = chat?.userName
//                        findViewById<TextInputEditText>(R.id.name).setText(name.toString())
//                      val userImage=chat?.userImage
//                      val userImg=  findViewById<ImageView>(R.id.userImage)
//                      Glide.with(userImg).load(userImage).override(500,500).circleCrop().into(userImg)
//                  }
//                }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.w("tag", "Failed to read value.", error.toException())
//            }
//        })

    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageURL = data?.data!!
            val userImg=  findViewById<ImageView>(R.id.userImage)
            Glide.with(userImg.context).load(imageURL).override(500,500).circleCrop().into(userImg)
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }


    private fun uploadImage() {
        val name = findViewById<EditText>(R.id.name).text.toString()
        val currentUser = auth.currentUser
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("uploading file....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val fileName = currentUser?.uid
        val storageReference = FirebaseStorage.getInstance().getReference("image/$fileName")

        val fileRef = storageReference.child(fileName + '.' + getFileExtension(imageURL))
        val rr = fileRef.putFile(Uri.parse(imageURL.toString())).addOnSuccessListener {
            Toast.makeText(this, "SAVE", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
            startActivity(Intent(applicationContext, MainActivity::class.java))


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
                val currentUser = auth.currentUser
                val user = currentUser?.uid?.let { User(it, name, downloadUri.toString()) }
                val userDao = UserDao()
                if (user != null) {
                    userDao.adduser(user)


                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}






