package com.anoop.userapp.activities

import android.content.Intent
import android.os.Bundle
import com.anoop.userapp.R
import com.anoop.userapp.glide.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_user_home.*

class UserHomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        supportActionBar!!.title = "Home"

        home_username.text = intent.getStringExtra("name")
        home_username.text = intent.getStringExtra("address")
        home_username.text = intent.getStringExtra("email")

        val auth = FirebaseAuth.getInstance()

        val storageReference = Firebase.storage.getReference("profileImages/${auth.currentUser!!.uid}.jpg")

        GlideApp.with(this)
            .load(storageReference)
            .placeholder(R.drawable.default_user_profile)
            .into(single_image)


        home_logout_button.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this,SplashActivity::class.java))
            finish()
        }


    }
}
