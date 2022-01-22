package com.example.you.fragment


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.you.chat.ChatUserList
import com.example.you.activities.Comment
import com.example.you.adapter.MainViewAdapter
import com.example.you.activities.OtherProfile
import com.example.you.R
import com.example.you.dao.PostDao
import com.example.you.dao.UserDao
import com.example.you.databinding.FragmentHomeBinding
import com.example.you.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class HomeFragment : Fragment(), MainViewAdapter.IpostAdapter {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var postDao: PostDao
    lateinit var auth: FirebaseAuth
    private lateinit var mAdapter: MainViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)

         postDao = PostDao()
        val postCollection = postDao.postCollection
        val query =
           postCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOption =
            query.let {
                FirestoreRecyclerOptions.Builder<Post>().setQuery(it, Post::class.java).build()
            }


        mAdapter = MainViewAdapter(recyclerViewOption,this)
        val recyclerView = binding.recycleView
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mAdapter.startListening()


    return binding.root
    }




   override fun onLikeClicked(postId: String) {
        postDao.updateLike(postId)

    }

   override  fun postDelete(postId: String) {

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
        inflater.inflate(R.menu.chatbutton, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.chat-> {
            startActivity(Intent(requireContext(), ChatUserList::class.java))
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