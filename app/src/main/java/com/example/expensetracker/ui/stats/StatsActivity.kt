package com.example.expensetracker.ui.stats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.repository.FirestoreRepository
import com.example.expensetracker.databinding.ActivityStatsBinding
import com.example.expensetracker.ui.main.MainActivity
import com.example.expensetracker.ui.profile.ProfileActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var b: ActivityStatsBinding
    private val repo = FirestoreRepository()

    private lateinit var adapter: CategoryStatAdapter
    private var reg: com.google.firebase.firestore.ListenerRegistration? = null

    // offsets for browsing
    private var dayOffset = 0
    private var weekOffset = 0
    private var monthOffset = 0
    private var yearOffset = 0

    // current range
    private var currentStartMillis: Long = 0L
    private var currentEndMillis: Long = Long.MAX_VALUE

    // cache all expenses from firestore
    private var allExpenses: List<Expense> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(b.root)

        // RecyclerView
        adapter = CategoryStatAdapter(emptyList()) { stat ->
            startActivity(
                Intent(this, CategoryDetailsActivity::class.java)
                    .putExtra("category", stat.category)
                    .putExtra("start", currentStartMillis)
                    .putExtra("end", currentEndMillis)
            )
        }
        b.categoryRv.layoutManager = LinearLayoutManager(this)
        b.categoryRv.adapter = adapter

        // Bottom nav
        b.bottomNav.selectedItemId = R.id.nav_stats
        b.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_stats -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Default toggle = week
        b.rangeToggle.check(b.weekBtn.id)

        // Set initial range + title
        setRangeFromToggle()
        updateRangeTitle()

        // Toggle change
        b.rangeToggle.addOnButtonCheckedListener { _, _, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            // reset offsets when switching range mode
            dayOffset = 0
            weekOffset = 0
            monthOffset = 0
            yearOffset = 0

            setRangeFromToggle()
            updateRangeTitle()
            renderStats()
        }

        // Prev / Next
        b.prevBtn.setOnClickListener {
            when (b.rangeToggle.checkedButtonId) {
                b.dayBtn.id -> dayOffset -= 1
                b.weekBtn.id -> weekOffset -= 1
                b.monthBtn.id -> monthOffset -= 1
                b.yearBtn.id -> yearOffset -= 1
            }
            setRangeFromToggle()
            updateRangeTitle()
            renderStats()
        }

        b.nextBtn.setOnClickListener {
            when (b.rangeToggle.checkedButtonId) {
                b.dayBtn.id -> dayOffset += 1
                b.weekBtn.id -> weekOffset += 1
                b.monthBtn.id -> monthOffset += 1
                b.yearBtn.id -> yearOffset += 1
            }
            setRangeFromToggle()
            updateRangeTitle()
            renderStats()
        }
    }

    override fun onStart() {
        super.onStart()

        reg = repo.listenExpenses { list, err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                allExpenses = emptyList()
                b.totalTv.text = "฿0.00"
                adapter.submit(emptyList())
                return@listenExpenses
            }

            allExpenses = list
            renderStats()
        }
    }

    override fun onStop() {
        super.onStop()
        reg?.remove()
        reg = null
    }

    // -------- stats rendering --------
    private fun renderStats() {
        val filtered = allExpenses.filter { it.date in currentStartMillis until currentEndMillis }

        val total = filtered.sumOf { it.amount }
        b.totalTv.text = "฿%.2f".format(total)

        if (total <= 0.0) {
            adapter.submit(emptyList())
            return
        }

        val stats = filtered
            .groupBy { it.category.trim().ifEmpty { "Other" } }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .map { (cat, sum) ->
                val percent = ((sum / total) * 100).toInt()
                CategoryStat(cat, sum, percent)
            }

        adapter.submit(stats)
    }

    // -------- range logic --------
    private fun setRangeFromToggle() {
        val (start, end) = when (b.rangeToggle.checkedButtonId) {
            b.dayBtn.id -> dayRangeMillis()
            b.weekBtn.id -> weekRangeMillis()
            b.monthBtn.id -> monthRangeMillis()
            b.yearBtn.id -> yearRangeMillis()
            else -> weekRangeMillis()
        }
        currentStartMillis = start
        currentEndMillis = end
    }

    private fun updateRangeTitle() {
        // ✅ You must have a TextView with id: rangeTitleTv in activity_stats.xml
        val fmtDay = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val fmtMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val fmtYear = SimpleDateFormat("yyyy", Locale.getDefault())

        val text = when (b.rangeToggle.checkedButtonId) {
            b.dayBtn.id -> fmtDay.format(currentStartMillis)
            b.weekBtn.id -> "${fmtDay.format(currentStartMillis)} - ${fmtDay.format(currentEndMillis - 1)}"
            b.monthBtn.id -> fmtMonth.format(currentStartMillis)
            b.yearBtn.id -> fmtYear.format(currentStartMillis)
            else -> ""
        }

        b.rangeTitleTv.text = text
    }

    // -------- range helpers (WITH OFFSETS) --------
    private fun dayRangeMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        cal.add(Calendar.DAY_OF_MONTH, dayOffset)

        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun weekRangeMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // start Monday
        cal.firstDayOfWeek = Calendar.MONDAY
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diff = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -diff)

        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)

        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        val end = cal.timeInMillis
        return start to end
    }

    private fun monthRangeMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        cal.add(Calendar.MONTH, monthOffset)

        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun yearRangeMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        cal.add(Calendar.YEAR, yearOffset)

        val start = cal.timeInMillis
        cal.add(Calendar.YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }
}
