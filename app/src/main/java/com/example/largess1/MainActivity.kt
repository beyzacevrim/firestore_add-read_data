package com.example.largess1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var getDataBtn: Button
    private lateinit var nameTV: TextView
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.add_name)
        etAddress = findViewById(R.id.add_address)
        etEmail = findViewById(R.id.add_email)
        etPassword = findViewById(R.id.add_password)
        nameTV = findViewById(R.id.name_TV)

        btnSave = findViewById(R.id.btnSave)
        getDataBtn  = findViewById(R.id.getData_btn)


        btnSave.setOnClickListener {
            saveDataToFirestore()
        }
        getDataBtn.setOnClickListener {
            readDataFromFirestore()
        }
    }
    private fun readDataFromFirestore() {
        val docRef = db.collection("users").document("beyza@gmail.com")
        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    Log.d("Read Data Activity", "DocumentSnapshot data: ${document.data}")
                    val name = document.getString("name")
                    if (name != null) {
                        nameTV.text = name
                    }
                } else {
                    Log.d("Read Data Activity", "No such document")
                }
            }
            .addOnFailureListener{exception ->
                Log.d("Read Data Activity", "get failed with ", exception)
            }
    }
    private fun saveDataToFirestore() {
        val name = etName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurub", Toast.LENGTH_SHORT).show()
            return
        }
        val userData = hashMapOf(
            "name" to name,
            "address" to address,
            "email" to email,
            "password" to password
        )
        // kullanıcının UID'sini alma
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password) //firebase kütüphanesinin fonksiyonu
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val newUser = FirebaseAuth.getInstance().currentUser
                        val userId = newUser?.uid
                        userId?.let {
                            db.collection("users").document(it)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Veri başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                                    etName.text.clear()
                                    etAddress.text.clear()
                                    etEmail.text.clear()
                                    etPassword.text.clear()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Veri ekleme hatası", e)
                                    Toast.makeText(this, "Veri ekleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Kullanıcı oluşturma hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else {
            val userId = user.uid
            users.document(email).set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Veri başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                    etName.text.clear()
                    etAddress.text.clear()
                    etEmail.text.clear()
                    etPassword.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Veri ekleme hatası", e)
                    Toast.makeText(this, "Veri ekleme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}