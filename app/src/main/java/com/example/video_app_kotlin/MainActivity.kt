package com.example.video_app_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.video_app_kotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var videoArrayList: ArrayList<ModeloVideo>
    private lateinit var adaptadorVideo: AdaptadorVideo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val toolbar : Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(binding.toolBar)

        firebaseAuth = FirebaseAuth.getInstance()

        comprobarSesion()

        cargarVideos()

        binding.FABAgregarVideo.setOnClickListener {
            startActivity(Intent(applicationContext,AgregarVideoActivity::class.java))

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.mi_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_salir ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, LoginGoogleActivity::class.java)
                startActivity(intent)
                finishAffinity()
                Toast.makeText(applicationContext, "Haz cerrado sesión", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cargarVideos() {
        videoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Videos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                videoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloVideo = ds.getValue(ModeloVideo::class.java)
                    videoArrayList.add(modeloVideo!!)
                }

                adaptadorVideo = AdaptadorVideo(this@MainActivity,videoArrayList)
                binding.rvVideos.adapter = adaptadorVideo


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun comprobarSesion(){
        if (firebaseAuth.currentUser == null){
            startActivity(Intent(applicationContext, LoginGoogleActivity::class.java))
            finishAffinity()

        }
    }
}