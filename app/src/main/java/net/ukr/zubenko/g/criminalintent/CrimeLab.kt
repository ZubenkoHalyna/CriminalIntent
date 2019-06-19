package net.ukr.zubenko.g.criminalintent

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import database.CrimeBaseHelper
import java.util.*
import database.CrimeDbSchema.CrimeTable
import android.content.ContentValues
import android.database.Cursor
import database.CrimeCursorWrapper
import java.io.File


@SuppressLint("StaticFieldLeak")
object CrimeLab {
    private lateinit var mDatabase: SQLiteDatabase
    private lateinit var mContext: Context

    val mCrimes: List<Crime>
    get() {
        val crimes = mutableListOf<Crime>()
        val cursor = queryCrimes(null, null)
        cursor.use { cursor ->
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                cursor.getCrime()?.let {
                    crimes.add(it)
                }
                cursor.moveToNext()
            }
        }
        return crimes
    }

    fun init(context: Context) {
        mDatabase = CrimeBaseHelper(context).writableDatabase
        mContext = context
    }

    fun getCrime(id: UUID): Crime?
    {
        val cursor = queryCrimes(
            CrimeTable.Cols.UUID + " = ?",
            arrayOf(id.toString())
        )
        cursor.use { cursor ->
            if (cursor.count == 0) {
                return null
            }
            cursor.moveToFirst()
            return cursor.getCrime()
        }
    }

    fun indexOf(id: UUID) = mCrimes.indexOfFirst { it.mId == id }

    fun addCrime(c: Crime) {
        mDatabase.insert(CrimeTable.NAME, null, getContentValues(c))
    }

    fun remove(crime: Crime)
    {
        val uuidString = crime.mId.toString()
        mDatabase.delete(
            CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?", arrayOf(uuidString)
        )
    }

    fun updateCrime(crime: Crime) {
        val uuidString = crime.mId.toString()
        val values = getContentValues(crime)
        mDatabase.update(
            CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", arrayOf(uuidString)
        )
    }

    private fun getContentValues(crime: Crime): ContentValues {
        val values = ContentValues()
        values.put(CrimeTable.Cols.UUID, crime.mId.toString())
        values.put(CrimeTable.Cols.TITLE, crime.mTitle)
        values.put(CrimeTable.Cols.DATE, crime.mDate.time)
        values.put(CrimeTable.Cols.SOLVED, if (crime.mSolved) 1 else 0)
        values.put(CrimeTable.Cols.POLICE, if (crime.mRequiresPolice) 1 else 0)
        values.put(CrimeTable.Cols.SUSPECT, crime.mSuspect)
        values.put(CrimeTable.Cols.CONTACT, crime.mContact)
        return values
    }

    private fun queryCrimes(whereClause: String?, whereArgs: Array<String>?): CrimeCursorWrapper {
        return CrimeCursorWrapper(mDatabase.query(
            CrimeTable.NAME, null, whereClause,  whereArgs, null, null, null))
    }

    fun getPhotoFile(crime: Crime): File {
        return File(mContext.filesDir, crime.photoFilename)
    }

    fun getPhotoFile(fileName: String): File {
        return File(mContext.filesDir, fileName)
    }
}