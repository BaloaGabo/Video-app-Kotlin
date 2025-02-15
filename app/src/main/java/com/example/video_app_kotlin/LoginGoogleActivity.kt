package com.example.video_app_kotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.video_app_kotlin.databinding.ActivityLoginGoogleBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginGoogleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginGoogleBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog : ProgressDialog
    private lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        comprobarSesion()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLoginGoogle.setOnClickListener {
            iniciarGoogle()
        }
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finishAffinity()
        }
    }

    private fun iniciarGoogle() {
        val googleSingInIntent = mGoogleSignInClient.signInIntent
        googleSingInARL.launch(googleSingInIntent)
    }

    private val googleSingInARL = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()){resultado->

        if (resultado.resultCode == RESULT_OK){

            val data = resultado.data

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val cuenta = task.getResult(ApiException::class.java)

                //Iniciar sesión
                autenticarCuentaGoogle(cuenta.idToken)
            }catch (e:Exception){

                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun autenticarCuentaGoogle(idToken: String?) {
        val credencial =GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener {authResult->
                if(authResult.additionalUserInfo!!.isNewUser){
                    actualizarInfoUsuario()
                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun actualizarInfoUsuario() {
        progressDialog.setMessage("Guardando informacion")
        progressDialog.show()
        val uid = firebaseAuth.uid
        val nombre = firebaseAuth.currentUser?.displayName
        val email = firebaseAuth.currentUser?.email

        val datosUsuario = HashMap<String,Any>()
        datosUsuario["uid"]="${uid}"
        datosUsuario["nombre"]="${nombre}"
        datosUsuario["email"]="${email}"

        val references = FirebaseDatabase.getInstance().getReference("Usuarios")
            references.child(uid!!)
                .setValue(datosUsuario)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finishAffinity()

                }
                .addOnFailureListener { e->
                    Toast.makeText(
                        this,
                        "${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
    }
}