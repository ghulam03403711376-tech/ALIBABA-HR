package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.PayrollSlip
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// Safe Core Icon Mappings to bypass standard/extended dependencies compile discrepancies
private val IconPunchClock = Icons.Default.Info
private val IconFingerprint = Icons.Default.Lock
private val IconPeople = Icons.Default.Person
private val IconUpcoming = Icons.Default.List
private val IconReceiptLong = Icons.Default.Done
private val IconDashboard = Icons.Default.Menu
private val IconGroups = Icons.Default.Person
private val IconStorefront = Icons.Default.Home
private val IconBrightness3 = Icons.Default.Star
private val IconWarningAmbiguity = Icons.Default.Warning
private val IconCloudUpload = Icons.Default.Add
private val IconCalculate = Icons.Default.Refresh
private val IconPictureAsPdf = Icons.Default.CheckCircle
private val IconBorderAll = Icons.Default.List
private val IconAccountCircle = Icons.Default.Person
private val IconPin = Icons.Default.Lock

private fun isFriday(dateStr: String): Boolean {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    } catch (e: Exception) {
        false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val viewModel: MainViewModel = viewModel()
    val employees by viewModel.allEmployees.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val leaves by viewModel.allLeaves.collectAsState()
    val holidays by viewModel.allHolidays.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val config by viewModel.currentConfig.collectAsState()

    val currentRole by viewModel.currentUserRole.collectAsState()
    val currentEmployeeUser by viewModel.currentEmployeeUser.collectAsState()
    val activeShopFilter by viewModel.activeShopFilter.collectAsState()

    val simulatedDate by viewModel.simulatedDate.collectAsState()
    val simulatedTime by viewModel.simulatedTime.collectAsState()
    val biometricState by viewModel.biometricState.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    val infoMsg by viewModel.infoMessage.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    var activeTab by remember { mutableStateOf("Dashboard") } // Dashboard, Terminal, Employees, Leaves, Payroll, Logs

    // Snackbars & feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(infoMsg) {
        infoMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                // Top Custom Simulator Helper Bar
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                IconPunchClock,
                                contentDescription = "Time controls",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulated Time Console:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick inputs to adjust simulation date
                            Button(
                                onClick = { viewModel.setSimulatedDate("2026-05-28") }, // Thursday (Weekday)
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (simulatedDate == "2026-05-28") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (simulatedDate == "2026-05-28") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("Thu (Weekday)", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = { viewModel.setSimulatedDate("2026-05-29") }, // Friday (Auto Shift trigger)
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (simulatedDate == "2026-05-29") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (simulatedDate == "2026-05-29") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("Fri (Auto Shift)", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Hour fast forwards
                            Text(
                                text = "$simulatedDate | $simulatedTime",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    // advance time by 15 mins
                                    val parts = simulatedTime.split(":")
                                    var hr = parts[0].toInt()
                                    var mn = parts[1].toInt() + 15
                                    if (mn >= 60) {
                                        hr = (hr + 1) % 24
                                        mn -= 60
                                    }
                                    viewModel.setSimulatedTime(String.format("%02d:%02d", hr, mn))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Forward 15m", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Main Store Header Theme (Geometric Balance)
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ALIBABA CENTRE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = GeoBlue600
                                )
                                Text(
                                    text = "HR & Attendance",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeoSlate900
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // User Role Display Badge
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GeoBlue50)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Role: $currentRole",
                                        color = GeoBlue700,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Sign Out Sim and switch role triggers
                                var showRoleDropdown by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GeoBlue100)
                                        .border(1.dp, GeoBlue100, CircleShape)
                                        .clickable { showRoleDropdown = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "SA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GeoBlue700
                                    )
                                }
                                DropdownMenu(
                                    expanded = showRoleDropdown,
                                    onDismissRequest = { showRoleDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("As CEO (Global Remote)") },
                                        onClick = { viewModel.setUserRole("CEO"); showRoleDropdown = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("As Super Admin (Full Control)") },
                                        onClick = { viewModel.setUserRole("Super Admin"); showRoleDropdown = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("As HR Manager") },
                                        onClick = { viewModel.setUserRole("HR Manager"); showRoleDropdown = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("As Shabbir Ahmed (Shop 1 Mgr)") },
                                        onClick = {
                                            val shabbir = employees.find { it.fullName == "Shabbir Ahmed" }
                                            viewModel.setUserRole("Shop Manager", shabbir)
                                            viewModel.setShopFilter(1)
                                            showRoleDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("As Pitras Noor (Shop 2 Mgr)") },
                                        onClick = {
                                            val pitras = employees.find { it.fullName == "Pitras Noor" }
                                            viewModel.setUserRole("Shop Manager", pitras)
                                            viewModel.setShopFilter(2)
                                            showRoleDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("As Employee (Self Attendance)") },
                                        onClick = {
                                            val staff = employees.find { it.shiftGroup == "A" }
                                            viewModel.setUserRole("Employee", staff)
                                            showRoleDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Shop Selector (Geometric Tabs)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            val tabs = listOf(0 to "Combined Shops", 1 to "Kids Hall", 2 to "Men's Hall")
                            tabs.forEach { (shopId, title) ->
                                val isSelected = activeShopFilter == shopId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setShopFilter(shopId) }
                                        .padding(vertical = 12.dp)
                                        .drawBehind {
                                            if (isSelected) {
                                                drawLine(
                                                    color = GeoBlue600,
                                                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) GeoBlue600 else GeoSlate400
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Triple("Dashboard", IconDashboard, "Admin"),
                        Triple("Terminal", IconFingerprint, "Terminal"),
                        Triple("Employees", IconPeople, "Staff"),
                        Triple("Leaves", IconUpcoming, "Leaves"),
                        Triple("Payroll", IconReceiptLong, "Payroll"),
                        Triple("Logs", Icons.Default.Lock, "Logs")
                    ).forEach { item ->
                        val isSelected = activeTab == item.first
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = item.first }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GeoBlue100 else Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = item.second,
                                    contentDescription = item.third,
                                    tint = if (isSelected) GeoBlue700 else GeoSlate400,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.third.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GeoBlue700 else GeoSlate400,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GeoBackground)
        ) {
            when (activeTab) {
                "Dashboard" -> DashboardScreen(viewModel, employees, attendance, holidays, activeShopFilter)
                "Terminal" -> TerminalScannerScreen(viewModel, employees, attendance, simulatedDate, simulatedTime, biometricState)
                "Employees" -> EmployeeManagementScreen(viewModel, employees, activeShopFilter)
                "Leaves" -> LeaveManagementScreen(viewModel, leaves, employees, currentRole)
                "Payroll" -> PayrollScreen(viewModel, employees, simulatedDate)
                "Logs" -> SecurityLogsScreen(viewModel, logs, alerts)
            }
        }
    }
}

// 1. DASHBOARD COMPONENT
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    employees: List<Employee>,
    attendance: List<AttendanceRecord>,
    holidays: List<Holiday>,
    activeShopFilter: Int
) {
    val scrollState = rememberScrollState()
    val today = viewModel.simulatedDate.collectAsState().value

    val shopEmployees = if (activeShopFilter == 0) employees else employees.filter { it.shopId == activeShopFilter }
    val presents = attendance.filter { it.dateString == today && it.status != "Absent" && it.status != "On Leave" }.map { it.employeeId }
    val presentCount = shopEmployees.count { presents.contains(it.id) }
    val absentCount = shopEmployees.size - presentCount - attendance.count { it.dateString == today && it.status == "On Leave" && shopEmployees.any { emp -> emp.id == it.employeeId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GeoBlue600, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val mgrLabel = when (activeShopFilter) {
                        1 -> "Shabbir Ahmed"
                        2 -> "Pitras Noor"
                        else -> "All Outlets Active"
                    }
                    Text(
                        text = "ACTIVE SHOP MANAGER",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8F),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = mgrLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Shift: 09:00 AM — 08:30 PM",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7F)
                    )
                }
                
                // Live mode badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2F), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3F), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(GeoGreen400)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LIVE MODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(
                title = "Present",
                value = "$presentCount/${shopEmployees.size}",
                icon = Icons.Default.CheckCircle,
                color = GeoSlate800,
                modifier = Modifier.weight(1F)
            )
            KpiCard(
                title = "Late",
                value = "0",
                icon = Icons.Default.CheckCircle,
                color = GeoAmber600,
                modifier = Modifier.weight(1F)
            )
            KpiCard(
                title = "Absent",
                value = "$absentCount",
                icon = Icons.Default.Close,
                color = GeoRose600,
                modifier = Modifier.weight(1F)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "6-Month Attendance Comparison",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Orange: Shop 1 (Kids Hall)  |  Green: Shop 2 (Men's Hall)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                val gridColor = Color.LightGray.copy(alpha = 0.4F)
                val shop1Color = AlibabaOrange
                val shop2Color = AlibabaMint

                val rows = 4
                val cols = 6
                val rowHeight = size.height / rows
                val colWidth = size.width / (cols - 1)

                for (i in 0..rows) {
                    val y = i * rowHeight
                    drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0F, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }

                val shop1Points = listOf(0.85F, 0.92F, 0.78F, 0.95F, 0.88F, 0.94F)
                val shop2Points = listOf(0.75F, 0.83F, 0.89F, 0.91F, 0.84F, 0.90F)

                val shop1Path = Path().apply {
                    val startY = size.height * (1F - shop1Points[0])
                    moveTo(0F, startY)
                    for (i in 1..5) {
                        val x = i * colWidth
                        val y = size.height * (1F - shop1Points[i])
                        lineTo(x, y)
                    }
                }

                val shop2Path = Path().apply {
                    val startY = size.height * (1F - shop2Points[0])
                    moveTo(0F, startY)
                    for (i in 1..5) {
                        val x = i * colWidth
                        val y = size.height * (1F - shop2Points[i])
                        lineTo(x, y)
                    }
                }

                drawPath(shop1Path, color = shop1Color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                drawPath(shop2Path, color = shop2Color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                for (i in 0..5) {
                    val x = i * colWidth
                    drawCircle(color = shop1Color, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, size.height * (1F - shop1Points[i])))
                    drawCircle(color = shop2Color, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, size.height * (1F - shop2Points[i])))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun").forEach { month ->
                Text(month, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Active Outlets & Religious Holidays List",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GeoSlate900
        )
        Text(
            "Moon-sighted holidays auto-triggered. 2x special overtime applies.",
            fontSize = 11.sp,
            color = GeoSlate500
        )

        Spacer(modifier = Modifier.height(8.dp))

        holidays.forEach { hol ->
            val isShopMatch = activeShopFilter == 0 || hol.shopId == 0 || hol.shopId == activeShopFilter
            if (isShopMatch) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GeoSlate100)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                IconBrightness3,
                                contentDescription = "Moon sighting marker photo",
                                tint = GeoBlue600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(hol.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GeoSlate800)
                        }
                        Text(
                            text = hol.dateString,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoSlate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, GeoSlate100)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeoSlate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}


// 2. BIOMETRIC TERMINAL COMPONENT
@Composable
fun TerminalScannerScreen(
    viewModel: MainViewModel,
    employees: List<Employee>,
    attendance: List<AttendanceRecord>,
    simulatedDate: String,
    simulatedTime: String,
    biometricState: String
) {
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    val currentSelectedEmployee = employees.find { it.id == selectedEmployeeId }

    val activeRecord = attendance.find {
        it.employeeId == currentSelectedEmployee?.id && it.dateString == simulatedDate
    }

    val isFriday = isFriday(simulatedDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Alibaba Biometric Portal",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            "Please select an active employee to simulate fingerprint scanning inputs.",
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        var showPickerMenu by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showPickerMenu = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentSelectedEmployee?.fullName ?: "Select Employee to scan...",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(Icons.Default.ArrowBack, contentDescription = "Open selector")
                }
            }
            DropdownMenu(
                expanded = showPickerMenu,
                onDismissRequest = { showPickerMenu = false },
                modifier = Modifier.fillMaxWidth(0.9F)
            ) {
                employees.forEach { emp ->
                    DropdownMenuItem(
                        text = {
                            Text("${emp.fullName} (${emp.designation} — Group ${emp.shiftGroup})")
                        },
                        onClick = {
                            selectedEmployeeId = emp.id
                            showPickerMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentSelectedEmployee != null) {
            val shiftTimes = viewModel.getShiftTimes(currentSelectedEmployee, simulatedDate)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, GeoSlate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${currentSelectedEmployee.fullName} — ${currentSelectedEmployee.designation}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GeoSlate900
                    )
                    Text(
                        "Outlets Center: ${if (currentSelectedEmployee.shopId == 1) "Shop 1 (Kids Hall)" else "Shop 2 (Men's Hall)"}",
                        fontSize = 12.sp,
                        color = GeoSlate500
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = GeoSlate100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Standard Shift Group", fontSize = 10.sp, color = GeoSlate400, fontWeight = FontWeight.SemiBold)
                            Text("Group ${currentSelectedEmployee.shiftGroup}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GeoSlate800)
                        }
                        Column {
                            Text("Active Shift Hours", fontSize = 10.sp, color = GeoSlate400, fontWeight = FontWeight.SemiBold)
                            Text("${shiftTimes.first} to ${shiftTimes.second}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GeoBlue600)
                        }
                        Column {
                            Text("Status", fontSize = 10.sp, color = GeoSlate400, fontWeight = FontWeight.SemiBold)
                            val isLocked = currentSelectedEmployee.locked
                            Text(
                                if (isLocked) "LOCKED" else "ACTIVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isLocked) GeoRose600 else GeoGreen400
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Punch Stage Flow (Biometric verification matching required)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AttendanceStepBadge(
                    label = "CHECK-IN",
                    actionTime = activeRecord?.checkInTime,
                    ignored = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceStepBadge(
                    label = "LUNCH OUT",
                    actionTime = activeRecord?.lunchOutTime,
                    ignored = isFriday
                )
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceStepBadge(
                    label = "LUNCH IN",
                    actionTime = activeRecord?.lunchInTime,
                    ignored = isFriday
                )
                Spacer(modifier = Modifier.width(8.dp))
                AttendanceStepBadge(
                    label = "CHECK-OUT",
                    actionTime = activeRecord?.checkOutTime,
                    ignored = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Punch Console
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(1.25f)
                        .clip(CircleShape)
                        .background(GeoBlue500.copy(alpha = 0.1f))
                )
                // Inner ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(1.10f)
                        .clip(CircleShape)
                        .background(GeoBlue500.copy(alpha = 0.2f))
                )
                
                // Main Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .border(2.dp, GeoBlue600, CircleShape)
                        .clickable {
                            viewModel.simulateBiometricScan(
                                biometricId = currentSelectedEmployee.biometricId ?: "",
                                expectedEmployee = currentSelectedEmployee
                            ) { isMatch ->
                                if (isMatch) {
                                    val nextStep = when {
                                        activeRecord?.checkInTime == null -> 1
                                        !isFriday && activeRecord.lunchOutTime == null -> 2
                                        !isFriday && activeRecord.lunchInTime == null -> 3
                                        activeRecord.checkOutTime == null -> 4
                                        else -> 0
                                    }
    
                                    if (nextStep > 0) {
                                        viewModel.executeAttendanceAction(currentSelectedEmployee, nextStep)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconFingerprint,
                        contentDescription = "Pulsing fingerprint scan device locator",
                        tint = when (biometricState) {
                            "SUCCESS" -> GeoGreen400
                            "FAILED" -> GeoRose600
                            else -> GeoBlue600
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "FIDO2 BIOMETRIC SCAN",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GeoSlate800,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Please place your registered finger on the reader",
                fontSize = 11.sp,
                color = GeoSlate500,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    viewModel.simulateBiometricScan(
                        biometricId = "fp_unknown_fake",
                        expectedEmployee = null
                    ) { }
                }
            ) {
                Icon(IconWarningAmbiguity, contentDescription = "Spoof tests", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simulate Suspicious Unregistered Fingerprint")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    viewModel.overrideWithPin(currentSelectedEmployee, currentSelectedEmployee.pin) { isSuccess ->
                        if (isSuccess) {
                            val nextStep = when {
                                activeRecord?.checkInTime == null -> 1
                                !isFriday && activeRecord.lunchOutTime == null -> 2
                                !isFriday && activeRecord.lunchInTime == null -> 3
                                activeRecord.checkOutTime == null -> 4
                                else -> 0
                            }
                            if (nextStep > 0) {
                                viewModel.executeAttendanceAction(currentSelectedEmployee, nextStep)
                            }
                        }
                    }
                }
            ) {
                Icon(IconPin, contentDescription = "Pin bypass option", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Admin/Manager Secure PIN Override Bypass")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Assign or pick an employee profile above to begin daily operations.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AttendanceStepBadge(
    label: String,
    actionTime: Long?,
    ignored: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                when {
                    ignored -> GeoSlate50
                    actionTime != null -> GeoBlue100
                    else -> GeoSlate200
                }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                ignored -> GeoSlate400
                actionTime != null -> GeoBlue700
                else -> GeoSlate600
            }
        )
    }
}


// 3. EMPLOYEE MANAGEMENT SCREEN
@Composable
fun EmployeeManagementScreen(
    viewModel: MainViewModel,
    employees: List<Employee>,
    activeShopFilter: Int
) {
    val shopEmployees = if (activeShopFilter == 0) employees else employees.filter { it.shopId == activeShopFilter }
    var searchQuery by remember { mutableStateOf("") }
    val filteredEmployees = shopEmployees.filter { it.fullName.contains(searchQuery, ignoreCase = true) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Employees Catalog (${shopEmployees.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoSlate900
            )

            Button(
                onClick = {
                    val dummyImports = listOf(
                        Employee(
                            fullName = "Kamil Khan",
                            cnic = "34101-9988776-1",
                            phone = "0345-1234567",
                            homeAddress = "Awan Outpost, Lahore",
                            shopId = 1,
                            department = "Sales",
                            designation = "Associate",
                            shiftGroup = "B",
                            salaryType = "Monthly",
                            salaryAmount = 24000.0,
                            commissionPercentage = 0.01,
                            leavePolicy = "No leave",
                            overtimePolicy = true,
                            biometricId = "fp_kamil",
                            qrCode = "ALIBABA-EMP-501",
                            employeePhoto = "placeholder_avatar",
                            joiningDate = "2026-05-28",
                            status = "Active"
                        ),
                        Employee(
                            fullName = "Zainab Bibi",
                            cnic = "34101-9988776-2",
                            phone = "0345-7654321",
                            homeAddress = "Main Road, Sialkot",
                            shopId = 2,
                            department = "Accounts",
                            designation = "Senior Clerk",
                            shiftGroup = "A",
                            salaryType = "Monthly",
                            salaryAmount = 35000.0,
                            commissionPercentage = 0.01,
                            leavePolicy = "2/month",
                            overtimePolicy = false,
                            biometricId = "fp_zainab",
                            qrCode = "ALIBABA-EMP-502",
                            employeePhoto = "placeholder_avatar",
                            joiningDate = "2026-05-28",
                            status = "Active"
                        )
                    )
                    viewModel.submitExcelImportSimulation(dummyImports)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeoBlue600, contentColor = Color.White)
            ) {
                Icon(IconCloudUpload, contentDescription = "Excel Upload", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bulk Excel Import", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name...", color = GeoSlate400) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GeoBlue600,
                unfocusedBorderColor = GeoSlate200,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = GeoSlate400) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredEmployees) { emp ->
                val cardColor = if (emp.locked) Color(0xFFFFF1F2) else Color.White
                val borderColor = if (emp.locked) GeoRose600.copy(alpha = 0.4f) else GeoSlate100
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emp.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GeoSlate900)
                            val statusText = if (emp.locked) "LOCKED" else emp.status
                            val color = if (emp.locked) GeoRose600 else GeoGreen400
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(color.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    statusText.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Designation: ${emp.designation}", fontSize = 12.sp, color = GeoSlate500)
                        Text("CNIC: ${emp.cnic}", fontSize = 12.sp, color = GeoSlate500)
                        Text("Phone: ${emp.phone}", fontSize = 12.sp, color = GeoSlate500)
                        
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeoBlue50)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Outlet: ${if (emp.shopId == 1) "Shop 1 - Kids Hall" else "Shop 2 - Men's Hall"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoBlue700
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = GeoSlate100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Base Salary: Rs. ${emp.salaryAmount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoSlate800
                            )

                            if (emp.locked) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.repository.updateLockStatus(emp.id, false)
                                            viewModel.addAlert("System Admin unlocked ${emp.fullName} account.")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoAmber600, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("Reset Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// 4. LEAVE ARRANGEMENT SCREEN
@Composable
fun LeaveManagementScreen(
    viewModel: MainViewModel,
    leaves: List<LeaveRequest>,
    employees: List<Employee>,
    currentRole: String
) {
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    var selectedLeaveType by remember { mutableStateOf("Casual") }
    var leaveReason by remember { mutableStateOf("") }
    var showEmpMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Leave Allocation & Approvals",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = GeoSlate900
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GeoSlate100),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Submit New Leave Request",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GeoSlate800
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showEmpMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoSlate200),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = GeoSlate50)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val emp = employees.find { it.id == selectedEmployeeId }
                            Text(emp?.fullName ?: "Pick staff member...", color = GeoSlate800, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowBack, contentDescription = "picker", tint = GeoSlate500, modifier = Modifier.size(16.dp).scale(scaleX = 1f, scaleY = -1f)) // Rotating/mirroring arrow for dropdown vibe
                        }
                    }
                    DropdownMenu(expanded = showEmpMenu, onDismissRequest = { showEmpMenu = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(text = { Text(emp.fullName) }, onClick = { selectedEmployeeId = emp.id; showEmpMenu = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var showTypeMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showTypeMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GeoSlate200),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = GeoSlate50)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedLeaveType, color = GeoSlate800, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowBack, contentDescription = "picker", tint = GeoSlate500, modifier = Modifier.size(16.dp).scale(scaleX = 1f, scaleY = -1f))
                        }
                    }
                    DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                        listOf("Casual", "Medical", "Annual", "Unpaid", "Friday leave").forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { selectedLeaveType = type; showTypeMenu = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = leaveReason,
                    onValueChange = { leaveReason = it },
                    placeholder = { Text("Reason for absence leave...", color = GeoSlate400) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeoBlue600,
                        unfocusedBorderColor = GeoSlate200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val empId = selectedEmployeeId
                        if (empId != null && leaveReason.isNotBlank()) {
                            viewModel.submitLeaveRequest(empId, selectedLeaveType, viewModel.simulatedDate.value, leaveReason)
                            leaveReason = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoBlue600, contentColor = Color.White)
                ) {
                    Text("Submit Request", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Pending Leaves Trail",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = GeoSlate900
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(leaves) { req ->
                val requester = employees.find { it.id == req.employeeId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GeoSlate100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(requester?.fullName ?: "Staff", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GeoSlate900)
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(GeoBlue50)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    req.dateString,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoBlue700
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Type: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoSlate500
                            )
                            Text(
                                req.leaveType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoSlate800
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Status: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeoSlate500
                            )
                            
                            val statusColor = when (req.status) {
                                "Approved" -> GeoGreen400
                                "Rejected" -> GeoRose600
                                else -> GeoAmber600
                            }
                            Text(
                                req.status.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Reason: ${req.reason}",
                            fontSize = 12.sp,
                            color = GeoSlate500
                        )

                        if (req.status == "Pending" && currentRole != "Employee") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.handleLeaveStatus(req, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoRose600, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 8.dp).height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.handleLeaveStatus(req, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreen400, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// 5. PAYROLL RECEIPT GENERATOR COMPONENTS
@Composable
fun PayrollScreen(
    viewModel: MainViewModel,
    employees: List<Employee>,
    simulatedDate: String
) {
    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    val currentSelectedEmployee = employees.find { it.id == selectedEmployeeId }
    val targetMonth = simulatedDate.substring(0, 7)

    var generatedSlip by remember { mutableStateOf<PayrollSlip?>(null) }
    var isDownloadedReceipt by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Alibaba Payroll Engine",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = GeoSlate900
        )
        Text(
            "Calculates complex itemized salaries matching multi-outlets retail rules.",
            fontSize = 12.sp,
            color = GeoSlate500,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        var showEmployeesList by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showEmployeesList = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GeoSlate200),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = GeoSlate50)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(currentSelectedEmployee?.fullName ?: "Pick employee to generate...", color = GeoSlate800, fontSize = 13.sp)
                    Icon(Icons.Default.ArrowBack, contentDescription = "dropdown toggle", tint = GeoSlate500, modifier = Modifier.size(16.dp).scale(scaleX = 1f, scaleY = -1f))
                }
            }
            DropdownMenu(expanded = showEmployeesList, onDismissRequest = { showEmployeesList = false }) {
                employees.forEach { emp ->
                    DropdownMenuItem(text = { Text(emp.fullName) }, onClick = {
                        selectedEmployeeId = emp.id
                        generatedSlip = null
                        isDownloadedReceipt = false
                        showEmployeesList = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentSelectedEmployee != null) {
            Button(
                onClick = {
                    generatedSlip = viewModel.generatePayrollSlip(currentSelectedEmployee, targetMonth)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GeoBlue600, contentColor = Color.White)
            ) {
                Icon(IconCalculate, contentDescription = "Calc stats")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Payroll Calculations", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            generatedSlip?.let { slip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GeoSlate100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ALIBABA CENTRE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GeoBlue600,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(GeoBlue50)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "MONTHLY SLIP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoBlue700
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Shop Outlets: ${slip.shopName}", fontSize = 12.sp, color = GeoSlate500)
                        Text("Statement Month: ${slip.month}", fontSize = 12.sp, color = GeoSlate500)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(slip.employeeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GeoSlate900)
                        Text(slip.designation, fontSize = 12.sp, color = GeoSlate500)

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = GeoSlate100)

                        SlipItem(label = "Base Salary", value = "Rs. ${slip.baseSalary}")
                        SlipItem(label = "Retail Commission (${slip.commissionSales} Sales)", value = "Rs. ${slip.commissionEarned}")
                        SlipItem(label = "Overtime (${slip.overtimeHours} hrs)", value = "Rs. ${slip.overtimeEarned}")
                        SlipItem(label = "Simulated Meal Allowances", value = "Rs. ${slip.mealAllowance}")
                        SlipItem(label = "Performance Target Bonus", value = "Rs. ${slip.performanceBonus}")

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = GeoSlate100)

                        SlipItem(label = "Late Penalties (${slip.totalLateMinutes}m, 120m free)", value = "- Rs. ${slip.lateDeduction}", tint = GeoRose600)
                        SlipItem(label = "Excess Leaves Unpaid Deductions", value = "- Rs. ${slip.leaveDeductions}", tint = GeoRose600)

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = GeoSlate100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Net Pay",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = GeoSlate900
                            )
                            Text(
                                "Rs. ${slip.finalSalary}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = GeoBlue600
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isDownloadedReceipt) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        isDownloadedReceipt = true
                                        viewModel.addAlert("EXPORT: PDF sheet saved local folder.")
                                    },
                                    border = BorderStroke(1.dp, GeoSlate200),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1F)
                                ) {
                                    Icon(IconPictureAsPdf, contentDescription = "pdf", tint = GeoSlate600, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoSlate700)
                                }

                                Button(
                                    onClick = {
                                        isDownloadedReceipt = true
                                        viewModel.addAlert("EXPORT: Excel audit payroll sheet generated.")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1F),
                                    colors = ButtonDefaults.buttonColors(containerColor = GeoBlue600, contentColor = Color.White)
                                ) {
                                    Icon(IconBorderAll, contentDescription = "excel", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GeoGreen400.copy(alpha = 0.3f)),
                                colors = CardDefaults.cardColors(containerColor = GeoGreen400.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "verified", tint = GeoGreen400, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "File Downloaded! Alibaba Centre Stamp verified & recorded.",
                                        fontSize = 11.sp,
                                        color = GeoGreen400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Select employee to see monthly payroll calculations.", color = GeoSlate400, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SlipItem(label: String, value: String, tint: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)
    }
}


// 6. LOGS AND VERIFICATION CHANNELS COMPONENT
@Composable
fun SecurityLogsScreen(viewModel: MainViewModel, dbLogs: List<AuditLog>, liveAlerts: List<String>) {
    var filterByFidoOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Security Audit Console",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeoSlate900
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "FIDO2 Info Only",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GeoSlate600
                )
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = filterByFidoOnly,
                    onCheckedChange = { filterByFidoOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GeoBlue600,
                        uncheckedThumbColor = GeoSlate400,
                        uncheckedTrackColor = GeoSlate200
                    ),
                    modifier = Modifier.scale(0.8F)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "whatsapp",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "WhatsApp Alerts Broadcast Status:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Simulated SMS/WhatsApp endpoint is active! Transmitting instantaneous alerts on late arrivals, locked screens, fraud biometrics, and manual overrides of shop managers.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B5E20)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Live Alerts Log",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GeoSlate800
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GeoSlate100, RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            LazyColumn {
                items(liveAlerts) { alert ->
                    Text(
                        text = alert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (alert.contains("WARNING") || alert.contains("CRITICAL")) GeoRose600 else GeoSlate700,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Encrypted Database Audit Trail Logs",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GeoSlate800
        )
        Spacer(modifier = Modifier.height(8.dp))

        val visibleLogs = if (filterByFidoOnly) {
            dbLogs.filter { it.action.contains("BIO") || it.action.contains("LOCK") }
        } else dbLogs

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visibleLogs) { log ->
                val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                val isCritical = log.action.contains("FAILED") || log.action.contains("LOCK")
                val cardBgColor = if (isCritical) GeoRose600.copy(alpha = 0.05f) else Color.White
                val borderColor = if (isCritical) GeoRose600.copy(alpha = 0.3f) else GeoSlate100
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${log.action}] — ${log.userName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isCritical) GeoRose600 else GeoSlate800
                            )
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeoSlate400
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.details,
                            fontSize = 11.sp,
                            color = GeoSlate600
                        )
                    }
                }
            }
        }
    }
}
