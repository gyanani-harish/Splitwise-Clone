package gyanani.harish.splitwiseclone

object FirebaseConstants {
    object Members {
        const val COLLECTION_NAME = "Members"
        object Fields {
            const val USER_ID = "userId"
            const val EMAIL = "email"
            const val PHONE = "phone"
            const val NAME = "name"
        }
    }

    object Transactions {
        const val COLLECTION_NAME = "Transactions"
        object Fields {
            const val ID = "id"
            const val AMOUNT = "amount"
            const val DATETIME = "datetime"
            const val DESCRIPTION = "description"
        }
    }

    object TransactionPayees {
        const val COLLECTION_NAME = "TransactionPayees"
        object Fields {
            const val TRANSACTION_ID = "transactionId"
            const val MEMBER_ID = "memberId"
            const val PAYEE_AMOUNT = "payeeAmount"
        }
    }

    object TransactionSplits {
        const val COLLECTION_NAME = "TransactionSplits"
        object Fields {
            const val TRANSACTION_ID = "transactionId"
            const val MEMBER_ID = "memberId"
            const val SPLIT_AMOUNT = "splitAmount"
        }
    }
}
