package com.example.you.fragment

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.you.R
import com.example.you.dao.PostDao
import com.example.you.dao.UserDao
import com.example.you.databinding.FragmentAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPostFragment : Fragment() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
    }
        lateinit var auth: FirebaseAuth
        lateinit var imageURL: Uri
        lateinit var binding: FragmentAddPostBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_post, container, false)

        auth = FirebaseAuth.getInstance()


        binding.postImage.setOnClickListener {
            checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            STORAGE_PERMISSION_CODE
            )
        }
       imageURL=Uri.EMPTY

        binding.postButton.setOnClickListener {
            if(imageURL==Uri.EMPTY) {
                uploadText()
            }
            else
            uploadPost()
        }

        return binding.root
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
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
            Glide.with(binding.postImage).load(imageURL).override(400,400).into(binding.postImage)
        }
    }


    private fun uploadPost(){
        val postDao = PostDao()
        val text = binding.postText.text.toString()
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("upload Image.....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm__ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val storageReference = FirebaseStorage.getInstance().getReference("post/$fileName")

        val fileRef = storageReference.child(fileName + '.' + getFileExtension(imageURL.toString()))
        val rr = fileRef.putFile(Uri.parse(imageURL.toString())).addOnSuccessListener {
            Toast.makeText(requireContext(), "added", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
            binding.postText.text.clear()
            binding.postImage.setImageResource(R.drawable.ic_image)

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "fail", Toast.LENGTH_SHORT).show()
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
                postDao.addPost(text, downloadUri.toString())

            }
        }


    }
    private fun uploadText(){
        val postDao = PostDao()
        val text = binding.postText.text.toString()
        postDao.addPost(text,null)
        binding.postText.text.clear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun status(status:String){
        val currentUid = auth.currentUser?.uid.toString()
        val hashmap   = HashMap<String,Any>()
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


