package com.example.myapplication.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.activities.SplashActivity
import com.example.myapplication.app.App
import com.example.myapplication.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.labo.kaji.fragmentanimations.MoveAnimation
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import java.util.*

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var gender:String
    private lateinit var age:String
    private lateinit var user:User
    private lateinit var arrayAge:MutableList<String>
    private lateinit var act:Activity
    private lateinit var allUsers: MutableList<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_sign_up, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        act = activity as Activity
        allUsers = (act.application as App).getAllUsers()

        arrayAge = mutableListOf()
        arrayAge.add("Age")
        for (x in 12..99){
            arrayAge.add(x.toString())
        }
        val arrayAdapter = ArrayAdapter(activity as Activity, android.R.layout.simple_spinner_item, arrayAge)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        root.age.adapter = arrayAdapter

        root.Login.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.loginContainer, LoginFragment()).commit()
        }

        root.gender.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>) {
                gender = ""
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gender = root.gender.selectedItem.toString()
            }
        }
        root.age.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>) {
                age = ""
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                age = root.age.selectedItem.toString()
            }
        }

        root.btn_sign_up.setOnClickListener {
            if (checkData()){
                root.btn_sign_up.startAnimation()
                val data = mapOf(
                    "id" to user.id,
                    "name" to user.name,
                    "gender" to user.gender,
                    "age" to user.age,
                    "email" to user.email,
                    "image" to user.image
                )
                auth.createUserWithEmailAndPassword(user.email, user.password).addOnSuccessListener {
                    db.collection("Users").add(data).addOnSuccessListener {
                        val i = Intent(activity, SplashActivity::class.java)
                        for (u in allUsers){
                            if (u.email == root.etEmail.text.toString()){
                                (act.application as App).setUser(u)
                            }
                        }
                        startActivity(i)
                        act.finish()
                    }.addOnFailureListener{exception ->
                        Log.e("hmd", exception.message.toString())
                        showSnackBar("Error")
                        root.btn_sign_up.stopAnimation()
                    }
                }.addOnFailureListener{exception ->
                    Log.e("hmd", exception.message.toString())
                    showSnackBar("Error")
                    root.btn_sign_up.stopAnimation()
                }
            }
        }
        return root
    }

    private fun checkData(): Boolean{
        var state = true
        if (etName.text.isEmpty() || etEmail.text.isEmpty() || gender.isEmpty()|| gender == "Gender" || age == "" || age == "Age" || etPassword.text.isEmpty()){
            showSnackBar("You must fill all fields!!")
            errorAnim(input_layout)
            state = false
        }else if (etName.text.length < 3){
            showSnackBar("name must be more than 3 characters")
            errorAnim(input_layout)
            state = false
        }else if (!(android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches())){
            showSnackBar("you must enter a valid email address")
            errorAnim(input_layout)
            state = false
        }else if (etPassword.text.length < 6){
            showSnackBar("password must be more than 6 characters")
            errorAnim(input_layout)
            state = false
        }else if (!(cb_terms.isChecked)){
            showSnackBar("you must agree to our terms of service and policies")
            errorAnim(terms_layout)
            state = false
        }
        user = User(UUID.randomUUID().toString(), etName.text.toString(), etEmail.text.toString(),
            gender, age, etPassword.text.toString(), "")
        return state
    }
    @SuppressLint("SetTextI18n")
    private fun showSnackBar(s:String){
        Snackbar.make(this.requireView(), s , Snackbar.LENGTH_LONG).show()
    }
    private fun errorAnim(v: View){
        val a = AnimationUtils.loadAnimation(activity, R.anim.shake)
        v.startAnimation(a)
    }
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        return if (enter) {
            MoveAnimation.create(MoveAnimation.LEFT, enter, 500)
        }else{
            MoveAnimation.create(MoveAnimation.RIGHT, enter, 500)
        }
    }
}