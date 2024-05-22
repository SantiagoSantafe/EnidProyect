package com.santiago.enidproyect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.santiago.enidproyect.ui.chat.User

class LogIn : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var btnIniciarGoogle: SignInButton
    private lateinit var auth: FirebaseAuth
    lateinit var clientWebID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        clientWebID = "322403123957-8t6g2r7qrcbrfn61rk8a5de5d8tp3u26.apps.googleusercontent.com"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        auth = FirebaseAuth.getInstance()

        btnIniciarGoogle = findViewById(R.id.sign_in_button)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientWebID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        btnIniciarGoogle.setOnClickListener {
            signInGmail()
        }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleActivity"
    }
    private fun signInGmail() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val mDbRef = FirebaseDatabase.getInstance().getReference()
                    val email = task.result?.user?.email ?: ""
                    val name = task.result?.user?.displayName ?: ""
                    mDbRef.child("user").child(auth.currentUser?.uid!!).setValue(User(name, email, auth.currentUser?.uid!!))

                    Menu(task.result?.user?.email ?: "")


                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
    private fun Menu(email:String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)

        }
        startActivity(homeIntent)
    }
}