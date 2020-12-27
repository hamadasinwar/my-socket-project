package com.example.myapplication.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.activities.SplashActivity
import com.example.myapplication.app.App
import com.example.myapplication.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labo.kaji.fragmentanimations.MoveAnimation
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var act:Activity
    private lateinit var allUsers: MutableList<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_login, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        act = activity as Activity
        allUsers = (act.application as App).getAllUsers()

        root.btn_sign_in.setOnClickListener {
            if (checkData()){
                root.btn_sign_in.startAnimation()
                auth.signInWithEmailAndPassword(root.etEmail.text.toString(), root.etPassword.text.toString())
                    .addOnSuccessListener {
                        for (u in allUsers){
                            if (u.email == root.etEmail.text.toString()){
                                (act.application as App).setUser(u)
                            }
                        }
                        startActivity(Intent(activity, SplashActivity::class.java))
                        act.finish()
                    }.addOnFailureListener{
                        showSnackBar("Error!!")
                        root.btn_sign_in.stopAnimation()
                    }
            }else{
                errorAnim()
            }
        }
        root.createAccount.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.loginContainer, SignUpFragment()).commit()
        }

        return root
    }

    private fun checkData():Boolean{
        var state = true
        if (etEmail.text.isEmpty() ||etPassword.text.isEmpty()){
            showSnackBar("You must fill all fields!!")
            state = false
        }else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches())){
            showSnackBar("you must enter a valid email address")
            state = false
        }else if (etPassword.text.length < 6){
            showSnackBar("password must be more than 6 characters")
            state = false
        }
        return state
    }
    private fun errorAnim(){
        val a = AnimationUtils.loadAnimation(activity, R.anim.shake)
        input_layout.startAnimation(a)
    }
    private fun showSnackBar(s:String){
        Snackbar.make(this.requireView(), s , Snackbar.LENGTH_LONG).show()
    }
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        return if (!enter) {
            MoveAnimation.create(MoveAnimation.LEFT, enter, 500)
        }else{
            MoveAnimation.create(MoveAnimation.RIGHT, enter, 500)
        }
    }

}