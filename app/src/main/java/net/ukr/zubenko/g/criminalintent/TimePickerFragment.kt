package net.ukr.zubenko.g.criminalintent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TimePicker
import android.widget.Toast
import java.util.*

class TimePickerFragment: DialogFragment() {
    private lateinit var mTimePicker: TimePicker

    companion object {
        const val ARG_TIME = "time"
        const val EXTRA_HOUR = "com.android.criminalintent.hour"
        const val EXTRA_MINUTE = "com.android.criminalintent.minute"

        fun newInstance(time: Date): TimePickerFragment {
            val args = Bundle()
            args.putSerializable(ARG_TIME, time)

            val fragment = TimePickerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val time = arguments?.getSerializable(ARG_TIME) as? Date ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = time

        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_time, null)

        mTimePicker = view.findViewById(R.id.dialog_time_picker)
        mTimePicker.setIs24HourView(true)
        mTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        mTimePicker.minute = calendar.get(Calendar.MINUTE)

        return AlertDialog.Builder(context ?: requireContext())
            .setView(view)
            .setTitle(R.string.time_picker_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                sendResult(Activity.RESULT_OK, mTimePicker.hour, mTimePicker.minute)
            }.create()
    }

    private fun sendResult(resultCode: Int, hour: Int, minute: Int) {
        targetFragment?.let {
            val intent = Intent()
            intent.putExtra(EXTRA_HOUR, hour)
            intent.putExtra(EXTRA_MINUTE, minute)
            it.onActivityResult(targetRequestCode, resultCode, intent)
        }
    }
}