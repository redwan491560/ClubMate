package com.example.clubmate.crypto_manager

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@RequiresApi(Build.VERSION_CODES.O)
object CryptoHelper {
    // Generate RSA Key Pair
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    // Encrypt with public key
    fun encrypt(message: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // Decrypt with private key
    fun decrypt(encryptedMessage: String, privateKey: PrivateKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.getDecoder().decode(encryptedMessage)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    // Convert public key to string
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    // Convert string to public key
    fun stringToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(publicKeyString)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }
}


class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    // Generate or retrieve the RSA key pair
    private fun getOrCreateKeyPair(): KeyPair {
        val alias = "e2ee_keypair"
        if (!keyStore.containsAlias(alias)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore"
            )
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setKeySize(2048)
                    .build()
            )
            return keyPairGenerator.generateKeyPair()
        }

        val privateKey = keyStore.getKey(alias, null) as PrivateKey
        val publicKey = keyStore.getCertificate(alias).publicKey
        return KeyPair(publicKey, privateKey)
    }

    // Retrieve the public key to share with others
    fun getPublicKey(): ByteArray {
        return getOrCreateKeyPair().public.encoded
    }

    // Encrypt a message using the recipient's public key
    fun encryptMessageWithPublicKey(message: String, recipientPublicKey: ByteArray): ByteArray {
        val publicKey =
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(recipientPublicKey))
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }
        return cipher.doFinal(message.toByteArray())
    }

    // Decrypt a message using the private key
    fun decryptMessageWithPrivateKey(encryptedMessage: ByteArray): String {
        val privateKey = getOrCreateKeyPair().private
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }
        return String(cipher.doFinal(encryptedMessage))
    }

    // Generate a random AES key
    private fun generateSessionKey(): SecretKey {
        return KeyGenerator.getInstance("AES").apply {
            init(256) // AES-256
        }.generateKey()
    }

    // Encrypt a message using the session key
    fun encryptMessageWithSessionKey(message: String, outputStream: OutputStream): ByteArray {
        val sessionKey = generateSessionKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, sessionKey)
        }

        val iv = cipher.iv
        val encryptedMessage = cipher.doFinal(message.toByteArray())

        DataOutputStream(outputStream).use {
            it.writeInt(iv.size)
            it.write(iv)
            it.writeInt(encryptedMessage.size)
            it.write(encryptedMessage)
        }

        return sessionKey.encoded
    }

    // Decrypt a message using the session key
    fun decryptMessageWithSessionKey(sessionKeyBytes: ByteArray, inputStream: InputStream): String {
        val sessionKey = SecretKeySpec(sessionKeyBytes, "AES")

        return DataInputStream(inputStream).use {
            val ivSize = it.readInt()
            val iv = ByteArray(ivSize)
            it.readFully(iv)

            val encryptedMessageSize = it.readInt()
            val encryptedMessage = ByteArray(encryptedMessageSize)
            it.readFully(encryptedMessage)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
                init(Cipher.DECRYPT_MODE, sessionKey, IvParameterSpec(iv))
            }
            String(cipher.doFinal(encryptedMessage))
        }
    }

    // Encrypt the session key using the recipient's public key
    fun encryptSessionKey(sessionKey: ByteArray, recipientPublicKey: ByteArray): ByteArray {
        val publicKey =
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(recipientPublicKey))
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }
        return cipher.doFinal(sessionKey)
    }

    // Decrypt the session key using the private key
    fun decryptSessionKey(encryptedSessionKey: ByteArray): ByteArray {
        val privateKey = getOrCreateKeyPair().private
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.DECRYPT_MODE, privateKey)
        }
        return cipher.doFinal(encryptedSessionKey)
    }
}
