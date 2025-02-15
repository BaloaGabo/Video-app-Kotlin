package com.example.video_app_kotlin

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.example.video_app_kotlin.databinding.ActivityAgregarVideoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AgregarVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarVideoBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var uriVideo: Uri? = null
    private var titulo: String? = null

    private val resultGaleriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val data = resultado.data
                uriVideo = data?.data
                setVideo()
                binding.txtEstado.text = "Video seleccionado Correctamente"
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) {
                videoPickGaleria()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento no esta concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val solicitarPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { esConcedido ->
            if (esConcedido) {
                videoGrabarCamara()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de para accede a la camara no esta concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val resultCamaraARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                val data = resultado.data
                uriVideo = data?.data
                setVideo()
                binding.txtEstado.text =
                    "El video se ha grabado con exito y esta listo para subirse"
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setMessage("El video se esta subiendo")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSelectVideo.setOnClickListener {
            videoPickDialog()
        }

        binding.btnSubirVideo.setOnClickListener {
            titulo = binding.etTituloVideo.text.toString().trim()

            if (titulo.isNullOrEmpty()) {
                Toast.makeText(applicationContext, "Por favor ingrese un titulo", Toast.LENGTH_SHORT)
                    .show()
            } else if (uriVideo == null) {
                Toast.makeText(
                    applicationContext,
                    "Por favor seleccione o grabe un video",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                subirVideo()
            }
        }

    }

    private fun subirVideo() {
        progressDialog.show()

        val autor = firebaseAuth.currentUser!!.displayName

        val tiempo = "" + System.currentTimeMillis()
        val rutaNombre = "Video/video_$tiempo"
        val storageRef = FirebaseStorage.getInstance().getReference(rutaNombre)
        storageRef.putFile(uriVideo!!)
            .addOnSuccessListener { task ->
                val uriTask = task.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = "$tiempo"
                    hashMap["titulo"] = "$titulo"
                    hashMap["tiempo"] = "$tiempo"
                    hashMap["videoUri"] = "$downloadUri"
                    hashMap["autor"] = "$autor"

                    val reference = FirebaseDatabase.getInstance().getReference("Videos")
                    reference.child(tiempo)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(applicationContext, "Se subio el video", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this@AgregarVideoActivity, MainActivity::class.java))
                            finish()

                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT)
                                .show()

                        }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun videoPickDialog() {
        val popupMenu = PopupMenu(this, binding.btnSelectVideo)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Galeria")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Camara")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                checkPermissionAndOpenGallery()
            } else if (itemId == 2) {
                checkPermissionAndOpenCamera()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                videoPickGaleria()
            } else {
                solicitarPermisoAlmacenamiento.launch(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                videoPickGaleria()
            } else {
                solicitarPermisoAlmacenamiento.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun videoPickGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val packageManager: PackageManager = packageManager
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        if (resolveInfo != null) {
            resultGaleriaARL.launch(intent)
        } else {
            Toast.makeText(this, "No se encontró ninguna aplicación para seleccionar un video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            videoGrabarCamara()
        } else {
            solicitarPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    private fun videoGrabarCamara() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        resultCamaraARL.launch(intent)
    }

    private fun setVideo() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setVideoURI(uriVideo)
        binding.videoView.requestFocus()
        binding.videoView.setOnPreparedListener {
            binding.videoView.pause()
        }
    }
}