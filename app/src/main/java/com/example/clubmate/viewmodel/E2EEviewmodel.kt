package com.example.clubmate.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class End2EndEncryptionViewmodel : ViewModel() {


    private val reference = FirebaseDatabase.getInstance().getReference("user")


    fun setData(child: String) {


        reference.child(child).child("30").setValue("50")
            .addOnSuccessListener {

            }.addOnFailureListener {

            }

    }

    fun getData(chatId: String) {

        reference.child(chatId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(String::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    fun deleteData(child: String) {


        reference.child(child).child("30").removeValue()

    }

}

