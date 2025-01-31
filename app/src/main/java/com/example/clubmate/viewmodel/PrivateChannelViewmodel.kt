package com.example.clubmate.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

class PrivateChannelViewmodel : ViewModel() {


    private val _channelRef = FirebaseDatabase.getInstance().getReference("private_channels")


    fun createChannel(channelId: String) {

        _channelRef.child(channelId)


    }

    fun incognito(state: Boolean) {

    }

    fun joinChannel(channelId: String) {

    }

    // messaging

}