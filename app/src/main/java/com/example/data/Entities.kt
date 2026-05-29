package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val cnic: String,
    val phone: String,
    val homeAddress: String,
    val shopId: Int, // 1 for Shop 1 (Kids Hall), 2 for Shop 2 (Men's Hall)
    val department: String,
    val designation: String,
    val shiftGroup: String, // "A", "B", "C", "Manager"
    val salaryType: String, // "Daily", "Weekly", "Monthly"
    val salaryAmount: Double,
    val commissionPercentage: Double, // e.g. 0.02 for 0.02%
    val leavePolicy: String, // "No leave", "1/month", "2/month", "Friday-only", "Mixed"
    val overtimePolicy: Boolean,
    val lateAllowanceMins: Int = 120,
    val mealAllowanceEligible: Boolean = true,
    val biometricId: String?, // Linked FIDO2 ID
    val qrCode: String,
    val employeePhoto: String, // base64 or placeholder url
    val joiningDate: String,
    val status: String, // "Active", "Inactive"
    val locked: Boolean = false,
    val pin: String = "1234",
    val consecutiveFailures: Int = 0
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val checkInTime: Long? = null,
    val lunchOutTime: Long? = null,
    val lunchInTime: Long? = null,
    val checkOutTime: Long? = null,
    val totalWorkingHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val lateMinutes: Int = 0,
    val status: String = "Absent", // "Present", "Absent", "Late", "Half Day", "On Leave"
    val editReason: String? = null,
    val editedBy: String? = null,
    val editTimestamp: Long? = null
)

@Entity(tableName = "leave_requests")
data class LeaveRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val leaveType: String, // "Casual", "Medical", "Annual", "Unpaid", "Friday leave"
    val dateString: String, // "YYYY-MM-DD"
    val reason: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val approvedBy: String? = null
)

@Entity(tableName = "holiday_calendar")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateString: String, // "YYYY-MM-DD"
    val shopId: Int // 0 for Both Shops, 1 for Kids Hall, 2 for Men's Hall
)

@Entity(tableName = "system_configs")
data class SystemConfig(
    @PrimaryKey val id: Int = 1,
    val ramadanShop1Active: Boolean = false,
    val ramadanShop2Active: Boolean = false,
    val offsetModeShop1: Boolean = false, // Overtime offsets late minutes on the same day
    val offsetModeShop2: Boolean = false,
    val biometricOnlyShop1: Boolean = false,
    val biometricOnlyShop2: Boolean = false,
    val manualEditWindowDays: Int = 7
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)
