package gyanani.harish.splitwiseclone.trxlist

data class TrxPayee(
    val transactionId: String,
    val memberId: String = "",
    val payeeAmount: Double = 0.0
) {
    lateinit var name: String
}
