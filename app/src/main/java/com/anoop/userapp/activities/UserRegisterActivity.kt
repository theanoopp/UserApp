package com.anoop.userapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.anoop.userapp.R
import com.anoop.userapp.dialog.LoadingDialog
import com.anoop.userapp.model.RegUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_user_register.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class UserRegisterActivity : BaseActivity() {

    private val loadingDialog by lazy { LoadingDialog().apply { isCancelable = false } }

    private var uri: Uri? = null

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Register..."

        login_button.setOnClickListener {
            onBackPressed()
        }

        upload_image_button.setOnClickListener {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1).start(this)

        }

        submit_button.setOnClickListener {

            val name = name_input.text.toString()
            val address = address_input.text.toString()
            val email = email_text.text.toString()
            val password = password_input.text.toString()

            when {
                name.isEmpty() -> Toast.makeText(
                    this,
                    "Enter Name",
                    Toast.LENGTH_SHORT
                ).show()
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

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->

                            val userId = authResult.user!!.uid

                            val userObj = RegUser(name, address, email)

                            db.collection("users")
                                .document(userId).set(userObj).addOnSuccessListener {

                                    if (uri != null) {
                                        val storage = Firebase.storage.reference
                                        val thumbFile = File(uri!!.path!!)
                                        try {
                                            val thumbBitmap = Compressor(this)
                                                .setMaxWidth(200)
                                                .setMaxHeight(200)
                                                .setQuality(50)
                                                .compressToBitmap(thumbFile)

                                            val byteArray = ByteArrayOutputStream()

                                            thumbBitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                50,
                                                byteArray
                                            )
                                            val thumbByte = byteArray.toByteArray()
                                            val thumbRef =
                                                storage.child("profileImages/$userId.jpg")
                                            thumbRef.putBytes(thumbByte)
                                                .addOnCompleteListener {
                                                    loadingDialog.dismiss()
                                                    Toast.makeText(
                                                        this,
                                                        "Please wait till your admin verifies your profile",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish()
                                                }


                                        } catch (e: IOException) {
                                            loadingDialog.dismiss()
                                            Toast.makeText(
                                                this,
                                                "Please wait till your admin verifies your profile",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }


                                    } else {
                                        loadingDialog.dismiss()
                                        Toast.makeText(
                                            this,
                                            "Please wait till your admin verifies your profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {

                uri = result.uri
                single_image.setImageURI(uri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@UserRegisterActivity, error.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}
