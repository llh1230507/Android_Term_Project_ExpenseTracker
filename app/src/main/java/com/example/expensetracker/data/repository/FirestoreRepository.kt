package com.example.expensetracker.data.repository

import com.example.expensetracker.data.model.Expense
import com.example.expensetracker.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirestoreRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun uid(): String = auth.currentUser?.uid ?: error("User not logged in")

    private fun profileRef() = db.collection("users").document(uid()).collection("meta").document("profile")
    private fun expensesCol() = db.collection("users").document(uid()).collection("expenses")

    fun createOrUpdateProfile(name: String, email: String, done: (Boolean, String?) -> Unit) {
        profileRef().set(UserProfile(name, email))
            .addOnSuccessListener { done(true, null) }
            .addOnFailureListener { done(false, it.message) }
    }

    fun getProfile(done: (UserProfile?, String?) -> Unit) {
        profileRef().get()
            .addOnSuccessListener { snap -> done(snap.toObject(UserProfile::class.java), null) }
            .addOnFailureListener { done(null, it.message) }
    }

    fun listenExpenses(onUpdate: (List<Expense>, String?) -> Unit): ListenerRegistration {
        return expensesCol()
            .orderBy("date")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList(), err.message)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { doc ->
                    val e = doc.toObject(Expense::class.java) ?: Expense()
                    e.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(list, null)
            }
    }

    fun addExpense(title: String, category: String, amount: Double, done: (Boolean, String?) -> Unit) {
        val data = mapOf(
            "title" to title,
            "category" to category,
            "amount" to amount,
            "date" to System.currentTimeMillis()
        )
        expensesCol().add(data)
            .addOnSuccessListener { done(true, null) }
            .addOnFailureListener { done(false, it.message) }
    }

    fun updateExpense(expenseId: String, title: String, category: String, amount: Double, done: (Boolean, String?) -> Unit) {
        val data = mapOf(
            "title" to title,
            "category" to category,
            "amount" to amount
        )
        expensesCol().document(expenseId).update(data)
            .addOnSuccessListener { done(true, null) }
            .addOnFailureListener { done(false, it.message) }
    }

    fun deleteExpense(expenseId: String, done: (Boolean, String?) -> Unit) {
        expensesCol().document(expenseId).delete()
            .addOnSuccessListener { done(true, null) }
            .addOnFailureListener { done(false, it.message) }
    }

    fun getExpense(expenseId: String, done: (Expense?, String?) -> Unit) {
        expensesCol().document(expenseId).get()
            .addOnSuccessListener { doc ->
                val e = doc.toObject(Expense::class.java)?.copy(id = doc.id)
                done(e, null)
            }
            .addOnFailureListener { done(null, it.message) }
    }
}
