package com.example.clubmate.viewmodel

// Add these imports
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.example.clubmate.crypto_manager.CryptoManager
import com.example.clubmate.db.Routes
import com.example.clubmate.db.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class AuthViewModel : ViewModel() {

    // auth
    private val _auth = FirebaseAuth.getInstance()

    // reference
    private var userRef = FirebaseDatabase.getInstance().getReference("user")


    private var _currentUser = MutableStateFlow(_auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser


    private val _userData = MutableStateFlow<Routes.UserModel?>(null)
    val userData: MutableStateFlow<Routes.UserModel?> = _userData


    private val _authState = MutableLiveData<Status>()
    val authState: LiveData<Status> = _authState


    private val passwordRegex =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()


    init {
        checkAuthenticationStatus()
    }

    fun checkAuthenticationStatus() {
        val currentUser = _currentUser.value
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                _authState.value = Status.Authenticated
                fetchUserData(currentUser.uid) { userData ->
                    _userData.value = userData
                }
            } else {
                _authState.value = Status.Error("Please verify your email before logging in")
            }
        } else {
            _authState.value = Status.NotAuthenticated
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun logIn(email: String, password: String) {

        if (email.isBlank() || password.isBlank()) {
            _authState.value = Status.Error("Email and password cannot be empty")
            return
        }

        _authState.value = Status.Loading
        _auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val curUser = _auth.currentUser
                    if (curUser != null) {
                        if (curUser.isEmailVerified) {

                            userRef.child(curUser.uid).get()
                                .addOnSuccessListener { snap ->
                                    val encryptedPrivateKey =
                                        snap.child("encryptedPrivateKey").value as? String

                                    if (!encryptedPrivateKey.isNullOrBlank()) {
                                        // Decrypt and store private key in Keystore
                                        Log.d(
                                            "CryptoManager",
                                            "Private key successfully restored in Keystore"
                                        )
                                        _authState.value = Status.Authenticated
                                        _currentUser.value = _auth.currentUser
                                    } else {
                                        Log.e("CryptoManager", "No encrypted private key found")
                                    }
                                }.addOnFailureListener { exception ->
                                    _authState.value =
                                        Status.Error("Failed to retrieve private key: ${exception.message}")
                                }

                            _authState.value = Status.Authenticated
                            _currentUser.value = _auth.currentUser
                        } else {
                            _authState.value =
                                Status.Error("Please verify your email before logging in")
                        }
                    } else {
                        _authState.value = Status.Error("No user is logged in")
                    }
                } else {
                    _authState.value = Status.Error(task.exception?.message ?: "Login failed")
                }
            }.addOnFailureListener { exception ->
                _authState.value = Status.Error(exception.message ?: "An error occurred")
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun register2Realtime(
        email: String,
        phone: String,
        userName: String,
        uid: String,
        context: Context,
        password: String, // Pass password for encryption
        onSuccess: () -> Unit
    ) {
        // Generate RSA Key Pair
        val keyPair =
            CryptoManager.getBothKeys(userUID = uid, password = password, context = context)

        val userData = Routes.UserModel(
            username = userName,
            phone = phone, email = email,
            uid = uid,
            encryptedPrivateKey = keyPair.second, // Store encrypted private key
            publicKey = keyPair.first // Store public key
        )
        // Prepare user data with encrypted private key and public key


        // Save to Firebase
        userRef.child(uid).setValue(userData)
            .addOnSuccessListener {
                onSuccess()
                Log.d("Auth Success", "User data saved to database with encryption")
            }
            .addOnFailureListener { exception ->
                _authState.value = Status.Error(exception.message ?: "Failed to save user data")
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun register(
        email: String, password: String,
        userName: String,
        context: Context,
        phone: String, onClick: (Boolean) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || phone.isBlank()) {
            _authState.value = Status.Error("Email / password or phone cannot be empty")
            onClick(false)
            return
        }

        if (!email.matches(emailRegex)) {
            _authState.value = Status.Error("Invalid email format")
            onClick(false)
            return
        }

        if (!password.matches(passwordRegex)) {
            _authState.value = Status.Error("Password does not meet requirements")
            onClick(false)
            return
        }

        _auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            register2Realtime(
                                context = context, email = email,
                                phone = phone, userName = userName,
                                uid = user.uid, password = password
                            ) {
                                _currentUser.value = user
                                onClick(true)
                            }
                        } else {
                            onClick(false)
                            _authState.value = Status.Error("Failed to send verification email")
                        }
                    }
                } else {
                    onClick(false)
                    _authState.value =
                        Status.Error(task.exception?.message ?: "Registration failed")
                }
            }.addOnFailureListener { exception ->
                onClick(false)
                _authState.value = Status.Error(exception.message ?: "An error occurred")
            }
    }

    fun uploadImage(uid: String, uri: Uri, onComplete: (Boolean) -> Unit) {
        uploadProfilePictureToStorage(uri, uid) { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                saveProfilePictureToDatabase(uid, imageUrl, onComplete)
            } else {
                onComplete(false) // Upload failed
            }
        }
    }

    private fun uploadProfilePictureToStorage(imageUri: Uri, uid: String, onComplete: (String) -> Unit) {
        MediaManager.get().upload(imageUri)
            .option("folder", "profile_pics/$uid")
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Uploading: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        onComplete(imageUrl) // Return Cloudinary image URL
                    } else {
                        onComplete("") // Failed to retrieve URL
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    onComplete("")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload rescheduled: ${error?.description}")
                    onComplete("")
                }
            }).dispatch()
    }

    private fun saveProfilePictureToDatabase(uid: String, imageUrl: String, onComplete: (Boolean) -> Unit) {

        userRef.child(uid).child("photoUrl").setValue(imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }


    fun signOut() {
        _auth.signOut()
        _userData.value = null
        _currentUser.value = null
        _authState.value = Status.NotAuthenticated
    }


    private fun fetchUserData(uid: String, onResult: (Routes.UserModel?) -> Unit) {

        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(Routes.UserModel::class.java)
                userData?.let {
                    onResult(userData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }
}
