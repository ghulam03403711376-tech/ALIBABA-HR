package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    suspend fun getAllEmployees(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE biometricId = :biometricId")
    suspend fun getEmployeeByBiometricId(biometricId: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Long)

    @Query("UPDATE employees SET consecutiveFailures = :failures WHERE id = :id")
    suspend fun updateConsecutiveFailures(id: Long, failures: Int)

    @Query("UPDATE employees SET locked = :locked, consecutiveFailures = 0 WHERE id = :id")
    suspend fun updateLockStatus(id: Long, locked: Boolean)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY dateString DESC, id DESC")
    fun getAllAttendanceRecordsFlow(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    suspend fun getAttendanceByDate(dateString: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND dateString = :dateString")
    suspend fun getRecordForEmployeeAndDate(employeeId: Long, dateString: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY dateString DESC")
    suspend fun getRecordsForEmployee(employeeId: Long): List<AttendanceRecord>
}

@Dao
interface LeaveRequestDao {
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeavesFlow(): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(request: LeaveRequest): Long

    @Update
    suspend fun updateLeave(request: LeaveRequest)

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY dateString DESC")
    suspend fun getLeavesForEmployee(employeeId: Long): List<LeaveRequest>
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holiday_calendar ORDER BY dateString ASC")
    fun getAllHolidaysFlow(): Flow<List<Holiday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: Holiday): Long

    @Query("DELETE FROM holiday_calendar WHERE id = :id")
    suspend fun deleteHolidayById(id: Long)
}

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_configs WHERE id = 1")
    suspend fun getConfig(): SystemConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: SystemConfig)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllLogsFlow(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)
}

@Database(
    entities = [
        Employee::class,
        AttendanceRecord::class,
        LeaveRequest::class,
        Holiday::class,
        SystemConfig::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val employeeDao: EmployeeDao
    abstract val attendanceDao: AttendanceDao
    abstract val leaveRequestDao: LeaveRequestDao
    abstract val holidayDao: HolidayDao
    abstract val systemConfigDao: SystemConfigDao
    abstract val auditLogDao: AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alibaba_centre_hr_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
