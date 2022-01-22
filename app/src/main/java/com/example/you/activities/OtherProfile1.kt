package com.example.you.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.you.chat.Chat
import com.example.you.R
import com.example.you.adapter.MainViewAdapter
import com.example.you.dao.CommentDao
import com.example.you.dao.PostDao
import com.example.you.databinding.OtherProfile1Binding
import com.example.you.login.Login
import com.example.you.model.Comments
import com.example.you.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class OtherProfile1 : AppCompatActivity(), MainViewAdapter.IpostAdapter {
    lateinit var auth: FirebaseAuth
    lateinit var binding: OtherProfile1Binding

    private lateinit var mAdapter: MainViewAdapter
    private lateinit var postDao: PostDao
    private lateinit var commentDao: CommentDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.other_profile1)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Profile"

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser




        if (currentUser == null) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

        }
        postDao = PostDao()
        commentDao = CommentDao()
        val commentId: Array<out String>? = intent.getStringArrayExtra("CommentId")
        GlobalScope.launch {
                val comment =
                    commentId?.get(0)?.let {
                        commentDao.getCommentById(it,commentId[1]).await().toObject(Comments::class.java)
                    }
                val uid = comment?.createdBy?.uid
                Log.d("hello", uid.toString())


                val db = Firebase.firestore.collection("user").document(uid.toString())
            db.get().addOnCompleteListener {
                val name = it.result?.data?.get("userName")
                val userImage = it.result?.data?.get("userImage")
                val image = binding.userImage
                Glide.with(image.context).load(userImage.toString()).circleCrop()
                    .into(image)
                binding.userName.text = name.toString()


                val postCollection =postDao.postCollection
                val query =
                    postCollection.whereEqualTo("createdBy.uid", uid)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                val recyclerViewOption =
                    query.let {
                        FirestoreRecyclerOptions.Builder<Post>()
                            .setQuery(it, Post::class.java)
                            .build()
                    }


                postDao = PostDao()
                mAdapter =
                    recyclerViewOption.let { MainViewAdapter(it, this@OtherProfile1) }
                val recyclerView = binding.recycleView
                recyclerView.adapter = mAdapter
                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                mAdapter.startListening()


            }
            if(currentUser?.uid.toString()!=comment?.createdBy?.uid.toString()){
                runOnUiThread {
                    binding.chat.visibility = View.VISIBLE
                }
            }

            binding.chat.setOnClickListener{
                val receiver=comment?.createdBy?.uid
                val receiverName=comment?.createdBy?.userName
                val receiverImage=comment?.createdBy?.userImage
                val receiverArray= arrayOf(receiver.toString(),receiverName.toString(),receiverImage.toString())
                val intent=Intent(applicationContext, Chat::class.java)
               // intent.putExtra("receiverId",receiver.toString())
                intent.putExtra("receiverId", receiverArray)
                startActivity(intent)
            }


        }

    }
    override fun onLikeClicked(postId: String) {
        postDao.updateLike(postId)

    }

    override fun postDelete(postId: String) {

                    postDao.deletePost(postId)

    }

    override fun openUserProfile(postId: String) {

    }

    override fun openComments(postId: String) {
        val intent = Intent(this, Comment::class.java)
        intent.putExtra("PostId",postId)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}
