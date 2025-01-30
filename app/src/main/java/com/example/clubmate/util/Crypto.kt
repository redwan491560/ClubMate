package com.example.clubmate.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.util.Base64

object CryptoUtils {

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // Key size
        return keyPairGenerator.generateKeyPair()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun publicKeyToString(publicKey: PublicKey): String {
        val encodedKey = publicKey.encoded
        return Base64.getEncoder().encodeToString(encodedKey)
    }
}
