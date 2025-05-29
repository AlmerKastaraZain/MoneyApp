package com.telkom.DanaApp

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.telkom.DanaApp.ui.theme.MoneyAppTheme
import com.telkom.DanaApp.view.login.Register

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<ComposeView>(R.id.composeableRegister).setContent {
            MoneyAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = colorResource(id = R.color.white),
                ) {
                    Register(
                        onGoToLoginClicked = {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent) //  Note: removed Intent.FLAG_ACTIVITY_NEW_TASK
                            this.finish()
                        },

                        onRegister = { email, password, confirmPassword, firstName, lastName, ->
                            RegisterUsingEmailAndPassword(
                                email,
                                password,
                                firstName,
                                lastName,
                                confirmPassword
                            )
                        },
                    )
                }
            }
        }

    }


    private fun RegisterUsingEmailAndPassword(email:String, password:String, firstName:String, lastName:String,  confirmPassword:String) {
        val context: Context = this
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$firstName $lastName") // Combine first and last name
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                            }
                        }

                    // Save firstName and lastName to Firebase Firestore
                    val userId = user?.uid
                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()
                        val userDocument = hashMapOf(
                            "userId" to userId,
                            "firstName" to firstName,
                            "lastName" to lastName
                        )
                        db.collection("App/MoneyApp/UserDatas").document(userId).set(userDocument)
                    }
                    //TODO: Navigate to another page
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }
}