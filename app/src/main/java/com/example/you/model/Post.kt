package com.example.you.model

data class Post(val post: String?="",val createdBy: User=User(),val createdAt: Long=0,val postImage: String?="",val likeBy: ArrayList<String> = ArrayList())
