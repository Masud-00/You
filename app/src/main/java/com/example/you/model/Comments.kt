package com.example.you.model

data class Comments(val textComment: String="",val createdBy:User=User(),val createdAt: Long=0,val postId: String="")
