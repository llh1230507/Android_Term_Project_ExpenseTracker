package com.example.expensetracker.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.databinding.RowExpenseBinding
import java.util.Locale

class ExpenseAdapter(
    private var items: List<Expense>,
    private val onOpen: (Expense) -> Unit,   // ✅ NEW
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.VH>() {

    inner class VH(val b: RowExpenseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = RowExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]

        holder.b.titleTv.text = e.title
        holder.b.categoryTv.text = e.category

        val amountText = String.format(Locale.getDefault(), "฿%.2f", e.amount)
        holder.b.amountTv.text = "-$amountText"

        // ✅ Open details when tapping the row
        holder.b.root.setOnClickListener { onOpen(e) }

        // ✅ Delete
        holder.b.deleteBtn.setOnClickListener { onDelete(e) }

        // ✅ Edit (only if your row has edit button)
        // If you DON’T have editBtn in row_expense.xml, remove this block.

    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<Expense>) {
        items = newItems
        notifyDataSetChanged()
    }
}