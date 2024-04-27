package gyanani.harish.splitwiseclone.trxlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gyanani.harish.splitwiseclone.R

class TrxAdapter(private val transactions: List<TrxDetails>) :
    RecyclerView.Adapter<TrxAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val datetimeTextView: TextView = itemView.findViewById(R.id.datetime)
        private val payeesTextView: TextView = itemView.findViewById(R.id.payees)
        private val splitsTextView: TextView = itemView.findViewById(R.id.splits)

        fun bind(transaction: TrxDetails) {
            amountTextView.text = String.format("$%.2f", transaction.amount)
            descriptionTextView.text = transaction.description
            datetimeTextView.text = transaction.datetime
            payeesTextView.text = "\nPayees: "+transaction.payees.joinToString { "\n"+it.name +" - "+ it.payeeAmount }
            splitsTextView.text = "\nSplits: "+transaction.splits.joinToString { "\n"+it.name +" - "+ it.splitAmount }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size
}
