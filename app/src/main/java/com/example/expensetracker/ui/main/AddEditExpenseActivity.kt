package com.example.expensetracker.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.R
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityAddEditExpenseBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditExpenseActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddEditExpenseBinding
    private val repo = FirestoreRepository()

    private val cal = Calendar.getInstance()
    private var selectedDateMillis: Long = System.currentTimeMillis()

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun openDatePicker() {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            selectedDateMillis = cal.timeInMillis
            b.dateEt.setText(formatDate(selectedDateMillis))
        }, y, m, d).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddEditExpenseBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ Dropdown categories
        val categories = resources.getStringArray(R.array.expense_categories)
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        b.categoryEt.setAdapter(catAdapter)

        // ✅ default date = today
        b.dateEt.setText(formatDate(selectedDateMillis))
        b.dateEt.setOnClickListener { openDatePicker() }

        val expenseId = intent.getStringExtra("expenseId")

        b.titlePageTv.text = if (expenseId == null) "Add Transaction" else "Edit Transaction"
        b.saveBtn.text = if (expenseId == null) "Save Transaction" else "Update Transaction"

        // ✅ If editing, load expense and prefill
        if (expenseId != null) {
            repo.getExpense(expenseId) { e, err ->
                if (err != null) {
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                    return@getExpense
                }
                if (e != null) {
                    b.titleEt.setText(e.title)
                    b.categoryEt.setText(e.category, false)
                    b.amountEt.setText(e.amount.toString())

                    // ✅ load date too
                    selectedDateMillis = e.date
                    cal.timeInMillis = selectedDateMillis
                    b.dateEt.setText(formatDate(selectedDateMillis))
                }
            }
        }

        b.saveBtn.setOnClickListener {
            val title = b.titleEt.text?.toString()?.trim().orEmpty()
            val category = b.categoryEt.text?.toString()?.trim().orEmpty()
            val amountStr = b.amountEt.text?.toString()?.trim().orEmpty()

            if (title.isEmpty() || category.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Amount must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expenseId == null) {
                repo.addExpense(title, category, amount, selectedDateMillis) { ok, err ->
                    if (!ok) Toast.makeText(this, err ?: "Save failed", Toast.LENGTH_SHORT).show()
                    else finish()
                }
            } else {
                repo.updateExpense(expenseId, title, category, amount, selectedDateMillis) { ok, err ->
                    if (!ok) Toast.makeText(this, err ?: "Update failed", Toast.LENGTH_SHORT).show()
                    else finish()
                }
            }
        }

        // ✅ Cancel button
        b.cancelBtn.setOnClickListener { finish() }
    }
}
