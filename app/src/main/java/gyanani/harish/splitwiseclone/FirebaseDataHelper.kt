package gyanani.harish.splitwiseclone

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import gyanani.harish.splitwiseclone.models.Member
import gyanani.harish.splitwiseclone.trxlist.TrxDetails
import gyanani.harish.splitwiseclone.trxlist.TrxPayee
import gyanani.harish.splitwiseclone.trxlist.TrxSplit
import java.util.Date

class FirebaseDataHelper {

    private val db = FirebaseFirestore.getInstance()

    fun addMember(userId: String, email: String, phone: String, name: String) {
        val member = hashMapOf(
            FirebaseConstants.Members.Fields.USER_ID to userId,
            FirebaseConstants.Members.Fields.EMAIL to email,
            FirebaseConstants.Members.Fields.PHONE to phone,
            FirebaseConstants.Members.Fields.NAME to name
        )

        db.collection(FirebaseConstants.Members.COLLECTION_NAME)
            .document(userId)
            .set(member)
    }

    fun addTransaction(id: String, amount: Double, datetime: Long, description: String) {
        val transaction = hashMapOf(
            FirebaseConstants.Transactions.Fields.ID to id,
            FirebaseConstants.Transactions.Fields.AMOUNT to amount,
            FirebaseConstants.Transactions.Fields.DATETIME to Date(datetime).toString(),
            FirebaseConstants.Transactions.Fields.DESCRIPTION to description
        )

        db.collection(FirebaseConstants.Transactions.COLLECTION_NAME)
            .document(id)
            .set(transaction)
    }

    fun addTransactionPayee(transactionId: String, memberId: String, payeeAmount: Double) {
        val payeeData = hashMapOf(
            FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID to transactionId,
            FirebaseConstants.TransactionPayees.Fields.MEMBER_ID to memberId,
            FirebaseConstants.TransactionPayees.Fields.PAYEE_AMOUNT to payeeAmount
        )
        db.collection(FirebaseConstants.TransactionPayees.COLLECTION_NAME).add(payeeData)
    }

    fun addTransactionSplit(transactionId: String, memberId: String, splitAmount: Double) {
        val splitData = hashMapOf(
            FirebaseConstants.TransactionSplits.Fields.TRANSACTION_ID to transactionId,
            FirebaseConstants.TransactionSplits.Fields.MEMBER_ID to memberId,
            FirebaseConstants.TransactionSplits.Fields.SPLIT_AMOUNT to splitAmount
        )
        db.collection(FirebaseConstants.TransactionSplits.COLLECTION_NAME).add(splitData)
    }

    // Fetch all members from Firestore and return a list of Member objects
    private fun fetchAllMembers(): Task<List<Member>> {
        val source = TaskCompletionSource<List<Member>>()
        db.collection(FirebaseConstants.Members.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { documents ->
                val members = documents.map { doc ->
                    Member(
                        userId = doc.id,  // Assuming the document ID is the user ID
                        email = doc.getString(FirebaseConstants.Members.Fields.EMAIL) ?: "",
                        name = doc.getString(FirebaseConstants.Members.Fields.NAME) ?: "",
                        phone = doc.getString(FirebaseConstants.Members.Fields.PHONE) ?: ""
                    )
                }
                source.setResult(members)
            }
            .addOnFailureListener { exception ->
                source.setException(exception)
            }
        return source.task
    }

    fun checkUserAndTransactionCounts(): Task<Boolean> {
        val db = FirebaseFirestore.getInstance()
        val userCountTask =
            db.collection(FirebaseConstants.Members.COLLECTION_NAME).get().continueWith { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    documents.size() >= 3 // Check if user count is 3
                } else {
                    false
                }
            }

        val transactionCountTask =
            db.collection(FirebaseConstants.Transactions.COLLECTION_NAME).get()
                .continueWith { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        documents.size() >= 3 // Check if transaction count is 3
                    } else {
                        false
                    }
                }

