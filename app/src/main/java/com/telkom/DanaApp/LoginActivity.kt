package com.telkom.DanaApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.telkom.DanaApp.component.MainScreen
import com.telkom.DanaApp.ui.theme.MoneyAppTheme
import com.telkom.DanaApp.view.Login

import com.telkom.DanaApp.ui.theme.White

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 1. Set the content view to your XML layout
        setContentView(R.layout.activity_login) // Use your app's R
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client)) // Use your app's R and getString
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<ComposeView>(R.id.composeableLogin).setContent {
            MoneyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = White
                ) {
                    // `loginErrorState` is created here, within the Compose context
                    // provided by `composeView.setContent`. It's 'remembered' so it
                    // survives recompositions within this ComposeView's content.
                    val loginErrorState = remember { mutableStateOf<String?>(null) }

                    Login(
                        onGoToRegisterClicked = {
                            val intent = Intent(this, SignUpActivity::class.java)
                            startActivity(intent)
                            // this.finish() // Consider if you want to finish LoginActivity
                        },
                        onLoginUsingFirebaseClicked = {
                            signInWithGoogle()
                        },
                        onLoginEmailAndPassword = { email, password ->
                            // The Activity's method updates the MutableState,
                            // which Compose observes.
                            loginUsingEmailAndPassword(email, password, loginErrorState)
                        },
                        loginErrorState = loginErrorState // Pass the MutableState itself
                    )
                }
            }
        }
    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN) // Consider using ActivityResultLauncher
    }

    @Deprecated("onActivityResult is deprecated, use ActivityResultLauncher instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUsingEmailAndPassword(email: String, password: String, loginErrorState: MutableState<String?>) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        loginErrorState.value = null // Clear error on success
                        navigateToHome()
                    } else {
                        // Update the MutableState's value. Compose will see this change.
                        loginErrorState.value = task.exception?.message ?: "Authentication failed."
                    }
                }
        } else {
            loginErrorState.value = "Email or Password cannot be empty"
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java) // Assuming HomeActivity exists
        startActivity(intent)
        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }
}