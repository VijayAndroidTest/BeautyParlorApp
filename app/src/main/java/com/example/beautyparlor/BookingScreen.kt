package com.example.beautyparlor

import android.net.Uri
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beautyparlor.entities.ServiceSubItem as ServiceSubItemEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    parentServiceName: String, // Add the parent service name here
    service: ServiceSubItemEntity,
    onConfirm: (date: String, time: String) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf<Calendar?>(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf("") }
    val today = remember { Calendar.getInstance() }

    val timeSlots = listOf(
        "10:00AM", "11:00AM", "12:00PM",
        "01:00PM", "02:00PM", "03:00PM",
        "04:00PM", "05:00PM", "06:00PM",
        "07:00PM", "08:00PM", "09:00PM"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Appointment",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF9C27B0)
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(16.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedDate != null && selectedTime.isNotEmpty()) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateString = dateFormat.format(selectedDate!!.time)

                            // Fix: Pass the parentServiceName here
                            val encodedParentServiceName = Uri.encode(parentServiceName)
                            val encodedSubItemName = Uri.encode(service.subItemName)
                            val encodedDate = Uri.encode(dateString)
                            val encodedTime = Uri.encode(selectedTime)

                            navController.navigate(
                                "bookingSummary/$encodedParentServiceName/$encodedSubItemName/${service.priceRange}/$encodedDate/$encodedTime"
                            )
                            Toast.makeText(context, "Proceeding to Summary...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDate != null && selectedTime.isNotEmpty())
                            Color(0xFF9C27B0) else Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Confirm Date and Time",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = paddingValues,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            // Display both service and sub-item names
                            "You selected: $parentServiceName - ${service.subItemName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9C27B0)
                        )
                        Text(
                            "Price: ₹${service.priceRange}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            // ... (rest of the code for BookingScreen remains the same)
            item {
                Text(
                    "Select a date",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Start
                )
            }
            item {
                CalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = {
                        selectedDate = it
                        selectedTime = ""
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Text(
                    "Choose Available Time Slot",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color(0xFF4CAF50), text = "Available")
                    LegendItem(color = Color(0xFFFF9800), text = "Filling Fast")
                    LegendItem(color = Color(0xFFFF5252), text = "Not Available")
                }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(timeSlots) { time ->
                        val isToday = selectedDate?.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                                selectedDate?.get(Calendar.YEAR) == today.get(Calendar.YEAR)

                        val timeFormat = SimpleDateFormat("hh:mma", Locale.getDefault())
                        val timeSlotDate = Calendar.getInstance().apply {
                            try {
                                this.time = timeFormat.parse(time) ?: Date()
                            } catch (e: Exception) {
                                this.time = Date()
                            }
                        }

                        val slotCalendar = (selectedDate?.clone() as Calendar?)?.apply {
                            set(Calendar.HOUR_OF_DAY, timeSlotDate.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, timeSlotDate.get(Calendar.MINUTE))
                        }

                        val isPastTime = isToday && (slotCalendar?.before(today) ?: false)
                        val slotStatus = if (isPastTime) "not_available" else "available"

                        val isEnabled = slotStatus != "not_available"
                        val borderColor = when (slotStatus) {
                            "available" -> Color(0xFF0A0A0A)
                            "filling_fast" -> Color(0xFF0E0E0E)
                            "not_available" -> Color(0xFFFF5252)
                            else -> Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clickable(enabled = isEnabled) { selectedTime = time }
                                .border(
                                    width = 2.dp,
                                    color = if (selectedTime == time) Color(0xFF026206) else borderColor,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    color = if (selectedTime == time) Color(0xFF016005) else Color.White,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = time,
                                color = when {
                                    selectedTime == time || !isEnabled -> Color.White
                                    else -> Color.Black
                                },
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val today = remember { Calendar.getInstance() }

    val months = remember {
        (0 until 12).map { i ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, i)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.time
        }
    }

    val lazyListState = rememberLazyListState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header with Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canGoPrevious = lazyListState.firstVisibleItemIndex > 0
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val targetIndex = lazyListState.firstVisibleItemIndex - 1
                            if (targetIndex >= 0) {
                                lazyListState.animateScrollToItem(targetIndex)
                            }
                        }
                    },
                    enabled = canGoPrevious
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = if (canGoPrevious) Color(0xFF9C27B0) else Color.Gray
                    )
                }

                val currentVisibleMonth =
                    months.getOrNull(lazyListState.firstVisibleItemIndex)?.let {
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(it)
                    } ?: ""

                Text(
                    text = currentVisibleMonth,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )

                val canGoNext = lazyListState.firstVisibleItemIndex < months.size - 1

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val targetIndex = lazyListState.firstVisibleItemIndex + 1
                            if (targetIndex < months.size) {
                                lazyListState.animateScrollToItem(targetIndex)
                            }
                        }
                    },
                    enabled = canGoNext
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = if (canGoNext) Color(0xFF9C27B0) else Color.Gray)
                }
            }

            // Days of the week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        day,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally scrolling list of months
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
            ) {
                items(months) { month ->
                    MonthView(
                        modifier = Modifier.fillParentMaxWidth(),
                        month = month,
                        today = today,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

@Composable
fun MonthView(
    modifier: Modifier = Modifier,
    month: Date,
    today: Calendar,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val monthCalendar = Calendar.getInstance().apply { time = month }
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDayOfWeek = (monthCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Correctly adjusts for Sunday as the first day (0-indexed)

    Column(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
        ) {
            items(firstDayOfWeek + daysInMonth) { index ->
                if (index < firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(40.dp))
                } else {
                    val day = index - firstDayOfWeek + 1
                    val date = (monthCalendar.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    val isPastDate = date.before(today)
                    val isSelected = selectedDate?.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                            selectedDate?.get(Calendar.YEAR) == date.get(Calendar.YEAR)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = when {
                                    isSelected -> Color(0xFF9C27B0)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .clickable(enabled = !isPastDate) {
                                onDateSelected(date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = when {
                                isSelected -> Color.White
                                isPastDate -> Color.LightGray
                                else -> Color.Black
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp)
    }
}