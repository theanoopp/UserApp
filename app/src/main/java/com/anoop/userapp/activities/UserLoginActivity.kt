package com.anoop.userapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.anoop.userapp.R
import com.anoop.userapp.dialog.LoadingDialog
import com.anoop.userapp.model.RegUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_login.*

class UserLoginActivity : BaseActivity() {

    private val loadingDialog by lazy { LoadingDialog().apply { isCancelable = false } }

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        supportActionBar!!.title = "Login"

        signup_button.setOnClickListener {
            startActivity(Intent(this, UserRegisterActivity::class.java))
        }

        submit_button.setOnClickListener {
            val email = email_text.text.toString()
            val password = password_input.text.toString()

            when {
                email.isEmpty() -> Toast.makeText(
                    this,
                    "Enter Email",
                    Toast.LENGTH_SHORT
                ).show()
                password.length < 6 -> Toast.makeText(
                    this,
                    "Password should be of at-least 6 characters ",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {

                    loadingDialog.show(supportFragmentManager, "loading_dialog")

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->

                            db.collection("users").document(authResult.user!!.uid).get()
                                .addOnSuccessListener {

                                    if (it.exists()) {

                                        val userObj = it.toObject(RegUser::class.java)

                                        if (userObj!!.admin) {
                                            startActivity(
                                                Intent(
                                                    this@UserLoginActivity,
                                                    AdminHomeActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {

                                            if (userObj.verified) {
                                                val intent = Intent(
                                                    this@UserLoginActivity,
                                                    UserHomeActivity::class.java
                                                )
                                                intent.putExtra("name", userObj.name)
                                                intent.putExtra("address", userObj.address)
                                                intent.putExtra("email", userObj.email)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "You are not verified yes, kindly wait for admin's approval",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                FirebaseAuth.getInstance().signOut()
                                            }

                                        }


                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Unable to Login, Please try again after some time",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    loadingDialog.dismiss()

                                }.addOnFailureListener {

                                    Toast.makeText(
                                        this,
                                        it.localizedMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    loadingDialog.dismiss()

                                }


                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                it.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()

                            loadingDialog.dismiss()
                        }

                }


            }


        }
    }
}
