Table Member {
id int [pk]
email varchar
phone varchar
name varchar
}

Table Transaction {
id int [pk]
amount decimal
datetime datetime
}

Table TransactionPayee {
transaction_id int [ref: > Transaction.id]
member_id int [ref: > Member.id]
payee_amount decimal
Indexes {
(transaction_id, member_id) [pk]
}
}

Table TransactionSplit {
transaction_id int [ref: > Transaction.id]
member_id int [ref: > Member.id]
split_amount decimal
Indexes {
(transaction_id, member_id) [pk]
}
}