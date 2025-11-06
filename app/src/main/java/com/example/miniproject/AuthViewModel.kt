package com.example.miniproject

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthViewModel"

    val currentUser = mutableStateOf(auth.currentUser)
    val profileImage = mutableStateOf<String?>(null)
    val errorMessage = mutableStateOf<String?>(null)
    val loading = mutableStateOf(false)

    init {
        loadProfileImage()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream) // Compress to reduce size
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun loadProfileImage() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        profileImage.value = document.getString("photoBase64")
                    }
                }
        }
    }

    fun signup(username: String, email: String, password: String, onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser!!
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).addOnCompleteListener {
                    firestore.collection("users").document(user.uid).set(mapOf("username" to username))
                    currentUser.value = auth.currentUser
                    loading.value = false
                    onSuccess()
                }
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
                loadProfileImage() // Load profile image on login
                loading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                loading.value = false
            }
    }

    fun updateProfile(username: String, imageUri: Uri?, onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null

        val user = auth.currentUser
        if (user == null) {
            errorMessage.value = "User not logged in"
            loading.value = false
            return
        }

        val userDocRef = firestore.collection("users").document(user.uid)
        val updates = mutableMapOf<String, Any>("username" to username)

        if (imageUri != null && imageUri.scheme == "content") {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, imageUri)
                } else {
                    val source = ImageDecoder.createSource(getApplication<Application>().contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source)
                }
                val base64Image = bitmapToBase64(bitmap)
                updates["photoBase64"] = base64Image
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image to Base64", e)
                errorMessage.value = "Failed to process image."
                loading.value = false
                return
            }
        }

        userDocRef.update(updates)
            .addOnSuccessListener {
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(username).build()
                user.updateProfile(profileUpdates).addOnCompleteListener { 
                    currentUser.value = auth.currentUser
                    if (updates.containsKey("photoBase64")) {
                        profileImage.value = updates["photoBase64"] as String
                    }
                    loading.value = false
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                errorMessage.value = "Failed to update profile: ${e.message}"
                loading.value = false
            }
    }

    fun signOut() {
        auth.signOut()
        currentUser.value = null
        profileImage.value = null
    }
}
