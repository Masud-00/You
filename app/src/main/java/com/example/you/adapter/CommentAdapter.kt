package com.example.you.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.you.R
import com.example.you.Utils
import com.example.you.dao.PostDao
import com.example.you.model.Comments
import com.example.you.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentAdapter(options: FirestoreRecyclerOptions<Comments>,val listner: ICommentAdapter): FirestoreRecyclerAdapter<Comments, CommentAdapter.ViewHolder>(options) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val userComment: TextView = itemView.findViewById(R.id.userComment)
        val time:TextView=itemView.findViewById(R.id.time)
        val commentDelete:ImageView=itemView.findViewById(R.id.comment_delete)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_card_view, parent, false)
        )
        viewHolder.userImage.setOnClickListener {
            listner.openUserProfile(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        viewHolder.commentDelete.setOnClickListener {
            listner.deleteComment(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
        }
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Comments) {
        Glide.with(holder.userImage.context).load(model.createdBy.userImage).circleCrop()
            .into(holder.userImage)
        holder.userName.text = model.createdBy.userName
        holder.userComment.text = model.textComment
        holder.time.text= Utils.getTimeAgo(model.createdAt)
        if(FirebaseAuth.getInstance().currentUser?.uid==model.createdBy.uid){
            holder.commentDelete.visibility=View.VISIBLE
        }



    }


    interface ICommentAdapter {
        fun openUserProfile(CommentId: String)
        fun deleteComment(CommentId: String)

    }
}