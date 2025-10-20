package com.example.miniproject

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    val currentUser = mutableStateOf(auth.currentUser)
    val errorMessage = mutableStateOf<String?>(null)
    val loading = mutableStateOf(false)


    fun signup(email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                currentUser.value = auth.currentUser
                loading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                loading.value = false
            }
    }


    fun login(email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                currentUser.value = auth.currentUser
                loading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                loading.value = false
            }
    }


    fun signOut() {
        auth.signOut()
        currentUser.value = null
    }
}