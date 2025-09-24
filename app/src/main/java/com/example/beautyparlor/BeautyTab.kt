package com.example.beautyparlor

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun BeautyTab(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var userPoints by remember { mutableStateOf(0L) }
    var myReferrals by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var referralCode by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(initialPage = 1)
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("My Referrals", "Refer a Friend")

    // Firestore real-time updates
    DisposableEffect(currentUser) {
        var listenerRegistration: ListenerRegistration? = null
        isLoading = true

        currentUser?.let { user ->
            val userRef = firestore.collection("users").document(user.uid)
            listenerRegistration = userRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("BeautyTab", "Listen failed.", e)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    userPoints = snapshot.getLong("points") ?: 0L
                    referralCode = snapshot.getString("mobileNumber") ?: ""

                    firestore.collection("users").document(user.uid)
                        .collection("referredUsers").get()
                        .addOnSuccessListener { referralsSnapshot ->
                            myReferrals = referralsSnapshot.documents.map { it.data ?: emptyMap() }
                            isLoading = false
                        }
                        .addOnFailureListener {
                            Log.e("BeautyTab", "Error fetching referrals", it)
                            isLoading = false
                        }
                } else {
                    isLoading = false
                }
            }
        } ?: run {
            isLoading = false
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Beauty Points", fontWeight = FontWeight.Bold, color = Color.Black)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("mainScreen") {
                                popUpTo("loginScreen") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Points Display
            Text(
                text = userPoints.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Text(text = "Points", fontSize = 16.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFF9C27B0)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                title,
                                color = if (pagerState.currentPage == index) Color(0xFF9C27B0) else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Pager
            HorizontalPager(
                count = tabTitles.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MyReferralsTabContent(myReferrals)
                    1 -> ReferAFriendTabContent(referralCode, isLoading)
                }
            }
        }
    }
}

@Composable
fun MyReferralsTabContent(referrals: List<Map<String, Any>>) {
    if (referrals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Start inviting friends to see your referrals here!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {

            Text(
                text = "Top Referrals",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                referrals.take(5).forEach { referral ->
                    ReferralCard(referral)
                }
            }

            Text(
                text = "Recent Referrals",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp, top = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                referrals.reversed().take(5).forEach { referral ->
                    ReferralCard(referral)
                }
            }
        }
    }
}

@Composable
fun ReferralCard(referral: Map<String, Any>) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "User: ${referral["referredUserMobile"]}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Date: ${(referral["referralDate"] as? Timestamp)?.toDate()}",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ReferAFriendTabContent(referralCode: String, isLoading: Boolean) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun shareReferralLink(code: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val referralMessage =
                "Join me on Beauty Parlor App! Enter my code $code to earn beauty points. Android: https://i.diawi.com/HGDJCG"
            putExtra(Intent.EXTRA_TEXT, referralMessage)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share your referral code"))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Invite a friend and earn 200 points!",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.refereafriend),
            contentDescription = "Refer a friend",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your Referral Code",
            color = Color.Black,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF9C27B0)
            )
        } else {
            Text(
                text = referralCode,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { shareReferralLink(referralCode) },
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05940B)),
            enabled = !isLoading
        ) {
            Text("Share Link", color = Color.White)
        }
    }
}