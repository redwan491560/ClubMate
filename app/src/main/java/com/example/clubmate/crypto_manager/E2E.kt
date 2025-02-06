package com.example.clubmate.crypto_manager

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_KEY_SIZE = 256
    private const val RSA_KEY_SIZE = 2048
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128


    // create keys
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateAndStoreRSAKeyPair(userUID: String): String {
        val keyAlias = "RSA_Key_$userUID"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        // If key already exists, return public key
        if (keyStore.containsAlias(keyAlias)) {
            return getPublicKey(userUID)
        }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
        )

        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(RSA_KEY_SIZE)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .build()
        )
        keyPairGenerator.generateKeyPair()

        return getPublicKey(userUID)
    }

    // retrieves the public key
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPublicKey(userUID: String): String {
        val keyAlias = "RSA_Key_$userUID"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val publicKey = keyStore.getCertificate(keyAlias)?.publicKey ?: return ""
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    // retrieves the private key RSA
    private fun getPrivateKey(userUID: String): PrivateKey? {
        val keyAlias = "RSA_Key_$userUID"
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
        return entry?.privateKey
    }

    // encrypt aes key
    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptAESKey(aesKey: String, recipientPublicKeyBase64: String): String {
        val keyBytes = Base64.getDecoder().decode(recipientPublicKeyBase64)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedKey = cipher.doFinal(aesKey.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedKey)
    }

    // decrypt aes key
    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptAESKey(encryptedAESKeyBase64: String, userUID: String): String? {
        val privateKey = getPrivateKey(userUID) ?: return null
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedKeyBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedAESKeyBase64))
        return String(decryptedKeyBytes)
    }

    // generates aes key
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateAESKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        val secretKey = keyGen.generateKey()
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptMessage(message: String, aesKeyBase64: String): String {
        val keyBytes = Base64.getDecoder().decode(aesKeyBase64)
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encryptedBytes = cipher.doFinal(message.toByteArray())

        // Store IV + CipherText
        val combined = iv + encryptedBytes
        return Base64.getEncoder().encodeToString(combined)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptMessage(encryptedMessageBase64: String, aesKeyBase64: String): String {
        val keyBytes = Base64.getDecoder().decode(aesKeyBase64)
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val decodedBytes = Base64.getDecoder().decode(encryptedMessageBase64)

        // Extract IV
        val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    /** Store private key securely in Keystore **/
    private fun storePrivateKey(uid: String, privateKey: PrivateKey) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = KeyStore.PrivateKeyEntry(privateKey, null)
        val params = KeyStore.PasswordProtection(null)
        keyStore.setEntry("RSA_Key_$uid", entry, params)
    }

    /** Import an encrypted private key and store it securely **/
    @RequiresApi(Build.VERSION_CODES.O)
    fun importPrivateKey(uid: String, encryptedKeyBase64: String, passphrase: String) {
        val encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64)
        val decryptedKeyBytes =
            decryptAESKey(String(encryptedKey), passphrase)?.toByteArray() ?: return

        // Convert back to PrivateKey
        val keySpec = PKCS8EncodedKeySpec(decryptedKeyBytes)
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec)

        // Store in Android Keystore
        storePrivateKey(uid, privateKey)
    }

    /** Export a private key securely (AES encrypted) **/
    @RequiresApi(Build.VERSION_CODES.O)
    fun exportPrivateKey(uid: String, passphrase: String): String? {
        val privateKey = getPrivateKey(uid) ?: return null
        val privateKeyBytes = privateKey.encoded

        // Encrypt with AES before storing
        val encryptedKey = encryptAESKey(String(privateKeyBytes), passphrase)
        return Base64.getEncoder().encodeToString(encryptedKey.toByteArray())
    }
}