package com.example.expensetracker.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.RowCategoryTotalBinding
import java.util.Locale

data class CategoryTotal(val category: String, val total: Double)

class CategoryTotalAdapter(
    private var items: List<CategoryTotal> = emptyList()
) : RecyclerView.Adapter<CategoryTotalAdapter.VH>() {

    inner class VH(val b: RowCategoryTotalBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = RowCategoryTotalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.categoryTv.text = item.category
        holder.b.totalTv.text = String.format(Locale.getDefault(), "฿%.2f", item.total)
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<CategoryTotal>) {
        items = newItems
        notifyDataSetChanged()
    }
}
