package gyanani.harish.splitwiseclone.trxlist

data class TrxDetails(
    val id: String = "",
    val amount: Double = 0.0,
    val datetime: String = "",
    val description: String = "",
    var payees: List<TrxPayee> = emptyList(),
    var splits: List<TrxSplit> = emptyList()
)
