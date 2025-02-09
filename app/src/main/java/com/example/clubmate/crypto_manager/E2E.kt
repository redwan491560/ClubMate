package com.example.clubmate.crypto_manager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


object CryptoManager {

    // --- Constants ---
    private const val RSA_KEY_SIZE = 2048
    private const val GCM_IV_LENGTH = 12      // 12 bytes is recommended for GCM IV
    private const val GCM_TAG_LENGTH = 128    // in bits

    /**
     * Called during user registration.
     *
     * Generates a new RSA key pair using the provided UID.
     * The private key is encrypted with the user's password and both keys are stored in secure
     * EncryptedSharedPreferences.
     *
     * @param context Android context (needed for accessing SharedPreferences)
     * @param userUID A unique identifier for the user.
     * @param password The user's password used to encrypt the private key.
     * @return A Pair containing the public key (Base64 encoded) and the encrypted private key (Base64 encoded).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getBothKeys(context: Context, userUID: String, password: String): Pair<String, String> {
        val prefs = getSecurePrefs(context)

        // Check if keys already exist for the user
        val existingPublicKey = prefs.getString("public_key_$userUID", null)
        val existingEncryptedPrivateKey = prefs.getString("private_key_$userUID", null)

        if (existingPublicKey != null && existingEncryptedPrivateKey != null) {
            Log.d("CryptoManager", "Keys already exist for UID: $userUID")
            return Pair(existingPublicKey, existingEncryptedPrivateKey)
        }

        // 1. Generate a new RSA key pair (using the default provider)
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        val keyPair = keyPairGenerator.generateKeyPair()

        // 2. Convert public key to Base64 string
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)

        // 3. Encrypt the private key with the user's password.
        // Use a binary-safe charset (ISO_8859_1) to convert the bytes to string without data loss.
        val privateKeyBytes = keyPair.private.encoded
        val privateKeyString = String(privateKeyBytes, Charsets.ISO_8859_1)
        val encryptedPrivateKey = encryptAESKey(privateKeyString, password)
            ?: run {
                Log.e("CryptoManager", "Failed to encrypt private key")
                return Pair(publicKeyBase64, "")
            }

        // 4. Store both keys in EncryptedSharedPreferences
        prefs.edit().apply {
            putString("public_key_$userUID", publicKeyBase64)
            putString("private_key_$userUID", encryptedPrivateKey)
            apply()
        }
        Log.d("CryptoManager", "Generated and stored new keys for UID: $userUID")

        // 5. Return both keys
        return Pair(publicKeyBase64, encryptedPrivateKey)
    }

    /**
     * Retrieves and decrypts the stored private key.
     *
     * @param context Android context.
     * @param userUID The user identifier.
     * @param password The user's password to decrypt the private key.
     * @return The RSA PrivateKey, or null if decryption or reconstruction fails.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDecryptedPrivateKey(context: Context, userUID: String, password: String): PrivateKey? {
        val prefs = getSecurePrefs(context)
        val encryptedPrivateKey = prefs.getString("private_key_$userUID", null) ?: return null
        val decryptedKeyString = decryptAESKey(encryptedPrivateKey, password) ?: return null

        // Convert the decrypted string back to bytes using the same charset used for encryption.
        val keyBytes = decryptedKeyString.toByteArray(Charsets.ISO_8859_1)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return try {
            KeyFactory.getInstance("RSA").generatePrivate(keySpec)
        } catch (e: Exception) {
            Log.e("CryptoManager", "Failed to reconstruct private key", e)
            null
        }
    }

    /**
     * Encrypts the provided plaintext (private key string) using AES/GCM with the user's password.
     *
     * @param plainText The string to encrypt.
     * @param password The password used to derive the AES key.
     * @return A Base64-encoded string containing the IV and the ciphertext, or null if encryption fails.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptAESKey(plainText: String, password: String): String? {
        return try {
            // For demonstration, the AES key is directly derived from the password's UTF-8 bytes.
            // In production, use a proper key derivation function (like PBKDF2) to generate a strong key.
            val keySpec = SecretKeySpec(password.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // Generate a random IV.
            val iv = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            // Prepend IV to ciphertext and encode to Base64.
            Base64.getEncoder().encodeToString(iv + encryptedBytes)
        } catch (e: Exception) {
            Log.e("CryptoManager", "AES encryption failed", e)
            null
        }
    }

    /**
     * Decrypts the provided Base64-encoded string (which contains the IV and ciphertext)
     * using AES/GCM with the user's password.
     *
     * @param encryptedBase64 The Base64 encoded string containing IV and ciphertext.
     * @param password The password to derive the AES key.
     * @return The decrypted plaintext string, or null if decryption fails.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptAESKey(encryptedBase64: String, password: String): String? {
        return try {
            val decodedBytes = Base64.getDecoder().decode(encryptedBase64)
            val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
            val encryptedBytes = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)
            val keySpec = SecretKeySpec(password.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoManager", "AES decryption failed", e)
            null
        }
    }

    /**
     * Helper function to retrieve an instance of EncryptedSharedPreferences.
     */
    private fun getSecurePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "SecurePrefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}