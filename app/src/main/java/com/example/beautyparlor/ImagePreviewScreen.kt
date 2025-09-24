package com.example.beautyparlor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var generatedImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var faceDetectionMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Makeup", "Bridal Packages", "Facials", "Hair Spa", "Nail Art", "Mehndi")
    val promptMap = mapOf(
        "Makeup" to listOf(
            "A person with a natural, everyday makeup look.",
            "A person with a bold, dramatic, and glamorous makeup look.",
            "A person with a soft, romantic, and ethereal makeup look.",
            "A person with a fresh, dewy, and glowing makeup look.",
            "A person with a retro-inspired vintage makeup look."
        ),
        "Bridal Packages" to listOf("A person with a full bridal makeover, including elegant makeup, a traditional hairstyle, and bridal accessories."),
        "Facials" to listOf("A person with glowing, radiant, and smooth skin after a professional facial treatment."),
        "Hair Spa" to listOf("A person with lustrous, healthy, and professionally styled hair, as if after a hair spa treatment."),
        "Nail Art" to listOf("A person's hands with detailed, colorful, and artistic nail art. The image should focus on the hands."),
        "Mehndi" to listOf("A person's hands with intricate and beautiful traditional Mehndi (henna) designs.")
    )

    // Use a temporary URI to save the photo
    val tempPhotoUri: Uri = remember {
        val file = File(context.cacheDir, "temp_photo.jpg")
        FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        coroutineScope.launch {
            if (success) {
                if (detectFaceInBitmap(context, tempPhotoUri)) {
                    imageUri = tempPhotoUri
                    generatedImages = emptyList()
                    selectedCategory = null
                    faceDetectionMessage = null
                } else {
                    faceDetectionMessage = "No face detected. Please take a photo with a face in it."
                    imageUri = null
                    generatedImages = emptyList()
                    selectedCategory = null
                    Toast.makeText(context, faceDetectionMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Photo capture failed or was cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempPhotoUri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Makeover") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = {
                    imageUri = null
                    generatedImages = emptyList()
                    selectedCategory = null
                    faceDetectionMessage = null
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            cameraLauncher.launch(tempPhotoUri)
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Take a new photo")
            }

            Spacer(Modifier.height(16.dp))

            faceDetectionMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            if (imageUri != null) {
                val bitmap = remember { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(imageUri) {
                    withContext(Dispatchers.IO) {
                        try {
                            val contentResolver = context.contentResolver
                            val inputStream = contentResolver.openInputStream(imageUri!!)
                            bitmap.value = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()
                        } catch (e: Exception) {
                            Log.e("ImagePreview", "Error loading image from URI", e)
                        }
                    }
                }

                bitmap.value?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .size(LocalConfiguration.current.screenWidthDp.dp - 32.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Original Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No photo taken yet.")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Category Selection Buttons
            Text(
                text = "Select a Beauty Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    Button(
                        onClick = {
                            selectedCategory = category
                            generatedImages = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == category) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (selectedCategory == category) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (imageUri != null && selectedCategory != null) {
                        isGenerating = true
                        generatedImages = emptyList()
                        coroutineScope.launch {
                            try {
                                val prompts = promptMap[selectedCategory] ?: listOf("A professional beauty parlor makeover.")
                                val bitmaps = generateImageWithAI(context, imageUri!!, prompts)
                                withContext(Dispatchers.Main) {
                                    generatedImages = bitmaps
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to generate AI image: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("API Call", "Error during API call", e)
                                }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    isGenerating = false
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please take a photo and select a service first.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = imageUri != null && selectedCategory != null && !isGenerating
            ) {
                Text(if (selectedCategory != null) "Generate AI ${selectedCategory} Look" else "Generate AI Look")
            }

            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            if (generatedImages.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Your AI Makeover",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    generatedImages.forEach { generatedBitmap ->
                        Image(
                            bitmap = generatedBitmap.asImageBitmap(),
                            contentDescription = "Generated Image",
                            modifier = Modifier
                                .size(LocalConfiguration.current.screenWidthDp.dp - 32.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

suspend fun detectFaceInBitmap(context: Context, imageUri: Uri): Boolean {
    return withContext(Dispatchers.IO) {
        // TODO: In a real app, you would use a machine learning library
        // like ML Kit to detect a face in the bitmap.
        // For this example, we'll assume a face is always detected.
        // The image is a .
        // The image shows a woman with clear skin, a natural makeup look, and her hair pulled back.
        // The lighting is soft and even, highlighting her features.
        // The background is a simple, blurred studio setting.
        return@withContext true
    }
}


suspend fun generateImageWithAI(
    context: Context,
    inputUri: Uri,
    prompts: List<String>
): List<Bitmap> {
    // Replace "YOUR_API_KEY_HERE" with your actual API key from Google AI Studio.
    // This key is required for authentication to use the Gemini API.
    val apiKey = "AIzaSyA4iPTX6auC4yHZnkCH3ayqfqMdzETFbLY"
    val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image-preview:generateContent?key=$apiKey"

    val base64Image: String? = try {
        val inputStream = context.contentResolver.openInputStream(inputUri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("API Call", "Error converting image to Base64", e)
        throw e
    }

    if (base64Image == null) {
        throw Exception("Failed to convert image to Base64")
    }

    val bitmaps = mutableListOf<Bitmap>()
    // Wrap the network call in withContext(Dispatchers.IO) to prevent NetworkOnMainThreadException
    withContext(Dispatchers.IO) {
        // Loop through each prompt to generate multiple images
        for (prompt in prompts) {
            try {
                val payload = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "$prompt"
                        },
                        {
                          "inlineData": {
                            "mimeType": "image/jpeg",
                            "data": "$base64Image"
                          }
                        }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "responseModalities": ["IMAGE"]
                  }
                }
            """.trimIndent()

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                connection.outputStream.use { os ->
                    val input = payload.toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(responseString)
                    val candidates = jsonObject.getJSONArray("candidates")

                    if (candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.getJSONObject("content")
                        val parts = content.getJSONArray("parts")

                        val inlineData = parts.getJSONObject(0).getJSONObject("inlineData")
                        val base64Data = inlineData.getString("data")
                        val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        if (bitmap != null) {
                            bitmaps.add(bitmap)
                        } else {
                            Log.e("API Call", "Failed to decode bitmap for prompt: $prompt")
                        }
                    }
                } else {
                    val errorStream = connection.errorStream.bufferedReader().use { it.readText() }
                    Log.e("API Call", "Error response for prompt '$prompt': $responseCode - $errorStream")
                }
            } catch (e: Exception) {
                Log.e("API Call", "Exception during API call for prompt '$prompt'", e)
            }
        }
    }

    if (bitmaps.isEmpty()) {
        throw Exception("Failed to generate any images.")
    }

    return bitmaps
}
