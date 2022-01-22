package com.example.you

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.you.databinding.ActivityMainBinding
import com.example.you.databinding.SplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: SplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.splash_screen)


        val intent=Intent(this,MainActivity::class.java)
        Handler().postDelayed({
            startActivity(intent)
            finish()
        },300)
    }
}