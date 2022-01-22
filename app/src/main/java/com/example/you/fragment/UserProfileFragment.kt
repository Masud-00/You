package com.example.you.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.you.*
import com.example.you.activities.Comment
import com.example.you.activities.Edit
import com.example.you.activities.OtherProfile
import com.example.you.adapter.MainViewAdapter
import com.example.you.chat.ChatUserList
import com.example.you.dao.PostDao
import com.example.you.dao.UserDao
import com.example.you.databinding.FragmentUserProfileBinding
import com.example.you.login.Login
import com.example.you.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserProfileFragment : Fragment(), MainViewAdapter.IpostAdapter {
    private lateinit var auth: FirebaseAuth
    private lateinit var mAdapter: MainViewAdapter
    private lateinit var postDao: PostDao
    private lateinit var binding: FragmentUserProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false)
        setHasOptionsMenu(true)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)

        }

        binding.edit.setOnClickListener {
            val intent = Intent(requireContext(), Edit::class.java)
            startActivity(intent)
        }


            val db = currentUser?.uid?.let { Firebase.firestore.collection("user").document(it) }
            db?.get()?.addOnCompleteListener {
                val name = it.result?.data?.get("userName") as String
                val userImage = it.result?.data?.get("userImage") as String
                val image = binding.userImage
                Glide.with(image.context).load(userImage).circleCrop().into(image)
                binding.userName.text = name
            }
        val postDao = PostDao()
        val postCollection = postDao.postCollection
        val query =
            currentUser?.uid?.let { postCollection.whereEqualTo("createdBy.uid", it)
                .orderBy("createdAt", Query.Direction.DESCENDING) }
        val recyclerViewOption =
            query?.let {
                 FirestoreRecyclerOptions.Builder<Post>().setQuery(it, Post::class.java).build()
            }

            mAdapter = recyclerViewOption?.let { MainViewAdapter(it,this) }!!
            val recyclerView = binding.recycleView
            recyclerView.adapter = mAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            mAdapter.startListening()


        return binding.root

    }


    override fun onLikeClicked(postId: String) {
        postDao.updateLike(postId)

    }
    override fun postDelete(postId: String) {
                    postDao.deletePost(postId)

    }

   override fun openUserProfile(PostId: String) {
       val intent = Intent(requireContext(), OtherProfile::class.java)
       intent.putExtra("PostId",PostId)
       startActivity(intent)
    }
    override fun openComments(PostId: String) {
        val intent = Intent(requireContext(), Comment::class.java)
        intent.putExtra("PostId",PostId)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.chat->{
            startActivity(Intent(requireContext(), ChatUserList::class.java))
            true
        }
        R.id.logout-> {
            auth.signOut()
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
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


