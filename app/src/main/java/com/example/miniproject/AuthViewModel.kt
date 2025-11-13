package com.example.miniproject

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.io.File
import java.io.FileOutputStream

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs = application.getSharedPreferences("profile_prefs", Application.MODE_PRIVATE)
    private val TAG = "AuthViewModel"

    // Firebase user state
    val currentUser = mutableStateOf(auth.currentUser)
    // Local profile image state, loaded from permanent local storage
    val localProfileImageUri = mutableStateOf<Uri?>(loadProfileImageUri())
    
    val errorMessage = mutableStateOf<String?>(null)
    val loading = mutableStateOf(false)

    init {
        auth.addAuthStateListener {
            currentUser.value = it.currentUser
            if(it.currentUser != null) {
                localProfileImageUri.value = loadProfileImageUri()
            }
        }
    }

    private fun saveProfileImageUri(uri: Uri?) {
        Log.d(TAG, "Saving URI to SharedPreferences: $uri")
        prefs.edit().putString("profile_image_uri_${auth.currentUser?.uid}", uri?.toString()).apply()
    }

    private fun loadProfileImageUri(): Uri? {
        val uid = auth.currentUser?.uid ?: return null
        val uriString = prefs.getString("profile_image_uri_$uid", null)
        Log.d(TAG, "Loaded URI from SharedPreferences: $uriString")
        return uriString?.let { Uri.parse(it) }
    }
    
    private fun copyImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val file = File(getApplication<Application>().filesDir, "profile_${auth.currentUser?.uid}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Log.d(TAG, "Copied image to internal storage at: ${Uri.fromFile(file)}")
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error copying image to internal storage", e)
            null
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
                localProfileImageUri.value = loadProfileImageUri()
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

        var permanentImageUri: Uri? = imageUri
        if (imageUri != null && imageUri.scheme == "content") {
            permanentImageUri = copyImageToInternalStorage(imageUri)
            if (permanentImageUri == null) {
                errorMessage.value = "Failed to save image locally."
                loading.value = false
                return
            }
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
            if (updateTask.isSuccessful) {
                Log.d(TAG, "Firebase display name updated successfully.")
                currentUser.value = auth.currentUser
                
                if (imageUri != null && imageUri.scheme == "content") {
                    saveProfileImageUri(permanentImageUri)
                    localProfileImageUri.value = permanentImageUri
                }

                loading.value = false
                onSuccess()
            } else {
                val error = "Failed to update profile: ${updateTask.exception?.message}"
                Log.e(TAG, error)
                errorMessage.value = error
                loading.value = false
            }
        }
    }

    fun signOut() {
        val uid = auth.currentUser?.uid
        val file = File(getApplication<Application>().filesDir, "profile_$uid.jpg")
        if (file.exists()) {
            file.delete()
        }
        prefs.edit().remove("profile_image_uri_$uid").apply()
        auth.signOut()
        localProfileImageUri.value = null
    }
}