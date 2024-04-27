package gyanani.harish.splitwiseclone

object DummyDataManager {

    fun populateDummyData(repo: BaseRepo) {
        // Add members
        repo.addMember("1", "alice@example.com", "1234567890", "Alice")
        repo.addMember("2", "bob@example.com", "0987654321", "Bob")
        repo.addMember("3", "charlie@example.com", "1122334455", "Charlie")

        // Add transactions
        addCabFaceExpense(repo)
        addBreakfastExpense(repo)
        addTrainTicketExpense(repo)
    }

    private fun addBreakfastExpense(repo: BaseRepo) {
        repo.addTransaction("2", 50.0, System.currentTimeMillis(), "Breakfast")
        repo.addTransactionPayee("2", "1", 25.0) // Alice paid $25
        repo.addTransactionPayee( "2", "2", 25.0) // Bob paid $25
        repo.addTransactionSplit( "2", "1", 25.0) // Alice is responsible for $50
        repo.addTransactionSplit( "2", "2", 25.0) // Alice is responsible for $50

    }

    private fun addCabFaceExpense(repo: BaseRepo) {
        repo.addTransaction("1", 100.0, System.currentTimeMillis(), "Cab fare")
        repo.addTransactionPayee("1", "1", 50.0) // Alice paid $50
        repo.addTransactionPayee("1", "2", 50.0) // Bob paid $50
        repo.addTransactionSplit("1", "1", 100.0) // Alice is responsible for $100

    }

    private fun addTrainTicketExpense(repo: BaseRepo) {
        repo.addTransaction("3", 75.0, System.currentTimeMillis(), "Train tickets")
        // Members: Alice (1), Bob (2), Charlie (3)

        // Other transactions remain unchanged, focusing only on train tickets here
        // Transaction 3: Train tickets - paid by Alice and Charlie, split among all three
        repo.addTransactionPayee("3", "1", 37.5) // Alice paid $37.50
        repo.addTransactionPayee("3", "3", 37.5) // Charlie paid $37.50

        // Splitting the transaction evenly among all three friends
        val members = listOf("1", "2", "3") // IDs of Alice, Bob, and Charlie
        val totalCost = 75.0 // Total cost of the train tickets
        val splitAmount = totalCost / members.size // Each person's share

        members.forEach { memberId ->
            repo.addTransactionSplit("3", memberId, splitAmount) // $25 each
        }
    }
}