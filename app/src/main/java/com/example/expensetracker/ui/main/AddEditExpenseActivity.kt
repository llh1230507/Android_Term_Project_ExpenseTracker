package com.example.expensetracker.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityAddEditExpenseBinding

class AddEditExpenseActivity : AppCompatActivity() {
    private lateinit var b: ActivityAddEditExpenseBinding
    private val repo = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddEditExpenseBinding.inflate(layoutInflater)
        setContentView(b.root)

        val expenseId = intent.getStringExtra("expenseId")

        b.titlePageTv.text = if (expenseId == null) "Add Transaction" else "Edit Transaction"
        b.saveBtn.text = if (expenseId == null) "Save Transaction" else "Update Transaction"

        if (expenseId != null) {
            repo.getExpense(expenseId) { e, err ->
                if (err != null) {
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                    return@getExpense
                }
                if (e != null) {
                    b.titleEt.setText(e.title)
                    b.categoryEt.setText(e.category)
                    b.amountEt.setText(e.amount.toString())
                }
            }
        }

        b.saveBtn.setOnClickListener {
            val title = b.titleEt.text?.toString()?.trim().orEmpty()
            val category = b.categoryEt.text?.toString()?.trim().orEmpty()
            val amountStr = b.amountEt.text?.toString()?.trim().orEmpty()

            if (title.isEmpty() || category.isEmpty() ||  amountStr.isEmpty()) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expenseId == null) {
                repo.addExpense(title, category, amount) { ok, err ->
                    if (!ok) Toast.makeText(this, err ?: "Save failed", Toast.LENGTH_SHORT).show()
                    else finish()
                }
            } else {
                repo.updateExpense(expenseId, title, category, amount) { ok, err ->
                    if (!ok) Toast.makeText(this, err ?: "Update failed", Toast.LENGTH_SHORT).show()
                    else finish()
                }
            }
        }

        b.cancelBtn.setOnClickListener {
            finish()
        }
    }
}