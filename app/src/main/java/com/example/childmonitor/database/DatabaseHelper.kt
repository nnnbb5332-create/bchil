package com.example.childmonitor.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.mindrot.jbcrypt.BCrypt

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "child_monitor.db"
        const val DATABASE_VERSION = 2
        
        // جدول ولياء الأمور
        const val TABLE_PARENTS = "parents"
        const val COL_PARENT_ID = "parent_id"
        const val COL_PARENT_EMAIL = "email"
        const val COL_PARENT_PASSWORD = "password"
        const val COL_PARENT_NAME = "name"
        const val COL_PARENT_CREATED_AT = "created_at"
        
        // جدول الأطفال
        const val TABLE_CHILDREN = "children"
        const val COL_CHILD_ID = "child_id"
        const val COL_CHILD_PARENT_ID = "parent_id"
        const val COL_CHILD_NAME = "name"
        const val COL_CHILD_CODE = "child_code"
        const val COL_CHILD_CREATED_AT = "created_at"
        
        // جدول المواقع
        const val TABLE_LOCATIONS = "locations"
        const val COL_LOCATION_ID = "location_id"
        const val COL_LOCATION_CHILD_ID = "child_id"
        const val COL_LOCATION_LATITUDE = "latitude"
        const val COL_LOCATION_LONGITUDE = "longitude"
        const val COL_LOCATION_ADDRESS = "address"
        const val COL_LOCATION_TIMESTAMP = "timestamp"
        
        // جدول استخدام التطبيقات
        const val TABLE_APP_USAGE = "app_usage"
        const val COL_USAGE_ID = "usage_id"
        const val COL_USAGE_CHILD_ID = "child_id"
        const val COL_USAGE_APP_NAME = "app_name"
        const val COL_USAGE_PACKAGE_NAME = "package_name"
        const val COL_USAGE_TIME = "usage_time"
        const val COL_USAGE_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // إنشاء جدول ولياء الأمور
        db?.execSQL("""
            CREATE TABLE $TABLE_PARENTS (
                $COL_PARENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PARENT_EMAIL TEXT UNIQUE NOT NULL,
                $COL_PARENT_PASSWORD TEXT NOT NULL,
                $COL_PARENT_NAME TEXT NOT NULL,
                $COL_PARENT_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now'))
            )
        """.trimIndent())

        // إنشاء جدول الأطفال
        db?.execSQL("""
            CREATE TABLE $TABLE_CHILDREN (
                $COL_CHILD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CHILD_PARENT_ID INTEGER NOT NULL,
                $COL_CHILD_NAME TEXT NOT NULL,
                $COL_CHILD_CODE TEXT UNIQUE NOT NULL,
                $COL_CHILD_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY ($COL_CHILD_PARENT_ID) REFERENCES $TABLE_PARENTS($COL_PARENT_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // إنشاء جدول المواقع
        db?.execSQL("""
            CREATE TABLE $TABLE_LOCATIONS (
                $COL_LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOCATION_CHILD_ID INTEGER NOT NULL,
                $COL_LOCATION_LATITUDE REAL NOT NULL,
                $COL_LOCATION_LONGITUDE REAL NOT NULL,
                $COL_LOCATION_ADDRESS TEXT,
                $COL_LOCATION_TIMESTAMP INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY ($COL_LOCATION_CHILD_ID) REFERENCES $TABLE_CHILDREN($COL_CHILD_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // إنشاء جدول استخدام التطبيقات
        db?.execSQL("""
            CREATE TABLE $TABLE_APP_USAGE (
                $COL_USAGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USAGE_CHILD_ID INTEGER NOT NULL,
                $COL_USAGE_APP_NAME TEXT NOT NULL,
                $COL_USAGE_PACKAGE_NAME TEXT NOT NULL,
                $COL_USAGE_TIME INTEGER NOT NULL,
                $COL_USAGE_TIMESTAMP INTEGER DEFAULT (strftime('%s', 'now')),
                FOREIGN KEY ($COL_USAGE_CHILD_ID) REFERENCES $TABLE_CHILDREN($COL_CHILD_ID) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // إنشاء فهارس للبحث السريع
        db?.execSQL("CREATE INDEX idx_parent_email ON $TABLE_PARENTS($COL_PARENT_EMAIL)")
        db?.execSQL("CREATE INDEX idx_child_code ON $TABLE_CHILDREN($COL_CHILD_CODE)")
        db?.execSQL("CREATE INDEX idx_child_parent ON $TABLE_CHILDREN($COL_CHILD_PARENT_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_APP_USAGE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHILDREN")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PARENTS")
        onCreate(db)
    }

    // ==================== دوال ولياء الأمور ====================
    
    fun registerParent(email: String, password: String, name: String): Long {
        val db = writableDatabase
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        
        val values = ContentValues().apply {
            put(COL_PARENT_EMAIL, email.lowercase())
            put(COL_PARENT_PASSWORD, hashedPassword)
            put(COL_PARENT_NAME, name)
        }
        
        return try {
            db.insert(TABLE_PARENTS, null, values)
        } catch (e: Exception) {
            -1
        }
    }
    
    fun loginParent(email: String, password: String): Parent? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PARENTS,
            null,
            "$COL_PARENT_EMAIL = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PARENT_PASSWORD))
            if (BCrypt.checkpw(password, storedHash)) {
                Parent(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PARENT_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_PARENT_EMAIL)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PARENT_NAME))
                )
            } else {
                cursor.close()
                null
            }
        } else {
            cursor.close()
            null
        }
    }
    
    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PARENTS,
            arrayOf(COL_PARENT_ID),
            "$COL_PARENT_EMAIL = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
    
    // ==================== دوال الأطفال ====================
    
    fun addChild(parentId: Long, childName: String): String? {
        val db = writableDatabase
        val childCode = generateChildCode()
        
        val values = ContentValues().apply {
            put(COL_CHILD_PARENT_ID, parentId)
            put(COL_CHILD_NAME, childName)
            put(COL_CHILD_CODE, childCode)
        }
        
        return try {
            val id = db.insert(TABLE_CHILDREN, null, values)
            if (id != -1L) childCode else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun loginChild(childCode: String): Child? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CHILDREN,
            null,
            "$COL_CHILD_CODE = ?",
            arrayOf(childCode.uppercase()),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            Child(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHILD_ID)),
                parentId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHILD_PARENT_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHILD_NAME)),
                code = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHILD_CODE))
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }
    
    fun getChildrenByParent(parentId: Long): List<Child> {
        val children = mutableListOf<Child>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CHILDREN,
            null,
            "$COL_CHILD_PARENT_ID = ?",
            arrayOf(parentId.toString()),
            null, null,
            "$COL_CHILD_CREATED_AT DESC"
        )
        
        while (cursor.moveToNext()) {
            children.add(Child(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHILD_ID)),
                parentId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CHILD_PARENT_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHILD_NAME)),
                code = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHILD_CODE))
            ))
        }
        cursor.close()
        return children
    }
    
    fun deleteChild(childId: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_CHILDREN, "$COL_CHILD_ID = ?", arrayOf(childId.toString())) > 0
    }
    
    fun isChildCodeExists(code: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CHILDREN,
            arrayOf(COL_CHILD_ID),
            "$COL_CHILD_CODE = ?",
            arrayOf(code.uppercase()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
    
    // ==================== دوال المواقع ====================
    
    fun insertLocation(childId: Long, latitude: Double, longitude: Double, address: String?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION_CHILD_ID, childId)
            put(COL_LOCATION_LATITUDE, latitude)
            put(COL_LOCATION_LONGITUDE, longitude)
            put(COL_LOCATION_ADDRESS, address)
        }
        return db.insert(TABLE_LOCATIONS, null, values)
    }
    
    fun getChildLocations(childId: Long, limit: Int = 100): List<LocationData> {
        val locations = mutableListOf<LocationData>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LOCATIONS,
            null,
            "$COL_LOCATION_CHILD_ID = ?",
            arrayOf(childId.toString()),
            null, null,
            "$COL_LOCATION_TIMESTAMP DESC",
            limit.toString()
        )
        
        while (cursor.moveToNext()) {
            locations.add(LocationData(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_ID)),
                childId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_CHILD_ID)),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOCATION_LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOCATION_LONGITUDE)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_ADDRESS)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_TIMESTAMP))
            ))
        }
        cursor.close()
        return locations
    }
    
    // ==================== دوال استخدام التطبيقات ====================
    
    fun insertAppUsage(childId: Long, appName: String, packageName: String, usageTime: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USAGE_CHILD_ID, childId)
            put(COL_USAGE_APP_NAME, appName)
            put(COL_USAGE_PACKAGE_NAME, packageName)
            put(COL_USAGE_TIME, usageTime)
        }
        return db.insert(TABLE_APP_USAGE, null, values)
    }
    
    fun getChildAppUsage(childId: Long, limit: Int = 100): List<AppUsageData> {
        val usageList = mutableListOf<AppUsageData>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_APP_USAGE,
            null,
            "$COL_USAGE_CHILD_ID = ?",
            arrayOf(childId.toString()),
            null, null,
            "$COL_USAGE_TIMESTAMP DESC",
            limit.toString()
        )
        
        while (cursor.moveToNext()) {
            usageList.add(AppUsageData(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USAGE_ID)),
                childId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USAGE_CHILD_ID)),
                appName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USAGE_APP_NAME)),
                packageName = cursor.getString(cursor.getColumnIndexOrThrow(COL_USAGE_PACKAGE_NAME)),
                usageTime = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USAGE_TIME)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USAGE_TIMESTAMP))
            ))
        }
        cursor.close()
        return usageList
    }
    
    // ==================== دوال مساعدة ====================
    
    private fun generateChildCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = StringBuilder()
        repeat(6) {
            code.append(chars.random())
        }
        val generatedCode = code.toString()
        
        // التأكد من عدم تكرار الرمز
        return if (isChildCodeExists(generatedCode)) {
            generateChildCode()
        } else {
            generatedCode
        }
    }
    
    // ==================== data classes ====================
    
    data class Parent(
        val id: Long,
        val email: String,
        val name: String
    )
    
    data class Child(
        val id: Long,
        val parentId: Long,
        val name: String,
        val code: String
    )
    
    data class LocationData(
        val id: Long,
        val childId: Long,
        val latitude: Double,
        val longitude: Double,
        val address: String?,
        val timestamp: Long
    )
    
    data class AppUsageData(
        val id: Long,
        val childId: Long,
        val appName: String,
        val packageName: String,
        val usageTime: Int,
        val timestamp: Long
    )
}
