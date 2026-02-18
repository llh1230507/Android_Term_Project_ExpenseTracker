package com.example.expensetracker.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.RowCategoryStatBinding

data class CategoryStat(
    val category: String,
    val total: Double,
    val percent: Int
)

class CategoryStatAdapter(
    private var items: List<CategoryStat>,
    private val onClick: (CategoryStat) -> Unit
) : RecyclerView.Adapter<CategoryStatAdapter.VH>() {

    inner class VH(val b: RowCategoryStatBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = RowCategoryStatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.categoryTv.text = item.category
        holder.b.amountTv.text = "-฿%.2f".format(item.total)
        holder.b.bar.progress = item.percent.coerceIn(0, 100)

        holder.itemView.setOnClickListener { onClick(item) } // ✅ clickable
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<CategoryStat>) {
        items = newItems
        notifyDataSetChanged()
    }
}
