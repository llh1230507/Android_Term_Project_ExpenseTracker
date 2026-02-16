package com.example.expensetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.databinding.ActivityLoginBinding
import com.example.expensetracker.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.goSignupTv.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        b.loginBtn.setOnClickListener {
            val email = b.emailEt.text?.toString()?.trim().orEmpty()
            val pass = b.passwordEt.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}