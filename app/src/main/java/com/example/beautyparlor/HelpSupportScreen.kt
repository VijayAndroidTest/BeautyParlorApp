package com.example.beautyparlor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val callNumber = "7092842454"
    val emailAddress = "nagesuresh7092@gmail.com"
    val subject = "Support Request for Golden Trendz App"

    val callSupport: () -> Unit = {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$callNumber")
        }
        context.startActivity(intent)
    }

    val emailSupport: () -> Unit = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this.
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Help and Support") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Contact Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name
            InfoRow(label = "Name", value = "Golden Trendz")
            Spacer(modifier = Modifier.height(8.dp))

            // Email
            InfoRow(label = "Email", value = emailAddress)
            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number
            InfoRow(label = "Phone", value = "+91 $callNumber")
            Spacer(modifier = Modifier.height(8.dp))

            // Location
            val locationText = "5/124,Sundram Plazza,B.S Sundram Street,Avinasi-641654."
            val linkText = "https://maps.app.goo.gl/Z4SBBpuursiCFxc89?g_st=aw"

            InfoRowWithLink(
                label = "Location",
                text = locationText,
                link = linkText,
                onLinkClick = { uriHandler.openUri(linkText) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Contact Support",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { callSupport() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Call Support")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { emailSupport() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Email Support")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun InfoRowWithLink(
    label: String,
    text: String,
    link: String,
    onLinkClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(text)
        append(" ") // Add a space between the address and the link
        pushStringAnnotation(tag = "URL", annotation = link)
        withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
            append("Golden TrendZ Map Location") // Change the link text to something more user-friendly
        }
        pop()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
            .clickable { onLinkClick() }, // Make the entire row clickable to open the link
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = annotatedString,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.6f)
        )
    }
}