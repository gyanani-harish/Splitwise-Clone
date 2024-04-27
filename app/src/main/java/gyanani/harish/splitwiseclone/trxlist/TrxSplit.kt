package gyanani.harish.splitwiseclone.trxlist

data class TrxSplit(
    val transactionId: String,
    val memberId: String = "",
    val splitAmount: Double = 0.0
) {
    lateinit var name: String
}