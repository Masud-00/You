package com.example.you.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.you.R
import com.example.you.Utils
import com.example.you.adapter.MainViewAdapter.*
import com.example.you.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainViewAdapter(options: FirestoreRecyclerOptions<Post>, val listner: IpostAdapter): FirestoreRecyclerAdapter<Post,ViewHolder>(options) {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userImage: ImageView= itemView.findViewById(R.id.userImage)
        val userName: TextView=itemView.findViewById(R.id.userName)
        val createdAt: TextView=itemView.findViewById(R.id.createdAt)
        val postText: TextView=itemView.findViewById(R.id.postText)
        val postImage: ImageView=itemView.findViewById(R.id.postImage)
        val likeImage: ImageView=itemView.findViewById(R.id.likeImage)
        val like: TextView=itemView.findViewById(R.id.like)
        val imageLinearLayout: LinearLayout=itemView.findViewById(R.id.imageLinearLayout)
        val post_delete: ImageView=itemView.findViewById(R.id.post_delete)
        val comment: ImageView=itemView.findViewById(R.id.comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val viewHolder=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_card_view,parent,false))
        viewHolder.likeImage.setOnClickListener {
            listner.onLikeClicked(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }

        viewHolder.post_delete.setOnClickListener {
            listner.postDelete(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        viewHolder.userImage.setOnClickListener {
            listner.openUserProfile(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        viewHolder.comment.setOnClickListener {
            listner.openComments(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Post) {
        val currentUser=FirebaseAuth.getInstance().currentUser
       val db=  Firebase.firestore.collection("user").document(model.createdBy.uid)

        db?.get()?.addOnCompleteListener {
            val name = it.result?.data?.get("userName")
            val userImage=it.result?.data?.get("userImage")
            holder.userName.text = name.toString()
            Glide.with(holder.userImage).load(userImage).circleCrop().into(holder.userImage)
        }
        holder.createdAt.text= Utils.getTimeAgo(model.createdAt)
        holder.postText.text=model.post
        if(model.postImage!=null) {
            holder.imageLinearLayout.visibility = View.VISIBLE
            Glide.with(holder.postImage.context).load(model.postImage).into(holder.postImage)
        }
        else
            holder.imageLinearLayout.visibility=View.GONE

        val auth= Firebase.auth
        val currentU=auth.currentUser?.uid
        val isLiked=model.likeBy.contains(currentU)
        if(isLiked){
            holder.likeImage.setImageDrawable(ContextCompat.getDrawable(holder.likeImage.context,
                R.drawable.ic_like
            ))
        }
        else{
            holder.likeImage.setImageDrawable(ContextCompat.getDrawable(holder.likeImage.context,
                R.drawable.ic_dislike
            ))
        }

        holder.like.text=model.likeBy.size.toString()


        if(FirebaseAuth.getInstance().currentUser?.uid==model.createdBy.uid){
            holder.post_delete.visibility=View.VISIBLE
        }
    }

    interface IpostAdapter{
        fun onLikeClicked(postId:String)
        fun postDelete(postId: String)
        fun openUserProfile(postId: String)
        fun openComments(postId: String)

    }

}