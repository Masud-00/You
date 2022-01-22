package com.example.you.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.you.R
import com.example.you.adapter.CommentAdapter
import com.example.you.chat.Chat
import com.example.you.dao.CommentDao
import com.example.you.databinding.CommentBinding
import com.example.you.model.Comments
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class Comment: AppCompatActivity(), CommentAdapter.ICommentAdapter{
    lateinit var auth: FirebaseAuth
    lateinit var binding: CommentBinding
    lateinit var commentDao: CommentDao
    private lateinit var mAdapter: CommentAdapter
    lateinit var postId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.comment)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Comments"

        commentDao= CommentDao()

         postId = intent.getStringExtra("PostId").toString()
       Log.d("post",postId.toString())


            binding.commentSend.setOnClickListener {
                val commentText=binding.comment.text.toString()
                if(commentText.isNotEmpty()) {
                    commentDao.addComment(commentText, postId.toString())
                    binding.comment.text.clear()
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show()
                }
            }
//        val viewModelFactory= postId?.let { ViewModelFactory(it) }
//        viewModel= viewModelFactory?.let {
//            ViewModelProvider(this,
//                it
//            ).get(MainViewModel::class.java)
//        }!!

        val commentDao=CommentDao()
        val commentCollection= CommentDao.AddCommentCollection(postId.toString()).commentCollection
        val query1=commentCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions=
            FirestoreRecyclerOptions.Builder<Comments>().setQuery(query1, Comments::class.java).build()


        mAdapter = CommentAdapter(recyclerViewOptions,this)
        val recyclerView = binding.recycleView
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.startListening()


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun openUserProfile(CommentId: String) {
        val receiverArray= arrayOf(CommentId,postId)
        val intent= Intent(this, OtherProfile1::class.java)
        intent.putExtra("CommentId", receiverArray)
       startActivity(intent)
    }



   override fun deleteComment(CommentId: String) {
       Log.d("post",postId.toString())
        commentDao.deleteComment(CommentId,postId)
    }


}


