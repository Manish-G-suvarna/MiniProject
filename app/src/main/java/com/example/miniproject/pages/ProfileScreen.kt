package com.example.miniproject.pages

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.miniproject.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val user by authViewModel.currentUser
    val profileImageBase64 by authViewModel.profileImage
    var showEditDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    if (showEditDialog) {
        EditProfileDialog(
            authViewModel = authViewModel,
            onDismiss = { showEditDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                userName = user?.displayName ?: "User",
                userEmail = user?.email ?: "user@example.com",
                onEditProfileClick = { showEditDialog = true },
                onImageClick = { showEditDialog = true },
                profileImageBase64 = profileImageBase64
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileOptionItem(icon = Icons.Default.FavoriteBorder, text = "Favourites")
            ProfileOptionItem(icon = Icons.Default.Download, text = "Downloads")

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileOptionItem(icon = Icons.Default.Language, text = "Languages")
            ProfileOptionItem(icon = Icons.Default.LocationOn, text = "Location")

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileOptionItem(icon = Icons.Default.DeleteOutline, text = "Clear Cache")
            ProfileOptionItem(icon = Icons.Default.History, text = "Clear History")

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileOptionItem(icon = Icons.AutoMirrored.Filled.ExitToApp, text = "Log Out", showArrow = false) {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo("homepage") { inclusive = true }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "App Version 2.2",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EditProfileDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    val user = authViewModel.currentUser.value
    var username by remember { mutableStateOf(user?.displayName ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by authViewModel.loading
    val errorMessage by authViewModel.errorMessage

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !isLoading) {
                    Text("Add/Change Profile Picture")
                }
                selectedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(100.dp).padding(top = 16.dp)
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                errorMessage?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    authViewModel.updateProfile(username, selectedImageUri, onDismiss)
                },
                enabled = !isLoading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    onEditProfileClick: () -> Unit,
    onImageClick: () -> Unit,
    profileImageBase64: String?
) {

    val imageBitmap = remember(profileImageBase64) {
        if (profileImageBase64 != null) {
            try {
                val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { onImageClick() },
                    tint = Color.White
                )
            }

            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Picture",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(4.dp),
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
        Text(text = userEmail, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEditProfileClick) {
            Text("Edit Profile")
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    text: String,
    showArrow: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}
