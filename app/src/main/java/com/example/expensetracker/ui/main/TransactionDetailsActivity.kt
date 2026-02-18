package com.example.expensetracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityTransactionDetailsBinding

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var b: ActivityTransactionDetailsBinding
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ back button
        b.backBtn.setOnClickListener {
            finish()
        }

        val expenseId = intent.getStringExtra("expenseId") ?: run {
            finish()
            return
        }

        b.editBtn.setOnClickListener {
            startActivity(Intent(this, AddEditExpenseActivity::class.java).putExtra("expenseId", expenseId))
        }

        b.deleteBtn.setOnClickListener {
            repo.deleteExpense(expenseId) { ok, err ->
                if (ok) {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, err ?: "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        repo.getExpense(expenseId) { e, err ->
            if (e == null) {
                Toast.makeText(this, err ?: "Not found", Toast.LENGTH_SHORT).show()
                finish()
                return@getExpense
            }

            b.amountTv.text = "-฿%.2f".format(e.amount)
            b.titleTv.text = e.title
            b.dateTv.text = e.dateText()
            b.categoryValueTv.text = e.category
            b.statusChip.text = "Completed"
            b.txIdValueTv.text = "#${e.id.take(8)}"
        }
    }
}
