package com.anoop.userapp.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anoop.userapp.R
import com.anoop.userapp.model.RegUser
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({

            checkPermissions()
        }, 500)
    }

    private fun checkPermissions() {

        val listener = object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                when {
                    report.areAllPermissionsGranted() -> startApp()
                    report.isAnyPermissionPermanentlyDenied -> showSnackbar(
                        splash_view,
                        "Permission are needed for run app",
                        "settings"
                    )
                    else -> showSnackbar(splash_view, "Permission are needed to run app", "allow")
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {

                token.continuePermissionRequest()
            }
        }


        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(listener)
            .check()

    }

    private fun startApp() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            startActivity(Intent(this, UserLoginActivity::class.java))
            finish()
        } else {

            Firebase.firestore.collection("users").document(user.uid).get().addOnSuccessListener {

                if (it.exists()) {

                    val userObj = it.toObject(RegUser::class.java)

                    if (userObj!!.admin) {
                        startActivity(Intent(this@SplashActivity, AdminHomeActivity::class.java))
                    } else {

                        val intent = Intent(this@SplashActivity, UserHomeActivity::class.java)
                        intent.putExtra("name", userObj.name)
                        intent.putExtra("address", userObj.address)
                        intent.putExtra("email", userObj.email)

                        startActivity(intent)
                    }
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "Unable to Login, Please try again after some time",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }.addOnFailureListener {

                Toast.makeText(
                    this,
                    it.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }


    private fun showSnackbar(view: View, text: String, callback: String) {

        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        if (callback == "settings") {
            snackbar.setAction("Settings") {

                val context = view.context
                val myAppSettings = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.packageName)
                )
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
                myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(myAppSettings)

            }
        }
        if (callback == "allow") {
            snackbar.setAction("Allow") {
                checkPermissions()
            }
        }

        snackbar.show()
    }

}
