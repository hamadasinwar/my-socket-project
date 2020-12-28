@file:Suppress("DEPRECATION")

package com.example.myapplication.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.adapters.AppBarStateChangeListener
import com.example.myapplication.app.App
import com.example.myapplication.models.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var act: Activity
    private lateinit var slidDown: Animation
    private lateinit var app:App
    private lateinit var user:User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        act = activity as Activity
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage
        storageReference = storage.reference
        app = (act.application as App)
        (act as MainActivity).setSupportActionBar(root.toolbar)
        (act as MainActivity).supportActionBar!!.title = "Account Info"
        slidDown = AnimationUtils.loadAnimation(act, R.anim.slide_down)
        user = app.getUser()

        root.name.text = user.name
        root.email.text = user.email
        root.profileName.text = user.name
        root.profileEmail.text = user.email
        root.profileGender.text = user.gender
        root.profileAge.text = user.age
        if (user.image != ""){
            Picasso.get().load(Uri.parse(user.image)).into(root.profileImage)
        }else{
            root.profileImage.setImageResource(R.drawable.ic_person)
        }

        root.app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset < -300 && root.profileContent.y < 0) {
                root.profileContent.y = root.profileContent.y + (-verticalOffset / 200).toFloat()
            }
        })

        root.app_bar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State) {
                if (state.name == "COLLAPSED" && root.profileContent.y > 10.0F) {
                    root.profileContent.y = -55.0F
                }
            }
        })

        root.profileImage.setOnClickListener {
            ImagePicker.with(this).cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start { resultCode, data ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            val fileUri = data?.data
                            root.profileImage.setImageURI(fileUri)
                            updateImage(fileUri!!)
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        root.signOut.setOnClickListener {
            showAlert()
        }

        return root
    }

    private fun updateImage(img: Uri){

        val progressDialog = ProgressDialog(act)
        progressDialog.setTitle("Uploading Image...")
        progressDialog.show()

        storageReference.child("profile/${user.id}").putFile(img).addOnSuccessListener {snapshot->
            progressDialog.dismiss()
            snapshot.storage.downloadUrl.addOnSuccessListener {uri->
                db.collection("Users").whereEqualTo("id", user.id).get().addOnSuccessListener { querySnapshot->
                    db.collection("Users").document(querySnapshot.documents[0].id).update("image", uri.toString()).addOnSuccessListener {
                        user.image = uri.toString()
                    }.addOnFailureListener{ exception ->
                        Log.e("hmd", "${exception.message}")
                    }
                }
            }
        }.addOnFailureListener{ exception ->
            Log.e("hmd", "${exception.message}")
            Toast.makeText(act, "Error!!" ,Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(act)
        builder.setTitle("SignOut")
        builder.setMessage("are you sure you want to sign out?")
        builder.setIcon(R.drawable.ic_warning)
        builder.setPositiveButton("Yes"){_, _ ->
            val sharedPreference =  act.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putBoolean("hasConnection", false)
            editor.apply()
            auth.signOut()
            app.clearData()
            startActivity(Intent(act, LoginActivity::class.java))
            act.finish()
        }
        builder.setNegativeButton("Cancel"){_, _ -> }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}