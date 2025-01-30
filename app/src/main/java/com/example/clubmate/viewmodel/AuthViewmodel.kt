package com.example.clubmate.viewmodel

// Add these imports
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clubmate.db.Status
import com.example.clubmate.db.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


open class AuthViewModel : ViewModel() {

    // auth
    private val _auth = FirebaseAuth.getInstance()

    // reference
    private var userRef = FirebaseDatabase.getInstance().getReference("user")


    private var _currentUser = MutableStateFlow(_auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser


    private val _userData = MutableStateFlow<UserModel?>(null)
    val userData: MutableStateFlow<UserModel?> = _userData


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


    private fun register2Realtime(
        email: String,
        phone: String,
        userName: String,
        uid: String,
        onSuccess: () -> Unit
    ) {

        val userData = UserModel(
            username = userName,
            phone = phone,
            email = email,
            uid = uid,
            publicKey = "publicKeyString"
        )


        userRef.child(uid).setValue(userData)
            .addOnSuccessListener {
                onSuccess()
                Log.d("Auth Success", "User data saved to database")
            }
            .addOnFailureListener { exception ->
                _authState.value = Status.Error(exception.message ?: "Failed to save user data")
            }
    }


    fun register(
        email: String, password: String,
        userName: String,
        phone: String, onClick: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || phone.isBlank()) {
            _authState.value = Status.Error("Email / password or phone cannot be empty")
            return
        }
        if (!email.matches(emailRegex)) {
            _authState.value = Status.Error("Invalid email format")
            return
        }
        if (!password.matches(passwordRegex)) {
            _authState.value = Status.Error("Password does not meet requirements")
            return
        }
        _auth.signOut()
        _auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = _auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            register2Realtime(email, phone, userName, user.uid) {
                                _currentUser.value = user
                                onClick()
                            }
                        } else {
                            _authState.value = Status.Error("Failed to send verification email")
                        }
                    }
                } else {
                    _authState.value =
                        Status.Error(task.exception?.message ?: "Registration failed")
                }
            }.addOnFailureListener { exception ->
                _authState.value = Status.Error(exception.message ?: "An error occurred")
            }
    }


    open fun signOut() {
        _auth.signOut()
        _userData.value = null
        _currentUser.value = null
        _authState.value = Status.NotAuthenticated
    }


    private fun fetchUserData(uid: String, onResult: (UserModel?) -> Unit) {

        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserModel::class.java)
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
