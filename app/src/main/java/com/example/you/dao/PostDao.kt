package com.example.you.dao

import android.util.Log
import com.example.you.model.Post
import com.example.you.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.QueryDocumentSnapshot




class PostDao {
    private val db=Firebase.firestore
    private val auth=Firebase.auth

 val postCollection=db.collection("posts")
    fun addPost(text: String?,postImage:String?){
        val currentUser=auth.currentUser!!.uid
        GlobalScope.launch {
            val userDao=UserDao()
             val user=userDao.getUserById(currentUser).await().toObject(User::class.java)
            val currentTime=System.currentTimeMillis()
           val post= user?.let { Post(text, it,currentTime,postImage) }

            if (post != null) {
                postCollection.document().set(post)

            }
        }
    }
    fun getPostById(postId: String): Task<DocumentSnapshot> {
       return postCollection.document(postId).get()
    }
    fun updateLike(postId: String){
        GlobalScope.launch {
            val currentUser=auth.currentUser!!.uid
            val post=getPostById(postId).await().toObject(Post::class.java)
            val isLike=post?.likeBy?.contains(currentUser)
            if(isLike==true){
                post.likeBy.remove(currentUser)
            }
            else{
                post?.likeBy?.add(currentUser)
            }
            if(post!=null){
                postCollection.document(postId).set(post)
            }
        }
    }
    fun deletePost(postId: String){
        GlobalScope.launch {
        db.collection("posts/$postId/comments").get()
                .addOnCompleteListener { task ->
                    for (snapshot in task.result!!) {
                        db.collection("posts/$postId/comments").document(snapshot.id)
                            .delete()
                    }
                }
            postCollection.document(postId).delete()

        }
    }




}