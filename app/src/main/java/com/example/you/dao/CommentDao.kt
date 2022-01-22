package com.example.you.dao

import com.example.you.model.Comments
import com.example.you.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentDao {
    private val db= Firebase.firestore
    private val auth=Firebase.auth

        class AddCommentCollection(postId: String){
             val db= Firebase.firestore
            val commentCollection = db.collection("posts/$postId/comments")


        }

        fun addComment(text: String, postId: String) {
            val commentCollection = db.collection("posts/$postId/comments")
            val currentUser = auth.currentUser!!.uid
            GlobalScope.launch {
                val userDao = UserDao()
                val user = userDao.getUserById(currentUser).await().toObject(User::class.java)
                val currentTime = System.currentTimeMillis()
                val comment = user?.let { Comments(text, it, currentTime, postId) }
                if (comment != null) {
                    commentCollection.document().set(comment)
                }
            }
        }

        fun getCommentById(CommentId: String,postId:String): Task<DocumentSnapshot> {
            return  db.collection("posts/$postId/comments").document(CommentId).get()

        }



    fun deleteComment(commentId: String,postId: String,){

        db.collection("posts/$postId/comments").document(commentId).delete()
    }
    }
