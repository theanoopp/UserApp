package com.anoop.userapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anoop.userapp.R
import com.anoop.userapp.glide.GlideApp
import com.anoop.userapp.model.RegUser
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_admin_home.*


class AdminHomeActivity : BaseActivity() {

    private lateinit var adapter: FirestoreRecyclerAdapter<RegUser?, UserVH?>

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)
        supportActionBar!!.title = "Admin Home"

        val auth = FirebaseAuth.getInstance()
        admin_logout_button.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        val query = db.collection("users")

        val options = FirestoreRecyclerOptions.Builder<RegUser>()
            .setQuery(query, RegUser::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<RegUser?, UserVH?>(options) {

            override fun onBindViewHolder(holder: UserVH, position: Int, model: RegUser) {

                if (auth.currentUser!!.uid == model.docId) {
                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                } else {
                    holder.itemView.visibility = View.VISIBLE
                }

                holder.nameText.text = model.name
                holder.emailText.text = model.email
                holder.addressText.text = model.address

                val storageReference =
                    Firebase.storage.getReference("profileImages/${model.docId}.jpg")

                GlideApp.with(applicationContext)
                    .load(storageReference)
                    .placeholder(R.drawable.default_user_profile)
                    .into(holder.userImage)

                if (model.verified) {
                    holder.markButton.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_check_disable,
                            null
                        )
                    )
                }

                holder.markButton.setOnClickListener {
                    toggleVerified(model)
                }


            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): UserVH {
                val view: View = LayoutInflater.from(group.context)
                    .inflate(R.layout.single_user_item, group, false)
                return UserVH(view)
            }

            override fun getItemViewType(position: Int): Int {
                return 1
            }
        }

        user_list.setHasFixedSize(true)
        user_list.layoutManager = LinearLayoutManager(this)
        user_list.recycledViewPool.setMaxRecycledViews(1, 0)
        user_list.adapter = adapter


    }

    private fun toggleVerified(user: RegUser) {
        val data = hashMapOf("verified" to !user.verified)
        db.collection("users").document(user.docId).set(data, SetOptions.merge())
    }

    class UserVH(view: View) : RecyclerView.ViewHolder(view) {

        val userImage: CircleImageView = view.findViewById(R.id.single_image)
        val nameText: TextView = view.findViewById(R.id.single_username)
        val emailText: TextView = view.findViewById(R.id.single_email)
        val addressText: TextView = view.findViewById(R.id.single_address)
        val markButton: ImageButton = view.findViewById(R.id.single_check_button)

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
