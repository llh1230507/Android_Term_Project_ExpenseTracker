package com.example.expensetracker.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var b: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.backBtn.setOnClickListener { finish() }

        val email = auth.currentUser?.email ?: "-"
        b.emailTv.text = email

        repo.getProfile { profile, err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                return@getProfile
            }
            b.nameTv.text = profile?.name ?: "No name"
        }
    }
}
