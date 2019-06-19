package net.ukr.zubenko.g.criminalintent

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.DatePicker
import java.util.*
import android.content.Intent
import android.view.View
import android.view.ViewGroup


class DatePickerFragment: DialogFragment() {
    private lateinit var mDatePicker: DatePicker

    companion object {
        const val ARG_DATE = "date"
        const val EXTRA_DATE = "com.android.criminalintent.date"

        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)

            val fragment = DatePickerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val date = arguments?.getSerializable(ARG_DATE) as? Date ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_date, null)

        mDatePicker = view.findViewById(R.id.dialog_date_picker)
        mDatePicker.init(year, month, day, null)

        return AlertDialog.Builder(context ?: requireContext())
            .setView(view)
            .setTitle(R.string.date_picker_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                sendResult(Activity.RESULT_OK, GregorianCalendar(mDatePicker.year, mDatePicker.month, mDatePicker.dayOfMonth).time)
            }.create()
    }

    private fun sendResult(resultCode: Int, date: Date) {
        targetFragment?.let {
            val intent = Intent()
            intent.putExtra(EXTRA_DATE, date)
            it.onActivityResult(targetRequestCode, resultCode, intent)
        }
    }
}