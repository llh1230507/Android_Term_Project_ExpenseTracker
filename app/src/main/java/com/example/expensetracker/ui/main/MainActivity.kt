package com.example.expensetracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityMainBinding
import com.example.expensetracker.ui.auth.LoginActivity
import com.example.expensetracker.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.expensetracker.ui.stats.StatsActivity
import com.google.android.material.navigation.NavigationBarView
import com.example.expensetracker.R

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = FirestoreRepository()

    private lateinit var adapter: ExpenseAdapter
    private var reg: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.bottomNav.selectedItemId = R.id.nav_home

        b.bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        })


        adapter = ExpenseAdapter(
            items = emptyList(),
            onOpen = { e ->
                startActivity(
                    Intent(this, TransactionDetailsActivity::class.java)
                        .putExtra("expenseId", e.id)
                )
            },
            onEdit = { e ->
                startActivity(Intent(this, AddEditExpenseActivity::class.java).putExtra("expenseId", e.id))
            },
            onDelete = { e ->
                repo.deleteExpense(e.id) { ok, err ->
                    if (!ok) Toast.makeText(this, err ?: "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
        )

        b.expenseRv.layoutManager = LinearLayoutManager(this)
        b.expenseRv.adapter = adapter

        b.addFab.setOnClickListener {
            startActivity(Intent(this, AddEditExpenseActivity::class.java))
        }

        b.profileBtn.setOnClickListener {
            Toast.makeText(this, "Opening profile...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ProfileActivity::class.java))
        }



    }

    override fun onStart() {
        super.onStart()
        reg = repo.listenExpenses { list, err ->
            if (err != null) {
                b.totalTv.text = "Total: -"
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                adapter.submit(emptyList())
                return@listenExpenses
            }

            adapter.submit(list)
            val total = list.sumOf { it.amount }
            b.totalTv.text = "Total: ฿%.2f".format(total)
        }
    }

    override fun onStop() {
        super.onStop()
        reg?.remove()
        reg = null
    }
}
