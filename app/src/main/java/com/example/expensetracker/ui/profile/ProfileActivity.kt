package com.example.expensetracker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityProfileBinding
import com.example.expensetracker.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        repo.getProfile { profile, err ->
            if (profile == null) {
                Toast.makeText(this, err ?: "Profile not found", Toast.LENGTH_SHORT).show()
                return@getProfile
            }

            b.nameTv.text = profile.name
            b.emailTv.text = profile.email
        }

        // Edit Profile (you can create EditProfileActivity later)
        b.editBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        b.backBtn.setOnClickListener {
            finish()
        }





        // Logout
        b.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
    override fun onResume() {
        super.onResume()

        repo.getProfile { profile, err ->
            if (profile != null) {
                b.nameTv.text = profile.name
                b.emailTv.text = profile.email
            } else {
                b.nameTv.text = "Unknown"
                b.emailTv.text = auth.currentUser?.email ?: "-"
                Toast.makeText(this, err ?: "Profile not found", Toast.LENGTH_SHORT).show()
            }
        }

    }

}
