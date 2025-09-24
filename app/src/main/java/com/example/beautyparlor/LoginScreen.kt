    package com.example.beautyparlor

    import android.content.Context
    import android.content.Intent
    import android.net.Uri
    import android.util.Log
    import android.widget.Toast
    import androidx.compose.foundation.layout.*
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
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
    import com.google.firebase.firestore.FirebaseFirestore
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import com.google.firebase.auth.FirebaseAuth.AuthStateListener
    import com.google.firebase.Timestamp

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen(navController: NavController) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // ✅ Prefill email if user is already signed in
        var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        // This listener handles subsequent authentication changes (e.g., successful login).
        DisposableEffect(Unit) {
            val listener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    navController.navigate("mainScreen") {
                        popUpTo("loginScreen") { inclusive = true }
                    }
                }
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }

        /**
         * Updates the user's last login date in Firestore.
         */
        suspend fun updateLastLogin(userId: String) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("lastLoginDate", Timestamp.now())
                    .await()
            } catch (e: Exception) {
                Log.e("LoginScreen", "Failed to update last login: ${e.message}")
            }
        }

        val signIn = {
            when {
                email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    errorMessage = "Please enter a valid email address"
                }
                password.isBlank() -> errorMessage = "Please enter your password"
                else -> {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val result = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
                            result.user?.let { user ->
                                updateLastLogin(user.uid)
                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                // ✅ navigate only after login success
                                navController.navigate("mainScreen") {
                                    popUpTo("loginScreen") { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = when (e) {
                                is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                                else -> "Login failed: ${e.message}"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Login", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF9C27B0))
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Welcome Back!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                Text(
                    "Sign in to access your bookings",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    trailingIcon = {
                        val icon = if (isPasswordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                        val description = if (isPasswordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = description
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = { signIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIGNING IN...", color = Color.White)
                        }
                    } else {
                        Text("SIGN IN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate("signupScreen") },
                    enabled = !isLoading
                ) {
                    Text("Don't have an account? Sign Up", color = Color(0xFF6A1B9A))
                }
            }
        }
    }