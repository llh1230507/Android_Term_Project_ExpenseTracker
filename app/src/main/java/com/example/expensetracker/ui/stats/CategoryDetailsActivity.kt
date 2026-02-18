package com.example.expensetracker.ui.stats

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityCategoryDetailsBinding
import com.example.expensetracker.ui.main.ExpenseAdapter

class CategoryDetailsActivity : AppCompatActivity() {

    private lateinit var b: ActivityCategoryDetailsBinding
    private val repo = FirestoreRepository()

    private lateinit var adapter: ExpenseAdapter
    private var reg: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCategoryDetailsBinding.inflate(layoutInflater)
        setContentView(b.root)

        val category = intent.getStringExtra("category") ?: run { finish(); return }
        val start = intent.getLongExtra("start", 0L)
        val end = intent.getLongExtra("end", Long.MAX_VALUE)

        b.titleTv.text = category
        b.backBtn.setOnClickListener { finish() }

        adapter = ExpenseAdapter(
            items = emptyList(),
            onOpen = { /* optional: open TransactionDetailsActivity */ },
            onEdit = { /* optional */ },
            onDelete = { /* optional */ }
        )

        b.expenseRv.layoutManager = LinearLayoutManager(this)
        b.expenseRv.adapter = adapter

        // simplest: reuse listenExpenses and filter locally
        reg = repo.listenExpenses { list, err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                adapter.submit(emptyList())
                b.totalTv.text = "฿0.00"
                return@listenExpenses
            }

            val filtered = list.filter {
                it.category.trim() == category.trim() && it.date in start until end
            }.sortedByDescending { it.date }

            adapter.submit(filtered)
            b.totalTv.text = "฿%.2f".format(filtered.sumOf { it.amount })
        }
    }

    override fun onStop() {
        super.onStop()
        reg?.remove()
        reg = null
    }
}
