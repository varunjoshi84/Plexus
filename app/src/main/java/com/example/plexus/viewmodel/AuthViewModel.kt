package com.example.plexus.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plexus.data.model.UserModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private var verificationId: String? = null

    // Check if user already logged in
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // Get current Firebase user
    val currentUser get() = auth.currentUser

    // ─── Send OTP ─────────────────────────────────────
    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                _authState.value = AuthState.CodeSent
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ─── Verify OTP ───────────────────────────────────
    fun verifyOtp(code: String) {
        val vId = verificationId
        if (vId == null) {
            _authState.value = AuthState.Error("Session expired. Please resend OTP.")
            return
        }
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(vId, code)
        signInWithCredential(credential)
    }

    // ─── Sign In ──────────────────────────────────────
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                checkProfileCompletion()
            } catch (e: Exception) {
                val message = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Wrong OTP. Please try again."
                    else -> e.message ?: "Sign in failed"
                }
                _authState.value = AuthState.Error(message)
            }
        }
    }

    private fun checkProfileCompletion() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists() && doc.contains("username")) {
                    _authState.value = AuthState.Verified
                } else {
                    _authState.value = AuthState.NeedsProfile
                }
            } catch (e: Exception) {
                _authState.value = AuthState.NeedsProfile
            }
        }
    }

    fun saveUserProfile(username: String, displayName: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Check if username is taken
                val existing = db.collection("users")
                    .whereEqualTo("username", username)
                    .get().await()
                
                if (!existing.isEmpty && existing.documents.any { it.id != uid }) {
                    _authState.value = AuthState.Error("Username already taken")
                    return@launch
                }

                val user = UserModel(
                    uid = uid,
                    username = username,
                    displayName = displayName,
                    phone = phone,
                    bio = bio,
                    isOnline = true
                )
                
                db.collection("users").document(uid).set(user).await()
                _authState.value = AuthState.Verified
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    fun updateUserProfile(username: String, displayName: String) {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Check if username is taken by someone else
                val existing = db.collection("users")
                    .whereEqualTo("username", username)
                    .get().await()
                
                if (!existing.isEmpty && existing.documents.any { it.id != uid }) {
                    _authState.value = AuthState.Error("Username already taken")
                    return@launch
                }

                db.collection("users").document(uid).update(
                    mapOf(
                        "username" to username,
                        "displayName" to displayName
                    )
                ).await()
                
                _authState.value = AuthState.Verified
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    // ─── Logout ───────────────────────────────────────
    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        } finally {
            _authState.value = AuthState.Idle
        }
    }

    // ─── Reset state ──────────────────────────────────
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}