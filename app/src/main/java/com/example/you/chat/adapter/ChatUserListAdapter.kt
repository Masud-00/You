package com.example.you.chat.adapter



import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.you.R
import com.example.you.chat.Chat
import com.example.you.model.ChatMessage
import com.example.you.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatUserListAdapter(val listener: Context, private val mList: List<User>): RecyclerView.Adapter<ChatUserListAdapter.MainViewHolder>() {

    lateinit var theLastMessage: String
    class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.chatUserImage)
        val username: TextView = itemView.findViewById(R.id.chatUserName)
        val lastMsg:TextView=itemView.findViewById(R.id.lastMsg)
        val status:ImageView=itemView.findViewById(R.id.status)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {

            val viewHolder = MainViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chatlist_card, parent, false
                )
            )
            return viewHolder

    }


    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val currentMessage=mList[position]
        Glide.with(holder.userImage).load(currentMessage.userImage).circleCrop().into(holder.userImage)
        holder.username.text=currentMessage.userName
        lastMessage(currentMessage.uid,holder.lastMsg)
        if(currentMessage.status=="online"){
            holder.status.visibility=View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            val receiverArray= arrayOf(currentMessage.uid,currentMessage.userName,currentMessage.userImage)
            val intent= Intent(listener, Chat::class.java)
            intent.putExtra("receiverId", receiverArray)
            listener.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return mList.size
    }


    fun lastMessage(userId:String,text:TextView){
        theLastMessage="default"
            val currentUser=FirebaseAuth.getInstance().currentUser?.uid
        val db=FirebaseDatabase.getInstance().getReference("chats")
        db.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               for(snapshots in snapshot.children){
                   val chat=snapshots.getValue(ChatMessage::class.java)
                   if(chat?.receiver==currentUser && chat?.sender==userId || chat?.receiver==userId && chat?.sender==currentUser){
                       theLastMessage=chat.message
                   }
               }
                when(theLastMessage){
                    "default"->text.text=""
                    else->
                        text.text=theLastMessage

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

}









