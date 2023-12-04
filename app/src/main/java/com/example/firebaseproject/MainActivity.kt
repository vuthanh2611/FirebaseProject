package com.example.firebaseproject

//import com.example.firebaseproject.com.example.firebaseproject.MyAdapter

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseproject.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage


class MainActivity : ComponentActivity() {

    lateinit var gridView: GridView
    lateinit var dataList: ArrayList<DataClass>
    lateinit var adapter: MyAdapter
    val databaseReference = FirebaseDatabase.getInstance().getReference("Images")


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.gridView)
        dataList = ArrayList()
        adapter = MyAdapter(this, dataList)
        gridView.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val dataClass = dataSnapshot.getValue(DataClass::class.java)
                    if (dataClass != null && dataClass.imageURL.isNotEmpty()
                        && dataClass.imageURL.isNotBlank()) {
                        dataList.add(dataClass)
                    }
                }
//                    dataList.add(dataClass!!)

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })



        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, UploadActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


}











