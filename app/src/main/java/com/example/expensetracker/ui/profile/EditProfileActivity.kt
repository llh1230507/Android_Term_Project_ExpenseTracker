package com.example.expensetracker.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth

class EditProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityEditProfileBinding
    private val repo = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Prefill from Firestore (if exists)
        repo.getProfile { p, err ->
            if (p != null) {
                b.nameEt.setText(p.name)
                b.emailEt.setText(p.email)
            } else {
                // fallback: at least show auth email
                b.emailEt.setText(auth.currentUser?.email.orEmpty())
                Toast.makeText(this, err ?: "No saved profile yet", Toast.LENGTH_SHORT).show()
            }
        }

        b.cancelBtn.setOnClickListener { finish() }

        b.saveBtn.setOnClickListener {
            val name = b.nameEt.text?.toString()?.trim().orEmpty()
            val email = b.emailEt.text?.toString()?.trim().orEmpty()

            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(this, "Fill name and email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repo.createOrUpdateProfile(name, email) { ok, msg ->
                if (ok) {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, msg ?: "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
