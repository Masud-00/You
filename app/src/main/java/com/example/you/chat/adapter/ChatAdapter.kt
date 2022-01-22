package com.example.you.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.you.R
import com.example.you.Utils
import com.example.you.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(val listener:Context, private val mList: List<ChatMessage>): RecyclerView.Adapter<ChatAdapter.MainViewHolder>() {



    companion object {
        const val Msg_left = 0
        const val Msg_right = 1
    }
     class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val chatText:TextView=itemView.findViewById(R.id.chatText)
        val time:TextView=itemView.findViewById(R.id.time)
         val imageView: ImageView=itemView.findViewById(R.id.imageView)

             }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
                if(viewType== Msg_right) {
            val viewHolder = MainViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.chat_right_card, parent, false)
            )
            return viewHolder
        } else{
            val viewHolder = MainViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.chat_left_card, parent, false)
            )

           return viewHolder
        }
    }


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val currentMessage=mList[position]
        if(currentMessage.message.isNotEmpty()) {
            holder.chatText.visibility=View.VISIBLE
            holder.chatText.text = currentMessage.message
        }
        holder.time.text= Utils.getTimeAgo(currentMessage.createdAt)

        if(currentMessage.imageUrl.isNotEmpty()) {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(holder.imageView).load(currentMessage.imageUrl).into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
     return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentUserId=FirebaseAuth.getInstance().currentUser?.uid.toString()
        if(currentUserId==mList.get(position).sender) {
            return Msg_right
        }
        else
            return Msg_left
    }


}






