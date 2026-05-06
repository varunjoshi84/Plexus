package com.example.plexus.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

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
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    _authState.value = AuthState.Verified
                }
                .addOnFailureListener { e ->
                    val message = when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Wrong OTP. Please try again."
                        else -> e.message ?: "Sign in failed"
                    }
                    _authState.value = AuthState.Error(message)
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