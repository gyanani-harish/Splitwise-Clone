package gyanani.harish.splitwiseclone

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepo : BaseRepo {
    private val helper = FirebaseDataHelper()
    override fun addMember(userId: String, email: String, phone: String, name: String) {
        helper.addMember(userId, email, phone, name)
    }

    override fun addTransaction(id: String, amount: Double, datetime: Long, description: String) {
        helper.addTransaction(id, amount, datetime, description)
    }

    override fun addTransactionPayee(
        transactionId: String,
        memberId: String,
        payeeAmount: Double
    ) {
        helper.addTransactionPayee(transactionId, memberId, payeeAmount)
    }

    override fun addTransactionSplit(
        transactionId: String,
        memberId: String,
        splitAmount: Double
    ) {
        helper.addTransactionSplit(transactionId, memberId, splitAmount)
    }
}


