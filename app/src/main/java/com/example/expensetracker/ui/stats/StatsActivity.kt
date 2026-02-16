package com.example.expensetracker.ui.stats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityStatsBinding
import com.example.expensetracker.ui.main.MainActivity
import com.example.expensetracker.ui.profile.ProfileActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var b: ActivityStatsBinding
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Bottom nav
        b.bottomNav.selectedItemId = R.id.nav_stats
        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); true }
                R.id.nav_stats -> true
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Simple: show total spent (all time) for now
        repo.listenExpenses { list, _ ->
            val total = list.sumOf { it.amount }
            b.weekTotalTv.text = "฿%.2f".format(total)
        }
    }
}