package gyanani.harish.splitwiseclone
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneEditText = findViewById(R.id.mobileNoEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (phone.isNotEmpty() && password.isNotEmpty()) {
                authenticateUser(phone, password)
            } else {
                Toast.makeText(this, "Please enter both phone number and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateUser(phone: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("member")
            .whereEqualTo("phone", phone)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        // Assuming 'password' is stored in plain text for demonstration purposes
                        if (document.getString("password") == password) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            handleDummyData()
                            break
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking credentials: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleDummyData() {
        FirebaseDataHelper().checkUserAndTransactionCounts().addOnSuccessListener { result ->
            if (result) {
                // Logic if the condition is met
                Log.d("CheckCounts", "Both counts are equal to 3.")
            } else {
                DummyDataManager.populateDummyData(FirebaseRepo())
                // Logic if the condition is not met
                Log.d("CheckCounts", "Counts do not match the required criteria.")
            }
        }.addOnFailureListener { exception ->
            // Handle error
            Log.d("CheckCounts", "Error checking counts: ${exception.message}")
        }

    }
}
