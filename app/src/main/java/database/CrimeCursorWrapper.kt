package database

import android.database.Cursor
import android.database.CursorWrapper
import database.CrimeDbSchema.CrimeTable
import net.ukr.zubenko.g.criminalintent.Crime
import java.util.*




class CrimeCursorWrapper(cursor: Cursor) : CursorWrapper(cursor)
{
    fun getCrime(): Crime? {
        val uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID))
        val title = getString(getColumnIndex(CrimeTable.Cols.TITLE))
        val date = getLong(getColumnIndex(CrimeTable.Cols.DATE))
        val isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED))
        val police = getInt(getColumnIndex(CrimeTable.Cols.POLICE))
        val suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT))
        val contact = getString(getColumnIndex(CrimeTable.Cols.CONTACT))

        return Crime(title, isSolved != 0,police != 0, UUID.fromString(uuidString),
            Date(date), suspect, contact)
    }
}