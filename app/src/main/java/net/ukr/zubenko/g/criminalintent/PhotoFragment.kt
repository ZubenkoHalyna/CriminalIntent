package net.ukr.zubenko.g.criminalintent

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import java.io.File
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import java.util.concurrent.locks.Condition


class PhotoFragment: DialogFragment() {
    private lateinit var mPhotoView: ImageView
    private lateinit var mPhotoFile: File

    companion object {
        const val ARG_FILE_NAME = "fileName"

        fun newInstance(crime: Crime): PhotoFragment {
            val args = Bundle()
            args.putString(ARG_FILE_NAME, crime.photoFilename)

            val fragment = PhotoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_photo, null)
        val photoFileName = arguments?.getString(ARG_FILE_NAME)

        photoFileName?.let {
            mPhotoFile = CrimeLab.getPhotoFile(photoFileName)
            mPhotoView = view.findViewById(R.id.crime_photo)
        }

        var counter = 0

        mPhotoView.viewTreeObserver.addOnGlobalLayoutListener {
            if (mPhotoView.width != 0 && counter == 0) {
                counter++
                updatePhotoView(mPhotoView.width, mPhotoView.height)
            }
        }
        updatePhotoView()

        return AlertDialog.Builder(requireContext())
            .setView(view)
             .setPositiveButton(android.R.string.ok) { _, _ -> }
            .create()
    }

    private fun updatePhotoView() =
        updatePhotoView { PictureUtils.getScaledBitmap(mPhotoFile.path, requireActivity()) }

    private fun updatePhotoView(x: Int, y: Int) =
        updatePhotoView { PictureUtils.getScaledBitmap(mPhotoFile.path, x, y) }

    private fun updatePhotoView(getBitmap: () -> Bitmap) {
        if (!mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null)
        } else {
            val bitmap = getBitmap()
            mPhotoView.setImageBitmap(bitmap)
            dialog?.window?.setLayout(bitmap.width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}