package com.example.beautyparlor

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    // Prefill Google account email if available
    val account = GoogleSignIn.getLastSignedInAccount(context)
    val defaultEmail = account?.email ?: ""

    var email by remember { mutableStateOf(defaultEmail) }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = Firebase.firestore
    val functions = Firebase.functions("asia-south1")

    // Phone number hint launcher
    val phoneHintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val phoneNumber = Identity.getSignInClient(context)
                    .getPhoneNumberFromIntent(result.data)
                if (!phoneNumber.isNullOrEmpty()) {
                    mobileNumber = phoneNumber.takeLast(10) // prefill last 10 digits
                }
            } catch (e: Exception) {
                Log.e("SignupScreen", "Failed to get phone number: ${e.message}")
            }
        }
    }

    // Launch phone hint once on first composition
    LaunchedEffect(Unit) {
        try {
            val request = GetPhoneNumberHintIntentRequest.builder().build()
            val client = Identity.getSignInClient(context)
            val intent = client.getPhoneNumberHintIntent(request).await()
            phoneHintLauncher.launch(IntentSenderRequest.Builder(intent.intentSender).build())
        } catch (e: Exception) {
            Log.e("SignupScreen", "Phone Hint API not available: ${e.message}")
        }
    }

    // Signup function
    val signup = {
        when {
            name.isBlank() -> errorMessage = "Please enter your name"
            email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                errorMessage = "Please enter a valid email address"
            mobileNumber.isBlank() || mobileNumber.length != 10 ->
                errorMessage = "Please enter a valid 10-digit mobile number"
            password.isBlank() || password.length < 6 ->
                errorMessage = "Password must be at least 6 characters long"
            else -> {
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        val result = auth.createUserWithEmailAndPassword(
                            email.trim(),
                            password.trim()
                        ).await()
                        val userId = result.user?.uid ?: throw Exception("User creation failed.")

                        val userMap = hashMapOf(
                            "name" to name.trim(),
                            "mobileNumber" to mobileNumber.trim(),
                            "email" to email.trim(),
                            "points" to 100,
                            "creationDate" to Timestamp.now()
                        )
                        firestore.collection("users").document(userId).set(userMap).await()

                        // Handle referral code if any
                        if (referralCode.isNotBlank() && referralCode.length == 10) {
                            val data = hashMapOf(
                                "newUserId" to userId,
                                "newUserMobile" to mobileNumber.trim(),
                                "referralCode" to referralCode.trim()
                            )
                            functions.getHttpsCallable("processReferral").call(data).await()
                        }

                        Toast.makeText(context, "Successfully signed up!", Toast.LENGTH_SHORT).show()
                        navController.navigate("mainScreen") {
                            popUpTo("signupScreen") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        errorMessage = when (e) {
                            is FirebaseAuthInvalidCredentialsException ->
                                "An account with this email already exists."
                            else -> "Signup failed: ${e.message}"
                        }
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF9C27B0)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_revert),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 20.dp) // Add padding to the bottom for spacing
        ) {
            item {
                Text(
                    "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field: prefilled & editable
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mobile number: editable + optional click-to-pick
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        if (it.length <= 10) mobileNumber = it.filter(Char::isDigit)
                    },
                    label = { Text("Mobile Number *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Optional: launch phone hint for convenience
                            coroutineScope.launch {
                                try {
                                    val request = GetPhoneNumberHintIntentRequest.builder().build()
                                    val client = Identity.getSignInClient(context)
                                    val intent = client.getPhoneNumberHintIntent(request).await()
                                    phoneHintLauncher.launch(
                                        IntentSenderRequest.Builder(intent.intentSender).build()
                                    )
                                } catch (e: Exception) {
                                    Log.e("SignupScreen", "Phone Hint API not available: ${e.message}")
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (isPasswordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(painter = painterResource(id = icon), contentDescription = null)
                        }
                    }
                )

                // The Referral Code TextField is now inside the LazyColumn
//                Spacer(modifier = Modifier.height(16.dp))
//                OutlinedTextField(
//                    value = referralCode,
//                    onValueChange = { referralCode = it },
//                    label = { Text("Referral Code (Optional)") },
//                    singleLine = true,
//                    enabled = !isLoading,
//                    modifier = Modifier.fillMaxWidth()
//                )

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(8.dp))
                }
            }

            item {
                Button(
                    onClick = { signup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CREATING...", color = Color.White)
                    } else {
                        Text("SIGN UP", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}