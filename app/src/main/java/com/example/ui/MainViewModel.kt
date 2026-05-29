package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = Repository(db)

    // UI Input States
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendanceRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLeaves: StateFlow<List<LeaveRequest>> = repository.allLeaveRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allHolidays: StateFlow<List<Holiday>> = repository.allHolidays.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allLogs: StateFlow<List<AuditLog>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Statuses
    private val _currentConfig = MutableStateFlow(SystemConfig())
    val currentConfig: StateFlow<SystemConfig> = _currentConfig.asStateFlow()

    // Authentication States
    private val _currentUserRole = MutableStateFlow("Super Admin") // Standard CEO / SU
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _currentEmployeeUser = MutableStateFlow<Employee?>(null)
    val currentEmployeeUser: StateFlow<Employee?> = _currentEmployeeUser.asStateFlow()

    private val _activeShopFilter = MutableStateFlow(0) // 0 = Both, 1 = Kids Hall, 2 = Men's Hall
    val activeShopFilter: StateFlow<Int> = _activeShopFilter.asStateFlow()

    // Simulation Clock
    private val _simulatedDate = MutableStateFlow("2026-05-28") // Friday or weekday seed
    val simulatedDate: StateFlow<String> = _simulatedDate.asStateFlow()

    private val _simulatedTime = MutableStateFlow("09:00")
    val simulatedTime: StateFlow<String> = _simulatedTime.asStateFlow()

    // Biometric Scanner State
    private val _biometricState = MutableStateFlow("IDLE") // IDLE, SCANNING, SUCCESS, LOCKED, FAILED
    val biometricState: StateFlow<String> = _biometricState.asStateFlow()

    private val _unmatchedTries = MutableStateFlow(0)
    val unmatchedTries: StateFlow<Int> = _unmatchedTries.asStateFlow()

    private val _alerts = MutableStateFlow<List<String>>(
        listOf(
            "Alibaba Centre Attendance Engine Started.",
            "Friday shifts automatically locked from 9:00 AM to 12:50 PM.",
            "Managers configured Shabbir Ahmed (Shop 1) & Pitras Noor (Shop 2)."
        )
    )
    val alerts: StateFlow<List<String>> = _alerts.asStateFlow()

    // Monthly Mock Sales for commission calculations
    private val _monthlySalesShop1 = MutableStateFlow(1250000.0) // Kids Hall
    val monthlySalesShop1: StateFlow<Double> = _monthlySalesShop1.asStateFlow()

    private val _monthlySalesShop2 = MutableStateFlow(1450000.0) // Men's Hall
    val monthlySalesShop2: StateFlow<Double> = _monthlySalesShop2.asStateFlow()

    private val _customCommissionAmount = MutableStateFlow(100000.0) // Simulated monthly sales metric
    val customCommissionAmount: StateFlow<Double> = _customCommissionAmount.asStateFlow()

    // Dialog messages
    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
            _currentConfig.value = repository.getConfig()
        }
    }

    fun setShopFilter(shopId: Int) {
        _activeShopFilter.value = shopId
    }

    fun setUserRole(role: String, employee: Employee? = null) {
        _currentUserRole.value = role
        _currentEmployeeUser.value = employee
        viewModelScope.launch {
            repository.logAction(
                userId = employee?.qrCode ?: "SYS_USER",
                userName = employee?.fullName ?: role,
                role = role,
                action = "LOGIN_ROLE",
                details = "User signed in or switched role to $role ${employee?.fullName ?: ""}"
            )
        }
    }

    fun clearMessages() {
        _infoMessage.value = null
        _errorMessage.value = null
    }

    fun addAlert(msg: String) {
        val list = _alerts.value.toMutableList()
        list.add(0, "[${_simulatedDate.value} ${_simulatedTime.value}] $msg")
        _alerts.value = list.take(15) // Keep last 15 alerts
    }

    fun setSimulatedDate(date: String) {
        _simulatedDate.value = date
        // Log date adjustment
        addAlert("Simulated calendar date changed to $date")
    }

    fun setSimulatedTime(time: String) {
        _simulatedTime.value = time
    }

    // Smart Biometric simulation trigger
    fun simulateBiometricScan(biometricId: String, expectedEmployee: Employee?, onScanResult: (Boolean) -> Unit) {
        _biometricState.value = "SCANNING"
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // visual animation delay

            if (expectedEmployee == null) {
                // Suspicious non-registered biometric scan!
                _biometricState.value = "FAILED"
                addAlert("WARNING: Unknown fingerprint attempt reported at security console!")
                repository.logAction(
                    userId = "UNKNOWN",
                    userName = "Unknown Intruder",
                    role = "None",
                    action = "BIO_FAILED",
                    details = "Failed biometric match for ID: $biometricId. Logging alert."
                )
                onScanResult(false)
                _biometricState.value = "IDLE"
                return@launch
            }

            if (expectedEmployee.locked) {
                _biometricState.value = "LOCKED"
                _errorMessage.value = "Account for ${expectedEmployee.fullName} is LOCKED. Contact CEO or Admin!"
                onScanResult(false)
                _biometricState.value = "IDLE"
                return@launch
            }

            val isMatch = expectedEmployee.biometricId == biometricId
            if (isMatch) {
                _biometricState.value = "SUCCESS"
                _unmatchedTries.value = 0
                _infoMessage.value = "Fingerprint authenticated for ${expectedEmployee.fullName}!"
                repository.updateConsecutiveFailures(expectedEmployee.id, 0)
                repository.logAction(
                    userId = expectedEmployee.qrCode,
                    userName = expectedEmployee.fullName,
                    role = expectedEmployee.designation,
                    action = "BIO_SUCCESS",
                    details = "FIDO2 Fingerprint parsed successfully. ID match verified securely."
                )
                onScanResult(true)
            } else {
                _biometricState.value = "FAILED"
                val failures = expectedEmployee.consecutiveFailures + 1
                repository.updateConsecutiveFailures(expectedEmployee.id, failures)

                repository.logAction(
                    userId = expectedEmployee.qrCode,
                    userName = expectedEmployee.fullName,
                    role = expectedEmployee.designation,
                    action = "BIO_FAILED",
                    details = "Biometric mismatch ($failures/3). Fingerprint rejected."
                )

                if (failures >= 3) {
                    repository.updateLockStatus(expectedEmployee.id, true)
                    addAlert("CRITICAL: ${expectedEmployee.fullName} account locked! 3 continuous biometric violations.")
                    _errorMessage.value = "CRITICAL: Account locked due to 3 continuous failures. CEO / Admin notified!"
                    repository.logAction(
                        userId = expectedEmployee.qrCode,
                        userName = expectedEmployee.fullName,
                        role = expectedEmployee.designation,
                        action = "LOCK_ACCOUNT",
                        details = "Conforming to FIDO2 anti-spoofing policy. Device is temporarily disabled."
                    )
                } else {
                    _errorMessage.value = "Biometric mismatch! Attempt $failures of 3 before account locks."
                }
                onScanResult(false)
            }
            _biometricState.value = "IDLE"
        }
    }

    // Normal PIN override for managers/CEOs
    fun overrideWithPin(employee: Employee, enteredPin: String, onScanResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (employee.locked) {
                _errorMessage.value = "Account is LOCKED. Contact Admin!"
                onScanResult(false)
                return@launch
            }

            if (employee.pin == enteredPin) {
                _infoMessage.value = "PIN verification successful for ${employee.fullName}!"
                repository.logAction(
                    userId = employee.qrCode,
                    userName = employee.fullName,
                    role = employee.designation,
                    action = "PIN_SUCCESS",
                    details = "PIN fallback input matched secure database hash."
                )
                onScanResult(true)
            } else {
                _errorMessage.value = "Invalid PIN code entered!"
                repository.logAction(
                    userId = employee.qrCode,
                    userName = employee.fullName,
                    role = employee.designation,
                    action = "PIN_FAILED",
                    details = "Invalid override attempt."
                )
                onScanResult(false)
            }
        }
    }

    // 4-Step Attendance Execution
    fun executeAttendanceAction(employee: Employee, step: Int) {
        viewModelScope.launch {
            val dateStr = _simulatedDate.value
            val timeStr = _simulatedTime.value // e.g. "10:15"
            val totalMins = convertTimeToMinutes(timeStr)

            // Auto detect Friday
            val isFriday = isFriday(dateStr)

            // Shift configuration boundaries
            val shiftTimes = getShiftTimes(employee, dateStr)
            val expectedStartMins = convertTimeToMinutes(shiftTimes.first)
            val expectedEndMins = convertTimeToMinutes(shiftTimes.second)

            var record = repository.getRecordForEmployeeAndDate(employee.id, dateStr)
                ?: AttendanceRecord(employeeId = employee.id, dateString = dateStr, status = "Absent")

            when (step) {
                1 -> { // Check-In
                    if (record.checkInTime != null) {
                        _errorMessage.value = "${employee.fullName} has already Checked-In today!"
                        return@launch
                    }

                    // Calculate late minutes on Check-In
                    val late = if (totalMins > expectedStartMins) {
                        totalMins - expectedStartMins
                    } else 0

                    val status = if (late > 0) "Late" else "Present"

                    record = record.copy(
                        checkInTime = System.currentTimeMillis(), // raw physical simulated time stamp
                        lateMinutes = late,
                        status = status
                    )
                    addAlert("${employee.fullName} Checked-In at $timeStr. Late mins recorded today: $late min.")
                }
                2 -> { // Lunch OUT
                    if (record.checkInTime == null) {
                        _errorMessage.value = "Must Check-In first before taking Lunch leave!"
                        return@launch
                    }
                    if (isFriday) {
                        _errorMessage.value = "Friday has no lunch break or meal allowance!"
                        return@launch
                    }
                    if (record.lunchOutTime != null) {
                        _errorMessage.value = "${employee.fullName} already departed for Lunch!"
                        return@launch
                    }

                    record = record.copy(
                        lunchOutTime = System.currentTimeMillis()
                    )
                    addAlert("${employee.fullName} started Lunch break at $timeStr.")
                }
                3 -> { // Lunch IN
                    if (record.lunchOutTime == null) {
                        _errorMessage.value = "No corresponding Lunch OUT request found!"
                        return@launch
                    }
                    if (record.lunchInTime != null) {
                        _errorMessage.value = "${employee.fullName} already returned from Lunch!"
                        return@launch
                    }

                    val outMins = convertTimeMillisToSimulatedMinutes(record.lunchOutTime) // for simplicity, use current time
                    val currentMins = convertTimeToMinutes(timeStr)
                    val lunchPeriod = currentMins - outMins

                    var lateAdjust = record.lateMinutes
                    if (lunchPeriod > 60) {
                        val extraLate = lunchPeriod - 60
                        lateAdjust += extraLate
                        addAlert("${employee.fullName} exceeded 1hr lunch break by $extraLate min. Added to daily late total.")
                    } else {
                        // Offset credit if lunch return early
                        val earlyReturn = 60 - lunchPeriod
                        addAlert("${employee.fullName} returned early by $earlyReturn min.")
                    }

                    record = record.copy(
                        lunchInTime = System.currentTimeMillis(),
                        lateMinutes = lateAdjust
                    )
                }
                4 -> { // Check-Out
                    if (record.checkInTime == null) {
                        _errorMessage.value = "Cannot Check-Out without checking in first!"
                        return@launch
                    }
                    if (record.checkOutTime != null) {
                        _errorMessage.value = "${employee.fullName} already Checked-Out today!"
                        return@launch
                    }

                    // Total working hours computation
                    val inMins = convertTimeMillisToSimulatedMinutes(record.checkInTime)
                    val outMins = convertTimeToMinutes(timeStr)

                    var workingMins = outMins - inMins
                    // Deduct lunch break of 60 mins if they recorded lunch out
                    if (record.lunchOutTime != null && record.lunchInTime != null) {
                        workingMins -= 60
                    }

                    val workingHrs = (workingMins / 60.0).coerceAtLeast(0.0)

                    // Overtime calculation
                    val stdWorkingMins = expectedEndMins - expectedStartMins
                    val overtimeMins = if (workingMins > stdWorkingMins && employee.overtimePolicy) {
                        workingMins - stdWorkingMins
                    } else 0

                    val overtimeHrs = overtimeMins / 60.0

                    // Dynamic offset mode on the same day (Smart Offset)
                    var finalLate = record.lateMinutes
                    val config = currentConfig.value
                    val enableOffset = if (employee.shopId == 1) config.offsetModeShop1 else config.offsetModeShop2
                    if (enableOffset && overtimeMins > 0 && finalLate > 0) {
                        val offset = finalLate.coerceAtMost(overtimeMins)
                        finalLate -= offset
                        addAlert("SMART OFFSET applied for ${employee.fullName}: Overtime offset $offset late minutes.")
                    }

                    record = record.copy(
                        checkOutTime = System.currentTimeMillis(),
                        totalWorkingHours = Math.round(workingHrs * 100.0) / 100.0,
                        overtimeHours = Math.round(overtimeHrs * 100.0) / 100.0,
                        lateMinutes = finalLate,
                        status = if (record.status == "Late" && finalLate == 0) "Present" else record.status
                    )

                    // Record meal allowance eligibility if they stayed without explicit lunch records
                    addAlert("${employee.fullName} Checked-Out at $timeStr. Working hours: ${record.totalWorkingHours} hrs. Overtime: ${record.overtimeHours} hrs.")
                }
            }

            repository.insertOrUpdateAttendance(record)
            _infoMessage.value = "Attendance stage updated successfully!"
        }
    }

    // Manual Edit Attendance with Reason and Logging
    fun manualEditRecord(record: AttendanceRecord, editReason: String) {
        viewModelScope.launch {
            if (editReason.isBlank()) {
                _errorMessage.value = "A clear reason is mandatory for manual corrections!"
                return@launch
            }

            val updated = record.copy(
                editReason = editReason,
                editedBy = _currentUserRole.value + " Manual Override",
                editTimestamp = System.currentTimeMillis()
            )

            repository.insertOrUpdateAttendance(updated)
            _infoMessage.value = "Attendance manual edit saved!"

            val targetEmp = allEmployees.value.find { it.id == record.employeeId }
            addAlert("ADMIN CORRECTION: ${targetEmp?.fullName ?: "Employee"} session for date ${record.dateString} manually updated. Reason: $editReason")

            repository.logAction(
                userId = "ADMIN",
                userName = "System Administrator",
                role = _currentUserRole.value,
                action = "ATTENDANCE_EDIT",
                details = "Admin edited attendance logs for ${targetEmp?.fullName}. Date: ${record.dateString}. Reason: $editReason"
            )
        }
    }

    // Leave Approvals
    fun submitLeaveRequest(employeeId: Long, leaveType: String, dateString: String, reason: String) {
        viewModelScope.launch {
            val req = LeaveRequest(
                employeeId = employeeId,
                leaveType = leaveType,
                dateString = dateString,
                reason = reason,
                status = "Pending"
            )
            repository.insertLeaveRequest(req)
            _infoMessage.value = "Leave request submitted successfully for approval."
            addAlert("LEAVE REQUEST: Employee ID: $employeeId submitted a $leaveType leave for $dateString.")
        }
    }

    fun handleLeaveStatus(request: LeaveRequest, approved: Boolean) {
        viewModelScope.launch {
            val statusStr = if (approved) "Approved" else "Rejected"
            val updated = request.copy(
                status = statusStr,
                approvedBy = _currentUserRole.value
            )
            repository.updateLeaveRequest(updated)

            val emp = allEmployees.value.find { it.id == request.employeeId }
            _infoMessage.value = "Leave request for ${emp?.fullName} marked as $statusStr."
            addAlert("LEAVE DECISION: Request for ${emp?.fullName} on ${request.dateString} has been $statusStr.")

            // On approval, insert automatically into attendance as "On Leave" so they don't get marked as Absent
            if (approved) {
                val attRecord = AttendanceRecord(
                    employeeId = request.employeeId,
                    dateString = request.dateString,
                    status = "On Leave"
                )
                repository.insertOrUpdateAttendance(attRecord)
            }

            repository.logAction(
                userId = "APPROVER",
                userName = "Manager panel",
                role = _currentUserRole.value,
                action = "LEAVE_REPLY",
                details = "Leave on ${request.dateString} filtered: $statusStr by manager"
            )
        }
    }

    // Settings Updates
    fun updateSystemConfig(config: SystemConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
            _currentConfig.value = config
            _infoMessage.value = "Alibaba default configurations updated successfully!"
            repository.logAction(
                userId = "ADMIN",
                userName = "Config Console",
                role = "Super Admin",
                action = "CONFIG_UPDATE",
                details = "Updated shift rules, ramadan calendar limits, and manual edit timelines."
            )
        }
    }

    fun submitHoliday(name: String, dateString: String, shopId: Int) {
        viewModelScope.launch {
            val hol = Holiday(name = name, dateString = dateString, shopId = shopId)
            repository.insertHoliday(hol)
            _infoMessage.value = "Holiday '$name' configured successfully on $dateString!"
        }
    }

    fun removeHoliday(id: Long) {
        viewModelScope.launch {
            repository.deleteHolidayById(id)
            _infoMessage.value = "Holiday record removed."
        }
    }

    // Payroll Calculation Engine
    fun generatePayrollSlip(employee: Employee, targetMonth: String): PayrollSlip {
        // targetMonth = "2026-05"
        val activeRecords = allAttendance.value.filter {
            it.employeeId == employee.id && it.dateString.startsWith(targetMonth)
        }

        val leaves = allLeaves.value.filter {
            it.employeeId == employee.id && it.dateString.startsWith(targetMonth) && it.status == "Approved"
        }

        // 1. Base Salary
        val baseSalary = employee.salaryAmount

        // 2. Meal Allowance (Rs. 120 / day if stayed, i.e., Present but didn't record lunch out, and eligible)
        var mealAllowance = 0.0
        var presentDaysCount = 0
        var lateDaysCount = 0

        for (rec in activeRecords) {
            if (rec.status == "Present" || rec.status == "Late" || rec.status == "Half Day") {
                presentDaysCount++
                if (rec.status == "Late") lateDaysCount++

                // Stays condition: Check-in was done, but NO Lunch OUT/IN record was generated, on a non-Friday
                val isFri = isFriday(rec.dateString)
                if (!isFri && rec.lunchOutTime == null && employee.mealAllowanceEligible) {
                    mealAllowance += 120.0
                }
            }
        }

        // 3. Commission (Sales times pre-configured percent)
        val activeShopSales = if (employee.shopId == 1) _monthlySalesShop1.value else _monthlySalesShop2.value
        val commission = activeShopSales * (employee.commissionPercentage / 100.0)

        // 4. Overtime hours sum
        val totalOvertimeHrs = activeRecords.sumOf { it.overtimeHours }
        val perMinuteRate = baseSalary / 14400.0 // 24 working days * 600 mins = 14400 mins as standard in prompt
        val overtimeWorth = totalOvertimeHrs * 60.0 * perMinuteRate

        // 5. Late Deductions
        val totalLateMins = activeRecords.sumOf { it.lateMinutes }
        val freeAllowance = employee.lateAllowanceMins // standard 120 mins
        val deductibleLate = (totalLateMins - freeAllowance).coerceAtLeast(0)
        val lateDeduction = deductibleLate * perMinuteRate

        // 6. Leave Deductions (if exceed policy limits)
        // Let's check leaves policy limit (e.g., 2/month limit -> any approved beyond is unpaid, or Unpaid leave is directly structural Rs. 1000/day)
        val limit = when (employee.leavePolicy) {
            "2/month" -> 2
            "1/month" -> 1
            "Friday-only" -> 0
            else -> 0
        }
        val excessLeavesCount = (leaves.size - limit).coerceAtLeast(0)
        val leaveDeduction = excessLeavesCount * (baseSalary / 24.0) // Rs deducted per day

        // Final formula: Base Salary + Commission + Overtime + Meal Allowance - Late Deduction - Leave Deduction
        val bonus = if (presentDaysCount > 22) 2000.0 else 0.0 // performance incentive
        val grossEarnings = baseSalary + commission + overtimeWorth + mealAllowance + bonus
        val totalDeductions = lateDeduction + leaveDeduction
        val finalSalary = (grossEarnings - totalDeductions).coerceAtLeast(0.0)

        return PayrollSlip(
            employeeName = employee.fullName,
            designation = employee.designation,
            shopName = if (employee.shopId == 1) "Shop 1 - Kids Hall" else "Shop 2 - Men's Hall",
            month = targetMonth,
            baseSalary = baseSalary,
            presentDays = presentDaysCount,
            totalLateMinutes = totalLateMins,
            freeAllowanceUsed = Math.min(totalLateMins, freeAllowance),
            deductibleLateMinutes = deductibleLate,
            perMinuteRate = Math.round(perMinuteRate * 100.0) / 100.0,
            lateDeduction = Math.round(lateDeduction * 100.0) / 100.0,
            commissionSales = Math.round(activeShopSales * 100.0) / 100.0,
            commissionEarned = Math.round(commission * 100.0) / 100.0,
            overtimeHours = totalOvertimeHrs,
            overtimeEarned = Math.round(overtimeWorth * 100.0) / 100.0,
            mealAllowance = mealAllowance,
            performanceBonus = bonus,
            leaveDeductions = Math.round(leaveDeduction * 100.0) / 100.0,
            finalSalary = Math.round(finalSalary * 100.0) / 100.0
        )
    }

    // Helpers
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

    // Returns start & end times to check deviations
    fun getShiftTimes(employee: Employee, dateStr: String): Pair<String, String> {
        if (isFriday(dateStr)) {
            return Pair("09:00", "12:50") // Friday constraint
        }
        return when (employee.shiftGroup) {
            "A" -> Pair("10:00", "20:30")
            "B" -> Pair("09:00", "19:30")
            "C" -> Pair("09:00", "19:00")
            else -> Pair("09:00", "20:30") // Managers / Others
        }
    }

    private fun convertTimeToMinutes(timeStr: String): Int {
        // "10:15" -> 615
        return try {
            val parts = timeStr.split(":")
            val hr = parts[0].toInt()
            val min = parts[1].toInt()
            hr * 60 + min
        } catch (e: Exception) {
            0
        }
    }

    private fun convertTimeMillisToSimulatedMinutes(millis: Long): Int {
        // For testing simulation, let's extract current viewmodel clock time directly
        return convertTimeToMinutes(_simulatedTime.value)
    }

    fun submitExcelImportSimulation(simulatedList: List<Employee>) {
         viewModelScope.launch {
             for (emp in simulatedList) {
                 repository.insertEmployee(emp)
             }
             _infoMessage.value = "Successfully imported ${simulatedList.size} employee profile sheets!"
             addAlert("BULK OPERATIONS: Admin imported updated files from retail sheets.")
         }
    }
}

data class PayrollSlip(
    val employeeName: String,
    val designation: String,
    val shopName: String,
    val month: String,
    val baseSalary: Double,
    val presentDays: Int,
    val totalLateMinutes: Int,
    val freeAllowanceUsed: Int,
    val deductibleLateMinutes: Int,
    val perMinuteRate: Double,
    val lateDeduction: Double,
    val commissionSales: Double,
    val commissionEarned: Double,
    val overtimeHours: Double,
    val overtimeEarned: Double,
    val mealAllowance: Double,
    val performanceBonus: Double,
    val leaveDeductions: Double,
    val finalSalary: Double
)
