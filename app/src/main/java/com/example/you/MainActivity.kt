package com.example.you

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.you.dao.UserDao
import com.example.you.databinding.ActivityMainBinding
import com.example.you.fragment.AddPostFragment
import com.example.you.fragment.HomeFragment
import com.example.you.fragment.UserProfileFragment
import com.example.you.login.Login
import com.example.you.login.Register
import com.google.firebase.auth.FirebaseAuth
import com.ismaeldivita.chipnavigation.ChipNavigationBar


class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        auth= FirebaseAuth.getInstance()
        val currentUser=auth.currentUser

        if(currentUser==null){
            val intent= Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }else
            makeCurrentFragment(HomeFragment())

        val homeFragment= HomeFragment()
        val addPostFragment= AddPostFragment()
        val profileFragment= UserProfileFragment()


      findViewById<ChipNavigationBar>(R.id.navbar).setOnItemSelectedListener{
          when (it) {
              R.id.home -> makeCurrentFragment(homeFragment)
              R.id.add -> makeCurrentFragment(addPostFragment)
              R.id.profile -> makeCurrentFragment(profileFragment)
          }

      }



    }
    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.myNavHost, fragment).commit()
    }



}