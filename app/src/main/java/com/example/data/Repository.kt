package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val database: AppDatabase) {

    val allEmployees: Flow<List<Employee>> = database.employeeDao.getAllEmployeesFlow()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = database.attendanceDao.getAllAttendanceRecordsFlow()
    val allLeaveRequests: Flow<List<LeaveRequest>> = database.leaveRequestDao.getAllLeavesFlow()
    val allHolidays: Flow<List<Holiday>> = database.holidayDao.getAllHolidaysFlow()
    val allLogs: Flow<List<AuditLog>> = database.auditLogDao.getAllLogsFlow()

    suspend fun getEmployeeById(id: Long) = database.employeeDao.getEmployeeById(id)
    suspend fun getEmployeeByBiometricId(id: String) = database.employeeDao.getEmployeeByBiometricId(id)
    suspend fun insertEmployee(employee: Employee) = database.employeeDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = database.employeeDao.updateEmployee(employee)
    suspend fun deleteEmployeeById(id: Long) = database.employeeDao.deleteEmployeeById(id)
    suspend fun updateConsecutiveFailures(id: Long, failures: Int) = database.employeeDao.updateConsecutiveFailures(id, failures)
    suspend fun updateLockStatus(id: Long, locked: Boolean) = database.employeeDao.updateLockStatus(id, locked)

    suspend fun getAttendanceByDate(dateString: String) = database.attendanceDao.getAttendanceByDate(dateString)
    suspend fun getRecordForEmployeeAndDate(employeeId: Long, dateString: String) = database.attendanceDao.getRecordForEmployeeAndDate(employeeId, dateString)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord) = database.attendanceDao.insertOrUpdate(record)
    suspend fun getRecordsForEmployee(employeeId: Long) = database.attendanceDao.getRecordsForEmployee(employeeId)

    suspend fun insertLeaveRequest(request: LeaveRequest) = database.leaveRequestDao.insertLeave(request)
    suspend fun updateLeaveRequest(request: LeaveRequest) = database.leaveRequestDao.updateLeave(request)
    suspend fun getLeavesForEmployee(employeeId: Long) = database.leaveRequestDao.getLeavesForEmployee(employeeId)

    suspend fun insertHoliday(holiday: Holiday) = database.holidayDao.insertHoliday(holiday)
    suspend fun deleteHolidayById(id: Long) = database.holidayDao.deleteHolidayById(id)

    suspend fun getConfig(): SystemConfig {
        return database.systemConfigDao.getConfig() ?: SystemConfig().also {
            database.systemConfigDao.saveConfig(it)
        }
    }
    suspend fun saveConfig(config: SystemConfig) = database.systemConfigDao.saveConfig(config)

    suspend fun insertLog(log: AuditLog) = database.auditLogDao.insertLog(log)
    suspend fun logAction(userId: String, userName: String, role: String, action: String, details: String) {
        val auditLog = AuditLog(
            userId = userId,
            userName = userName,
            userRole = role,
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        database.auditLogDao.insertLog(auditLog)
    }

    suspend fun seedIfNeeded() {
        val count = database.employeeDao.getAllEmployees()
        if (count.isEmpty()) {
            // Seed base configuration
            database.systemConfigDao.saveConfig(
                SystemConfig(
                    id = 1,
                    ramadanShop1Active = false,
                    ramadanShop2Active = false,
                    offsetModeShop1 = true,
                    offsetModeShop2 = true,
                    biometricOnlyShop1 = true,
                    biometricOnlyShop2 = true,
                    manualEditWindowDays = 15
                )
            )

            // Seed employees
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val seedEmployees = listOf(
                // Shabbir Ahmed - Kids Hall Manager (Shop 1)
                Employee(
                    fullName = "Shabbir Ahmed",
                    cnic = "34101-2345678-1",
                    phone = "0300-1111111",
                    homeAddress = "Street 1, Near Kids Hall, Gujranwala",
                    shopId = 1,
                    department = "Management",
                    designation = "Shop Manager",
                    shiftGroup = "Manager",
                    salaryType = "Monthly",
                    salaryAmount = 65000.0,
                    commissionPercentage = 0.05,
                    leavePolicy = "2/month",
                    overtimePolicy = true,
                    biometricId = "fp_shabbir",
                    qrCode = "ALIBABA-EMP-101",
                    employeePhoto = "placeholder_shabbir",
                    joiningDate = today,
                    status = "Active",
                    pin = "1011"
                ),
                // Pitras Noor - Men's Hall Manager (Shop 2)
                Employee(
                    fullName = "Pitras Noor",
                    cnic = "34101-2345678-2",
                    phone = "0300-2222222",
                    homeAddress = "Main Bazaar, Sialkot",
                    shopId = 2,
                    department = "Management",
                    designation = "Shop Manager",
                    shiftGroup = "Manager",
                    salaryType = "Monthly",
                    salaryAmount = 65000.0,
                    commissionPercentage = 0.05,
                    leavePolicy = "2/month",
                    overtimePolicy = true,
                    biometricId = "fp_pitras",
                    qrCode = "ALIBABA-EMP-102",
                    employeePhoto = "placeholder_pitras",
                    joiningDate = today,
                    status = "Active",
                    pin = "2022"
                ),
                // Group A (Shop 1) - Ali Saeed, Osama Shaikh, Mis Kaynat
                Employee(
                    fullName = "Ali Saeed",
                    cnic = "34101-3456789-1",
                    phone = "0340-0000001",
                    homeAddress = "Model Town, Lahore",
                    shopId = 1,
                    department = "Sales",
                    designation = "Senior Sales Executive",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 35000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "1/month",
                    overtimePolicy = true,
                    biometricId = "fp_ali",
                    qrCode = "ALIBABA-EMP-201",
                    employeePhoto = "placeholder_ali",
                    joiningDate = today,
                    status = "Active"
                ),
                Employee(
                    fullName = "Osama Shaikh",
                    cnic = "34101-3456789-2",
                    phone = "0340-0000002",
                    homeAddress = "Samanabad, Lahore",
                    shopId = 1,
                    department = "Sales",
                    designation = "Sales Representative",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 30000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "1/month",
                    overtimePolicy = true,
                    biometricId = "fp_osama",
                    qrCode = "ALIBABA-EMP-202",
                    employeePhoto = "placeholder_osama",
                    joiningDate = today,
                    status = "Active"
                ),
                Employee(
                    fullName = "Mis Kaynat",
                    cnic = "34101-3456789-3",
                    phone = "0340-0000003",
                    homeAddress = "Gulshan Colony, Lahore",
                    shopId = 1,
                    department = "Accounts",
                    designation = "Cashier & Accountant",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 32000.0,
                    commissionPercentage = 0.01,
                    leavePolicy = "2/month",
                    overtimePolicy = false,
                    biometricId = "fp_kaynat",
                    qrCode = "ALIBABA-EMP-203",
                    employeePhoto = "placeholder_kaynat",
                    joiningDate = today,
                    status = "Active"
                ),
                // Group A (Shop 2) - Umar, Shamreez, Imran
                Employee(
                    fullName = "Umar",
                    cnic = "34101-4567890-1",
                    phone = "0340-0000004",
                    homeAddress = "Main Sanda Road, Lahore",
                    shopId = 2,
                    department = "Sales",
                    designation = "Sales Assistant",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 30000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "1/month",
                    overtimePolicy = true,
                    biometricId = "fp_umar",
                    qrCode = "ALIBABA-EMP-204",
                    employeePhoto = "placeholder_umar",
                    joiningDate = today,
                    status = "Active"
                ),
                Employee(
                    fullName = "Shamreez",
                    cnic = "34101-4567890-2",
                    phone = "0340-0000005",
                    homeAddress = "Green Town, Sialkot",
                    shopId = 2,
                    department = "Inventory",
                    designation = "Warehouse Assistant",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 28000.0,
                    commissionPercentage = 0.01,
                    leavePolicy = "No leave",
                    overtimePolicy = true,
                    biometricId = "fp_shamreez",
                    qrCode = "ALIBABA-EMP-205",
                    employeePhoto = "placeholder_shamreez",
                    joiningDate = today,
                    status = "Active"
                ),
                Employee(
                    fullName = "Imran",
                    cnic = "34101-4567890-3",
                    phone = "0340-0000006",
                    homeAddress = "Pasrur Road, Sialkot",
                    shopId = 2,
                    department = "Sales",
                    designation = "Junior Sales",
                    shiftGroup = "A",
                    salaryType = "Monthly",
                    salaryAmount = 28000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "No leave",
                    overtimePolicy = true,
                    biometricId = "fp_imran",
                    qrCode = "ALIBABA-EMP-206",
                    employeePhoto = "placeholder_imran",
                    joiningDate = today,
                    status = "Active"
                ),
                // Group B (Shop 1) - Mohibur Rahman, Ghulam Ali Haider
                Employee(
                    fullName = "Mohibur Rahman",
                    cnic = "34101-5678901-1",
                    phone = "0340-0000007",
                    homeAddress = "Civil Lines, Lahore",
                    shopId = 1,
                    department = "Operations",
                    designation = "Floor Executive",
                    shiftGroup = "B",
                    salaryType = "Monthly",
                    salaryAmount = 26000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "No leave",
                    overtimePolicy = true,
                    biometricId = "fp_mohib",
                    qrCode = "ALIBABA-EMP-301",
                    employeePhoto = "placeholder_mohib",
                    joiningDate = today,
                    status = "Active"
                ),
                Employee(
                    fullName = "Ghulam Ali Haider",
                    cnic = "34101-5678901-2",
                    phone = "0340-0000008",
                    homeAddress = "Peoples Colony, Gujranwala",
                    shopId = 1,
                    department = "Office",
                    designation = "Senior Clerk",
                    shiftGroup = "B",
                    salaryType = "Monthly",
                    salaryAmount = 32000.0,
                    commissionPercentage = 0.01,
                    leavePolicy = "1/month",
                    overtimePolicy = false,
                    biometricId = "fp_ghulam",
                    qrCode = "ALIBABA-EMP-302",
                    employeePhoto = "placeholder_ghulam",
                    joiningDate = today,
                    status = "Active"
                ),
                // Group B (Shop 2) - Ahmad Shabbir
                Employee(
                    fullName = "Ahmad Shabbir",
                    cnic = "34101-5678901-3",
                    phone = "0340-0000009",
                    homeAddress = "Main Cantt, Sialkot",
                    shopId = 2,
                    department = "Operations",
                    designation = "Floor Assistant",
                    shiftGroup = "B",
                    salaryType = "Monthly",
                    salaryAmount = 26000.0,
                    commissionPercentage = 0.02,
                    leavePolicy = "No leave",
                    overtimePolicy = true,
                    biometricId = "fp_ahmad",
                    qrCode = "ALIBABA-EMP-303",
                    employeePhoto = "placeholder_ahmad",
                    joiningDate = today,
                    status = "Active"
                ),
                // Group C (Shop 1) - Waheed Gujjar
                Employee(
                    fullName = "Waheed Gujjar",
                    cnic = "34101-6789012-1",
                    phone = "0340-0000010",
                    homeAddress = "Samanabad, Sialkot",
                    shopId = 1,
                    department = "Operations",
                    designation = "General Helper",
                    shiftGroup = "C",
                    salaryType = "Monthly",
                    salaryAmount = 22000.0,
                    commissionPercentage = 0.01,
                    leavePolicy = "Friday-only",
                    overtimePolicy = true,
                    biometricId = "fp_waheed",
                    qrCode = "ALIBABA-EMP-401",
                    employeePhoto = "placeholder_waheed",
                    joiningDate = today,
                    status = "Active"
                ),
                // Group C (Shop 2) - Ijaz Shah
                Employee(
                    fullName = "Ijaz Shah",
                    cnic = "34101-6789012-2",
                    phone = "0340-0000011",
                    homeAddress = "Saddar Town, Sialkot",
                    shopId = 2,
                    department = "Security",
                    designation = "Security Warden",
                    shiftGroup = "C",
                    salaryType = "Monthly",
                    salaryAmount = 25000.0,
                    commissionPercentage = 0.00,
                    leavePolicy = "No leave",
                    overtimePolicy = true,
                    biometricId = "fp_ijaz",
                    qrCode = "ALIBABA-EMP-402",
                    employeePhoto = "placeholder_ijaz",
                    joiningDate = today,
                    status = "Active"
                )
            )

            for (emp in seedEmployees) {
                database.employeeDao.insertEmployee(emp)
            }

            // Seed generic holidays
            val currentYear = today.split("-")[0]
            val defaultHolidays = listOf(
                Holiday(name = "Eid ul Fitr Day 1", dateString = "$currentYear-03-31", shopId = 0),
                Holiday(name = "Eid ul Fitr Day 2", dateString = "$currentYear-04-01", shopId = 0),
                Holiday(name = "Eid ul Adha Day 1", dateString = "$currentYear-06-07", shopId = 0),
                Holiday(name = "Eid ul Adha Day 2", dateString = "$currentYear-06-08", shopId = 0),
                Holiday(name = "10th Muharram (Ashura)", dateString = "$currentYear-07-28", shopId = 0),
                Holiday(name = "12 Rabi ul Awwal (Mawlid)", dateString = "$currentYear-09-15", shopId = 0)
            )
            for (hol in defaultHolidays) {
                database.holidayDao.insertHoliday(hol)
            }

            // Log seeding
            val seedLog = AuditLog(
                userId = "SU-001",
                userName = "Super Admin",
                userRole = "Super Admin",
                action = "SYSTEM_SEED",
                details = "Successfully seeded initial 13 Alibaba Centre employees, 6 national paid holidays, and Shop settings.",
                timestamp = System.currentTimeMillis()
            )
            database.auditLogDao.insertLog(seedLog)
        }
    }
}
