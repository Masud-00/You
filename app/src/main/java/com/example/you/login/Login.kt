package com.example.you.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.you.MainActivity
import com.example.you.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.button).setOnClickListener {
            login()
        }
        findViewById<TextView>(R.id.register).setOnClickListener {
            val intent= Intent(applicationContext,Register::class.java)
            startActivity(intent)
        }
    }
    private fun login(){
        val email=findViewById<TextView>(R.id.email).text.toString()
        val password=findViewById<TextView>(R.id.password).text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful){
                    val intent=Intent(applicationContext,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this,"login fail",Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Toast.makeText(this,"invalid email or password",Toast.LENGTH_SHORT).show()
        }
    }
}