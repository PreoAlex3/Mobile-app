package com.example.petshop.ui.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petshop.R
import com.example.petshop.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun AccountInfo() {
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory(context))
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }

        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.handleImageSelection(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(
                    name = uiState.customer?.name ?: "Name",
                    profileImagePath = if (uiState.isEditing) uiState.profileImagePathInput else uiState.customer?.profileImagePath,
                    onProfilePictureClick = {
                        if (uiState.isEditing) {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    isEditing = uiState.isEditing
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isEditing) {
                    EditProfileForm(
                        nameInput = uiState.nameInput,
                        emailInput = uiState.emailInput,
                        phoneInput = uiState.phoneInput,
                        addressInput = uiState.addressInput,
                        onNameChange = { viewModel.updateNameInput(it) },
                        onEmailChange = { viewModel.updateEmailInput(it) },
                        onPhoneChange = { viewModel.updatePhoneInput(it) },
                        onAddressChange = { viewModel.updateAddressInput(it) },
                        onSave = {
                            coroutineScope.launch {
                                viewModel.saveProfile()
                            }
                        },
                        onCancel = {
                            viewModel.setEditMode(false)
                        }
                    )
                } else {
                    // Display account details
                    AccountDetailSection(
                        icon = Icons.Default.Person,
                        title = "Personal Information",
                        details = listOf(
                            "Name" to (uiState.customer?.name ?: "Name"),
                            "Email" to (uiState.customer?.email ?: "name@gmail.com"),
                            "Phone" to (uiState.customer?.phone ?: "072643245"),
                            "Address" to (uiState.customer?.address ?: "Address not provided")
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccountActionButton(
                            icon = Icons.Default.Edit,
                            text = "Edit Profile",
                            onClick = { viewModel.setEditMode(true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    profileImagePath: String?,
    onProfilePictureClick: () -> Unit,
    isEditing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (profileImagePath != null && profileImagePath.isNotEmpty()) {
                    AsyncImage(
                        model = profileImagePath,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .then(
                                if (isEditing) Modifier.clickable { onProfilePictureClick() } else Modifier
                            ),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.profile_placeholder),
                        placeholder = painterResource(R.drawable.profile_placeholder)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.profile_placeholder),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .then(
                                if (isEditing) Modifier.clickable { onProfilePictureClick() } else Modifier
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                if (isEditing) {
                    IconButton(
                        onClick = { onProfilePictureClick() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EditProfileForm(
    nameInput: String,
    emailInput: String,
    phoneInput: String,
    addressInput: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = onPhoneChange,
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = addressInput,
                onValueChange = onAddressChange,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSave
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun AccountDetailSection(
    icon: ImageVector,
    title: String,
    details: List<Pair<String, String>>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AccountActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun AccountInfoPreview() {
    Surface {
        val mockCustomer = MockCustomer(
            name = "Andrei",
            email = "andrei@yahoo.com",
            phone = "072334352",
            address = "Street",
            profileImagePath = ""
        )
        PreviewAccountInfo(mockCustomer)
    }
}

data class MockCustomer(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val profileImagePath: String?
)

@Composable
private fun PreviewAccountInfo(mockCustomer: MockCustomer) {
    val isEditing = false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Header with mock data
        ProfileHeader(
            name = mockCustomer.name,
            profileImagePath = mockCustomer.profileImagePath,
            onProfilePictureClick = { },
            isEditing = isEditing
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display account details with mock data
        AccountDetailSection(
            icon = Icons.Default.Person,
            title = "Personal Information",
            details = listOf(
                "Name" to mockCustomer.name,
                "Email" to mockCustomer.email,
                "Phone" to mockCustomer.phone,
                "Address" to mockCustomer.address
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AccountActionButton(
                icon = Icons.Default.Edit,
                text = "Edit Profile",
                onClick = { }
            )
        }
    }
}