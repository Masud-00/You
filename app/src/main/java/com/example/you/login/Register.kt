package com.example.you.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.you.MainActivity
import com.example.you.R
import com.example.you.activities.Edit
import com.example.you.dao.UserDao
import com.example.you.model.User
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var userDao: UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        auth = FirebaseAuth.getInstance()
        userDao= UserDao()

        findViewById<Button>(R.id.button).setOnClickListener {
            register()
        }

        findViewById<TextView>(R.id.login).setOnClickListener {
            val intent= Intent(applicationContext,Login::class.java)
            startActivity(intent)
        }
    }
    private fun register(){
        val userName=findViewById<TextView>(R.id.userName).text.toString()
        val email=findViewById<TextView>(R.id.email).text.toString()
        val password=findViewById<TextView>(R.id.password).text.toString()

        if(email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful){
                    val userId=auth.currentUser?.uid.toString()
                    val user=User(userId,userName)
                    userDao.adduser(user)
                    val intent= Intent(applicationContext, Edit::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this,"Registration fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,"invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }
}