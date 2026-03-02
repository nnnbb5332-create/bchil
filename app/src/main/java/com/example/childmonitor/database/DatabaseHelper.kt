package com.example.childmonitor.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "child_monitor.db"
        const val DATABASE_VERSION = 1
        
        // جداول
        const val TABLE_LOCATIONS = "locations"
        const val TABLE_APP_USAGE = "app_usage"
        const val TABLE_CALLS = "calls"
        const val TABLE_SMS = "sms"
        const val TABLE_BATTERY = "battery"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE $TABLE_LOCATIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id TEXT,
                latitude REAL,
                longitude REAL,
                address TEXT,
                timestamp LONG
            )
        """.trimIndent())

        db?.execSQL("""
            CREATE TABLE $TABLE_APP_USAGE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id TEXT,
                app_name TEXT,
                package_name TEXT,
                usage_time INTEGER,
                timestamp LONG
            )
        """.trimIndent())

        db?.execSQL("""
            CREATE TABLE $TABLE_CALLS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id TEXT,
                phone_number TEXT,
                call_type INTEGER,
                duration INTEGER,
                timestamp LONG
            )
        """.trimIndent())

        db?.execSQL("""
            CREATE TABLE $TABLE_SMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id TEXT,
                phone_number TEXT,
                message TEXT,
                timestamp LONG
            )
        """.trimIndent())

        db?.execSQL("""
            CREATE TABLE $TABLE_BATTERY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                child_id TEXT,
                level INTEGER,
                is_charging INTEGER,
                timestamp LONG
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_APP_USAGE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CALLS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SMS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BATTERY")
        onCreate(db)
    }

    fun insertLocation(childId: String, latitude: Double, longitude: Double, address: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("child_id", childId)
            put("latitude", latitude)
            put("longitude", longitude)
            put("address", address)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert(TABLE_LOCATIONS, null, values)
    }

    fun insertAppUsage(childId: String, appName: String, packageName: String, usageTime: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("child_id", childId)
            put("app_name", appName)
            put("package_name", packageName)
            put("usage_time", usageTime)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert(TABLE_APP_USAGE, null, values)
    }
}
