package com.example.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.activities.SplashActivity
import com.example.myapplication.adapters.AppBarStateChangeListener
import com.example.myapplication.app.App
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.content_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*


class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var act: Activity
    private lateinit var slidDown: Animation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        act = activity as Activity
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.getReference("uploads/")
        (act as MainActivity).setSupportActionBar(root.toolbar)
        (act as MainActivity).supportActionBar!!.title = "Account Info"
        slidDown = AnimationUtils.loadAnimation(act, R.anim.slide_down)
        val user = (act.application as App).getUser()


        root.name.text = user.name
        root.email.text = user.email
        root.profileName.text = user.name
        root.profileEmail.text = user.email
        root.profileGender.text = user.gender
        root.profileAge.text = user.age
        if (user.image != ""){
            root.profileImage.setImageURI(Uri.parse(user.image))
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
                            updateImage(user.id, fileUri!!.path!!)
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        root.signOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(act, SplashActivity::class.java))
            act.finish()
        }

        return root
    }

    private fun updateImage(id: String, img: String){
        db.collection("Users").get().addOnSuccessListener { querySnapshot ->
            for (doc in querySnapshot){
                if (doc.getString("id") == id){
                    db.collection("Users").document(doc.id).update("image", img)
                    (act.application as App).updateUser(img)
                }
            }
        }
    }
}