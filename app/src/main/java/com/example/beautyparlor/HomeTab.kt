package com.example.beautyparlor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.beautyparlor.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(mainNavController: NavController, isAdmin: Boolean) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("User Name") }
    var isLoading by remember { mutableStateOf(true) }

    val menuItems = listOf(
        "My Profile",
        "Home",
        "My Bookings",
        "My Beauty Points",
        "Help and Support",
        "Logout"
    )
    val carouselImages = listOf(
        R.drawable.goldenfirst,
        R.drawable.homeimgg,
        R.drawable.homeiimgg,
        R.drawable.female,
        R.drawable.long_hair
    )
    val pagerState = rememberPagerState { carouselImages.size }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            try {
                val documentSnapshot = firestore.collection("users").document(user.uid).get().await()
                val data = documentSnapshot.data
                userName = data?.get("name") as? String ?: user.email.orEmpty().substringBefore("@")
            } catch (e: Exception) {
                // Handle potential errors, e.g., user doc not found or network issues
                println("Error fetching user data: ${e.message}")
            }
        }
        isLoading = false
    }

    LaunchedEffect(pagerState) {
        while (isActive) {
            delay(2000)
            val nextPage = (pagerState.currentPage + 1) % carouselImages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp),
                drawerContainerColor = Color(0xF0041C9D)
                //  drawerContainerColor = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 16.dp, start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome,",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = userName,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.onPrimary, thickness = 1.dp)

                LazyColumn {
                    items(menuItems) { item ->
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item, color = MaterialTheme.colorScheme.onPrimary)
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Navigate to $item",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                when (item) {
                                    "My Profile" -> mainNavController.navigate("myProfile")
                                    "My Bookings" -> mainNavController.navigate("myBookings")
                                    "Home" -> {
                                        mainNavController.navigate("mainScreen") {
                                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                    "My Beauty Points" -> mainNavController.navigate("beautyPoints")
                                    "Help and Support" -> mainNavController.navigate("helpSupport")
                                    "Logout" -> {
                                        auth.signOut()
                                        mainNavController.navigate("loginScreen") {
                                            popUpTo("mainScreen") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.onPrimary,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.golden_new),
                                contentDescription = "Golden Trendz Logo",
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(80.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                        }
                    },
                    actions = {
                        Image(
                            painter = painterResource(id = R.drawable.valga),
                            contentDescription = "Valga Partner Logo",
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 10.dp),
                            contentScale = ContentScale.Fit
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Admin-only button, visible only if isAdmin is true
                if (isAdmin) {
                    Button(
                        onClick = { mainNavController.navigate("adminDashboard") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp) // Set a specific height for the button
                            .padding(bottom = 16.dp),
                        shape = RectangleShape, // Make the button a rectangle
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xEB051572)) // Use the specified color
                    ) {
                        Text(
                            text = "Golden TrendZ Admins", // Change the button text
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) { page ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = carouselImages[page]),
                                contentDescription = "Promotional Image ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.menicon),
                                contentDescription = "Men's Services",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Button(
                                onClick = { mainNavController.navigate("beautyParlor/men") },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Men's Services",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.womensservice),
                                contentDescription = "Women's Services",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Button(
                                onClick = { mainNavController.navigate("beautyParlor/women") },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Women's Services",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    shape = RectangleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Combo Offers",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                }
            }
        }
    }
}