package gyanani.harish.splitwiseclone.trxlist

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gyanani.harish.splitwiseclone.FirebaseDataHelper
import gyanani.harish.splitwiseclone.R

class TransactionsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        FirebaseDataHelper().fetchAllTransactions().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                adapter = TrxAdapter(task.result)
                recyclerView.adapter = adapter
            } else {
                Log.e("TransactionsActivity", "Failed to fetch transactions: ", task.exception)
            }
        }
    }
}