package net.ukr.zubenko.g.criminalintent

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Crime(val mTitle: String,
            val mSolved: Boolean,
            val mRequiresPolice: Boolean = false,
            val mId: UUID = UUID.randomUUID(),
            val mDate: Date = Date(),
            val mSuspect: String? = null,
            val mContact: String? = null)
{
    val strDateTime: String
        get() = DateFormat.format("dd.MM.yyyy  HH:mm", mDate).toString()
    val strDate: String
        get() = DateFormat.format("dd.MM.yyyy", mDate).toString()
    val strTime: String
        get() = DateFormat.format("HH:mm", mDate).toString()

    val photoFilename: String
        get() = "IMG_$mId.jpg"

    fun copy(title: String = mTitle,
             solved: Boolean = mSolved,
             requiresPolice: Boolean = mRequiresPolice,
             id: UUID = mId,
             date: Date = mDate,
             suspect: String? = mSuspect,
             contact: String? = mContact): Crime
    {
        val crime = Crime(title, solved, requiresPolice, id, date, suspect, contact)
        CrimeLab.updateCrime(crime)

        return crime
    }
}
