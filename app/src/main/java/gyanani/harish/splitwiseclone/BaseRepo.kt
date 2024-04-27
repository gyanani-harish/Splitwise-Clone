package gyanani.harish.splitwiseclone

interface BaseRepo {
    fun addMember(userId: String, email: String, phone: String, name: String)
    fun addTransaction(id: String, amount: Double, datetime: Long, description: String)
    fun addTransactionPayee(
        transactionId: String,
        memberId: String,
        payeeAmount: Double
    )

    fun addTransactionSplit(
        transactionId: String,
        memberId: String,
        splitAmount: Double
    )
}