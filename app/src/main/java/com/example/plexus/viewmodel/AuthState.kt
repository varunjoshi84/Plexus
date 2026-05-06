package com.example.plexus.viewmodel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState()
    object Verified : AuthState()
    data class Error(val message: String) : AuthState()
}