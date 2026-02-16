package com.example.expensetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivitySignupBinding
import com.example.expensetracker.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySignupBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.goLoginTv.setOnClickListener {
            finish()
        }

        b.signupBtn.setOnClickListener {
            val name = b.nameEt.text?.toString()?.trim().orEmpty()
            val email = b.emailEt.text?.toString()?.trim().orEmpty()
            val pass = b.passwordEt.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    repo.createOrUpdateProfile(name, email) { ok, err ->
                        if (!ok) {
                            Toast.makeText(this, err ?: "Profile save failed", Toast.LENGTH_SHORT).show()
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}