package com.recio.parkingratecalculator.classes

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COLUMN + " INTEGER PRIMARY KEY, " +
                TYPE_COLUMN + " TEXT," +
                INITIAL_FEE_COLUMN + " INTEGER," +
                ADDITIONAL_FEE_COLUMN + " INTEGER)")

        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addRates(type: String, initialFee: Int, additionalFee: Int){
        val values = ContentValues()
        values.put(TYPE_COLUMN, type)
        values.put(INITIAL_FEE_COLUMN, initialFee)
        values.put(ADDITIONAL_FEE_COLUMN, additionalFee)

        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getRates(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun getCarRates(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $TYPE_COLUMN = 'car'", null)
    }

    fun getMotorcycleRates(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $TYPE_COLUMN = 'motorcycle'", null)
    }

    companion object{
        private const val DATABASE_NAME = "PARKING_DATABASE"
        private val DATABASE_VERSION = 1
        const val TABLE_NAME = "parking_fees"

        const val ID_COLUMN = "id"
        const val TYPE_COLUMN = "type"
        const val INITIAL_FEE_COLUMN = "initial_rate"
        const val ADDITIONAL_FEE_COLUMN = "additional_rate"
    }
}