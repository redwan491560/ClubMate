package com.example.clubmate

import android.app.Application
import com.google.firebase.FirebaseApp


class ClubMateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

}