        return Tasks.whenAllSuccess<Boolean>(userCountTask, transactionCountTask)
            .continueWith { task ->
                task.result.all { it } // Return true only if both conditions are true
            }
    }

    fun fetchAllTransactions(): Task<List<TrxDetails>> {
        val source = TaskCompletionSource<List<TrxDetails>>()
        db.collection(FirebaseConstants.Transactions.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.map { doc ->
                    TrxDetails(
                        id = doc.id,
                        amount = doc.getDouble(FirebaseConstants.Transactions.Fields.AMOUNT) ?: 0.0,
                        datetime = doc.getString(FirebaseConstants.Transactions.Fields.DATETIME)
                            ?: "",
                        description = doc.getString(FirebaseConstants.Transactions.Fields.DESCRIPTION)
                            ?: ""
                    )
                }

                // Fetch additional details for each transaction
                val tasks = transactions.map { transaction ->
                    val payeesTask = fetchPayees(transaction.id)
                    val splitsTask = fetchSplits(transaction.id)
                    val members = fetchAllMembers()
                    Tasks.whenAllComplete(payeesTask, splitsTask, members)
                        .continueWithTask { task ->
                            if (task.isSuccessful) {
                                val memberMap = members.result.associateBy { it.userId }
                                val payees = (task.result[0].result as List<TrxPayee>).map {
                                    it.name = memberMap[it.memberId]?.name ?: ""
                                    it
                                }
                                transaction.payees = payees
                                val splits = (task.result[1].result as List<TrxSplit>).map {
                                    it.name = memberMap[it.memberId]?.name ?: ""
                                    it
                                }
                                transaction.splits = splits
                            }
                            Tasks.forResult(transaction)
                        }
                }

                Tasks.whenAllSuccess<TrxDetails>(tasks)
                    .addOnSuccessListener { updatedTransactions ->
                        source.setResult(updatedTransactions)
                    }
            }
            .addOnFailureListener { exception ->
                source.setException(exception)
            }



        return source.task
    }

    private fun fetchPayees(
        transactionId: String,
    ): Task<List<TrxPayee>> {
        return db.collection(FirebaseConstants.TransactionPayees.COLLECTION_NAME)
            .whereEqualTo(FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID, transactionId)
            .get()
            .continueWith { task ->
                task.result?.documents?.map { doc ->
                    TrxPayee(
                        transactionId = doc.getString(FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID)
                            ?: "",
                        memberId = doc.getString(FirebaseConstants.TransactionPayees.Fields.MEMBER_ID)
                            ?: "",
                        payeeAmount = doc.getDouble(FirebaseConstants.TransactionPayees.Fields.PAYEE_AMOUNT)
                            ?: 0.0
                    )
                } ?: listOf()
            }
    }

    private fun fetchSplits(
        transactionId: String,
    ): Task<List<TrxSplit>> {
        return db.collection(FirebaseConstants.TransactionSplits.COLLECTION_NAME)
            .whereEqualTo(FirebaseConstants.TransactionSplits.Fields.TRANSACTION_ID, transactionId)
            .get()
            .continueWith { task ->
                task.result?.documents?.map { doc ->
                    TrxSplit(
                        transactionId = doc.getString(FirebaseConstants.TransactionSplits.Fields.TRANSACTION_ID)
                            ?: "",
                        memberId = doc.getString(FirebaseConstants.TransactionSplits.Fields.MEMBER_ID)
                            ?: "",
                        splitAmount = doc.getDouble(FirebaseConstants.TransactionSplits.Fields.SPLIT_AMOUNT)
                            ?: 0.0
                    )
                } ?: listOf()
            }
    }

//    private fun fetchAllTransactions(): Task<List<TrxDetails>> {
//        val source = TaskCompletionSource<List<TrxDetails>>()
//
//        db.collection(FirebaseConstants.Transactions.COLLECTION_NAME)
//            .get()
//            .addOnSuccessListener { documents ->
//                val fetchTasks = mutableListOf<Task<*>>()
//                val transactions = documents.map { doc ->
//                    TrxDetails(
//                        id = doc.getString(FirebaseConstants.Transactions.Fields.ID) ?: "",
//                        amount = doc.getDouble(FirebaseConstants.Transactions.Fields.AMOUNT) ?: 0.0,
//                        datetime = doc.getString(FirebaseConstants.Transactions.Fields.DATETIME) ?: "",
//                        description = doc.getString(FirebaseConstants.Transactions.Fields.DESCRIPTION) ?: ""
//                    )
//                    val payeesTask = fetchPayees(doc.id)
//                    val splitsTask = fetchSplits(doc.id)
//                    fetchTasks.add(payeesTask)
//                    fetchTasks.add(splitsTask)
//                    transaction
//                }
//
//                Tasks.whenAll(fetchTasks).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        source.setResult(transactions)
//                    } else {
//                        source.setException(Exception("Failed to fetch related transaction details."))
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                source.setException(exception)
//            }
//
//        return source.task
//    }
//
//    private fun fetchPayees(transactionId: String): Task<List<TrxPayee>> {
//        return db.collection(FirebaseConstants.TransactionPayees.COLLECTION_NAME)
//            .whereEqualTo(FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID, transactionId)
//            .get()
//            .continueWith { task ->
//                task.result?.documents?.map { doc ->
//                    TrxPayee(
//                        transactionId = doc.getString(FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID) ?: "",
//                        memberId = doc.getString(FirebaseConstants.TransactionPayees.Fields.MEMBER_ID) ?: "",
//                        payeeAmount = doc.getDouble(FirebaseConstants.TransactionPayees.Fields.PAYEE_AMOUNT) ?: 0.0
//                    )
//                } ?: listOf()
//            }
//    }
//
//    private fun fetchSplits(transactionId: String): Task<List<TrxSplit>> {
//        return db.collection(FirebaseConstants.TransactionSplits.COLLECTION_NAME)
//            .whereEqualTo(FirebaseConstants.TransactionSplits.Fields.TRANSACTION_ID, transactionId)
//            .get()
//            .continueWith { task ->
//                task.result?.documents?.map { doc ->
//                    TrxSplit(
//                        transactionId = doc.getString(FirebaseConstants.TransactionSplits.Fields.TRANSACTION_ID) ?: "",
//                        memberId = doc.getString(FirebaseConstants.TransactionSplits.Fields.MEMBER_ID) ?: "",
//                        splitAmount = doc.getDouble(FirebaseConstants.TransactionSplits.Fields.SPLIT_AMOUNT) ?: 0.0
//                    )
//                } ?: listOf()
//            }
//    }

//    private fun fetchAllTransactions(): Task<List<TrxDetails>> {
//        val source = TaskCompletionSource<List<TrxDetails>>()
//
//        db.collection("Transactions")
//            .get()
//            .addOnSuccessListener { documents ->
//                val fetchTasks = mutableListOf<Task<*>>()
//                val transactions = documents.map { doc ->
//                    val transaction = TrxDetails(
//                        id = doc.id,
//                        amount = doc.getDouble("amount") ?: 0.0,
//                        datetime = doc.getString("datetime") ?: "",
//                        description = doc.getString("description") ?: ""
//                    )
//                    val payeesTask = fetchPayeesAndSplits(transaction)
//                    val splitsTask = fetchSplits(transaction.id)
//                    fetchTasks.add(payeesTask)
//                    fetchTasks.add(splitsTask)
//                    transaction
//                }
//
//                Tasks.whenAll(fetchTasks).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        source.setResult(transactions)
//                    } else {
//                        source.setException(Exception("Failed to fetch related transaction details."))
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                source.setException(exception)
//            }
//
//        return source.task
//    }
//
//    private fun fetchTransactions(): Task<List<TrxDetails>> {
//        val source = TaskCompletionSource<List<TrxDetails>>()
//        db.collection(FirebaseConstants.Transactions.COLLECTION_NAME)
//            .get()
//            .addOnSuccessListener { documents ->
//                val transactions = mutableListOf<Task<TrxDetails>>()
//                val transactionCount = documents.size()
//                var fetchedCount = 0
//
//                documents.forEach { doc ->
//                    val transaction = TrxDetails(
//                        id = doc.id,
//                        amount = doc.getDouble( FirebaseConstants.Transactions.Fields.AMOUNT) ?: 0.0,
//                        datetime = doc.getString(FirebaseConstants.Transactions.Fields.DATETIME) ?: "",
//                        description = doc.getString(FirebaseConstants.Transactions.Fields.DESCRIPTION) ?: ""
//                    )
//                    transactions.add(transaction)
//
//                    // Fetch payees and splits for each transaction
//                    fetchPayeesAndSplits(transaction) {
//                        // Ensure all transactions are fetched and updated before setting the adapter
//                        fetchedCount++
//                        if (fetchedCount == transactionCount) {
//                            Tasks.whenAll(transactions).addOnCompleteListener {
//                                if (it.isSuccessful) {
//                                    source.setResult(transactions)
//                                } else {
//                                    source.setException(Exception("Failed to fetch related transaction details."))
//                                }
//                            }
//                            adapter = TrxAdapter(transactions)
//                            recyclerView.adapter = adapter
//                        }
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("TransactionsActivity", "Error getting documents: ", exception)
//            }
//    }
//
//    private fun fetchPayeesAndSplits(transaction: TrxDetails, onComplete: () -> Unit) {
//        // Simultaneous fetching of payees and splits
//        val payeesTask =
//            db.collection("TransactionPayees").whereEqualTo("transactionId", transaction.id).get()
//        val splitsTask =
//            db.collection("TransactionSplits").whereEqualTo("transactionId", transaction.id).get()
//
//        Tasks.whenAllComplete(payeesTask, splitsTask).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val payees = payeesTask.result?.documents?.map { doc ->
//                    TrxPayee(
//                        transactionId = doc.getString(FirebaseConstants.TransactionPayees.Fields.TRANSACTION_ID)
//                            ?: "",
//                        memberId = doc.getString("memberId") ?: "",
//                        payeeAmount = doc.getDouble("payeeAmount") ?: 0.0
//                    )
//                } ?: listOf()
//
//                val splits = splitsTask.result?.documents?.map { doc ->
//                    TrxSplit(
//                        transactionId = doc.getString("transactionId") ?: "",
//                        memberId = doc.getString("memberId") ?: "",
//                        splitAmount = doc.getDouble("splitAmount") ?: 0.0
//                    )
//                } ?: listOf()
//
//                // This is a simplification. You might need to add these lists to your Transaction object,
//                // depending on how you want to display them or use them further.
//            }
//            onComplete()
//        }
//    }


}
