package net.ukr.zubenko.g.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Context
import android.provider.ContactsContract
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import java.util.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ShareCompat.IntentBuilder
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import java.io.File
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.provider.MediaStore
import android.support.v4.content.FileProvider


class CrimeFragment: Fragment() {
    private var mCrime: Crime = Crime("", false)
    private lateinit var mTitleField: EditText
    private lateinit var mDateButton: Button
    private lateinit var mTimeButton: Button
    private lateinit var mSolvedCheckBox: CheckBox
    private lateinit var mPoliceRequiredCheckBox: CheckBox
    private lateinit var mReportButton: Button
    private lateinit var mSuspectButton: Button
    private lateinit var mMakeCallButton: ImageButton
    private lateinit var mPhotoButton: ImageButton
    private lateinit var mPhotoView: ImageView
    private lateinit var mPhotoFile: File
    private var mCallbacks: Callbacks? = null

    interface Callbacks {
        fun onCrimesUpdated()
        fun onDeleteCrime()
    }

    companion object {
        const val ARG_CRIME_ID = "crime_id"
        const val DIALOG_DATE = "DialogDate"
        const val DIALOG_PHOTO = "DialogPhoto"
        const val DIALOG_TIME = "DialogTime"
        const val PHOTO_DIR = "net.ukr.zubenko.g.criminalintent.fileprovider"
        const val REQUEST_DATE = 0
        const val REQUEST_TIME = 1
        const val REQUEST_CONTACT = 2
        const val REQUEST_PHOTO = 3
        const val SHOW_PHOTO = 4

        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle()
            args.putSerializable(ARG_CRIME_ID, crimeId)

            val fragment = CrimeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallbacks = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val crimeId = arguments?.get(ARG_CRIME_ID) as? UUID
        crimeId?.let {
            CrimeLab.getCrime(crimeId)?.let {
                mCrime = it
            }
        }
        mPhotoFile = CrimeLab.getPhotoFile(mCrime)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_crime -> {
                CrimeLab.remove(mCrime)
                mCallbacks?.onDeleteCrime()
                mCallbacks?.onCrimesUpdated()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        mDateButton = view.findViewById(R.id.crime_date)
        mDateButton.text = mCrime.strDate
        mDateButton.setOnClickListener {
            val dialog = DatePickerFragment.newInstance(mCrime.mDate)
            dialog.setTargetFragment(this, REQUEST_DATE)
            dialog.show(fragmentManager, DIALOG_DATE)
        }

        mTimeButton = view.findViewById(R.id.crime_time)
        mTimeButton.text = mCrime.strTime
        mTimeButton.setOnClickListener {
            val dialog = TimePickerFragment.newInstance(mCrime.mDate)
            dialog.setTargetFragment(this, REQUEST_TIME)
            dialog.show(fragmentManager, DIALOG_TIME)
        }

        mSolvedCheckBox = view.findViewById(R.id.crime_solved)
        mSolvedCheckBox.isChecked = mCrime.mSolved
        mSolvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            mCrime = mCrime.copy(solved = isChecked)
            mCallbacks?.onCrimesUpdated()
        }

        mPoliceRequiredCheckBox = view.findViewById(R.id.crime_police_required)
        mPoliceRequiredCheckBox.isChecked = mCrime.mRequiresPolice
        mPoliceRequiredCheckBox.setOnCheckedChangeListener { _, isChecked ->
            mCrime = mCrime.copy(requiresPolice = isChecked)
            mCallbacks?.onCrimesUpdated()
        }

        mTitleField = view.findViewById(R.id.crime_title)
        mTitleField.setText(mCrime.mTitle)
        mTitleField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Здесь намеренно оставлено пустое место
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mCrime = mCrime.copy(title = s.toString())
                mCallbacks?.onCrimesUpdated()
            }

            override fun afterTextChanged(s: Editable) {
                // И здесь тоже
            }
        })

        mReportButton = view.findViewById(R.id.crime_report)
        mReportButton.setOnClickListener {
            val shareIntent = IntentBuilder.from(requireActivity())
            shareIntent.setText(getCrimeReport())
            shareIntent.setSubject(getString(R.string.crime_report_subject))
            shareIntent.setType("text/plain")
            shareIntent.setChooserTitle(getString(R.string.send_report))
            startActivity(shareIntent.intent)
        }

        mSuspectButton = view.findViewById(R.id.crime_suspect)
        mSuspectButton.setOnClickListener {
                requestContact()
        }
        mCrime.mSuspect?.let {
            mSuspectButton.text = it
        }
        if (! isContactsActivity()) {
            mSuspectButton.isEnabled = false
        }

        mMakeCallButton = view.findViewById(R.id.make_call)
        mCrime.mContact?.let {phoneNumber ->
            mMakeCallButton.isEnabled = true
            mMakeCallButton.setOnClickListener { makeCall(phoneNumber) }
        }
        if (mCrime.mContact == null)
            mMakeCallButton.isEnabled = false

        mPhotoButton = view.findViewById(R.id.crime_camera)
        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val canTakePhoto = captureImage.resolveActivity(requireActivity().packageManager) != null
        mPhotoButton.isEnabled = canTakePhoto
        mPhotoButton.setOnClickListener {
            val uri = FileProvider.getUriForFile(
                requireActivity(),
                PHOTO_DIR,
                mPhotoFile
            )
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            val cameraActivities = requireActivity().packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            for (activity in cameraActivities) {
                requireActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivityForResult(captureImage, REQUEST_PHOTO)
        }

        mPhotoView = view.findViewById(R.id.crime_photo)
        mPhotoView.setOnClickListener {
            if (CrimeLab.getPhotoFile(mCrime).exists()) {
                val dialog = PhotoFragment.newInstance(mCrime)
                dialog.setTargetFragment(this, SHOW_PHOTO)
                dialog.show(fragmentManager, DIALOG_PHOTO)
            }
        }
        updatePhotoView()

        return view
    }

    private fun requestContact() = runWithPermissions(Manifest.permission.READ_CONTACTS) {
            val pickContact = Intent(
                Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI
            )
           startActivityForResult(pickContact, REQUEST_CONTACT)
    }

    private fun isContactsActivity(): Boolean {
        val pickContact = Intent(
            Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI
        )
        return requireActivity().packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    private fun makeCall(phoneNumber: String) {
        val number = Uri.parse("tel:$phoneNumber")
        val callIntent = Intent(Intent.ACTION_DIAL, number)
        requireContext().startActivity(callIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return

        when(requestCode) {
            REQUEST_DATE -> {
                (data?.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as? Date)?.let {
                    mCrime = mCrime.copy(date = combineDateTime(it, mCrime.mDate))
                    mCallbacks?.onCrimesUpdated()
                }
                mDateButton.text = mCrime.strDate
            }

            REQUEST_TIME -> {
                (data?.getSerializableExtra(TimePickerFragment.EXTRA_HOUR) as? Int)?.let { hour ->
                    (data.getSerializableExtra(TimePickerFragment.EXTRA_MINUTE) as? Int)?.let { minute ->
                        mCrime = mCrime.copy(date = combineDateTime(mCrime.mDate, hour, minute))
                        mCallbacks?.onCrimesUpdated()
                    }
                }
                mTimeButton.text = mCrime.strTime
            }

            REQUEST_CONTACT ->
                if (data != null) {

                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
                    data.data?.let {
                        val c = requireActivity().contentResolver.query(it, queryFields, null, null, null)
                        c.use {
                            c?.let {
                                if (c.count != 0) {
                                    c.moveToFirst()
                                    val suspect = c.getString(0)

                                    var phoneNumber: String? = null
                                    val id = c.getString(1)

                                    val phones = requireActivity().contentResolver.query(
                                        Phone.CONTENT_URI,
                                        null,
                                        Phone.CONTACT_ID + " = " + id,
                                        null,
                                        null
                                    )
                                    phones?.let {
                                        phones.moveToFirst()
                                        phoneNumber = phones.getString(phones.getColumnIndex(Phone.NUMBER))
                                        phoneNumber?.let { pn ->
                                            mMakeCallButton.setOnClickListener {
                                                makeCall(pn)
                                            }
                                            mMakeCallButton.isEnabled = true
                                        }
                                        phones.close()
                                    }
                                    mCrime.copy(suspect = suspect, contact = phoneNumber)
                                    mSuspectButton.text = suspect
                                    mCallbacks?.onCrimesUpdated()
                                }
                            }
                        }
                    }
                }
            REQUEST_PHOTO -> {
                val uri = FileProvider.getUriForFile(requireActivity(), PHOTO_DIR, mPhotoFile)
                requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    private fun combineDateTime(date: Date, time: Date): Date
    {
        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = time

        return combineDateTime(date,
            timeCalendar.get(Calendar.HOUR_OF_DAY),
            timeCalendar.get(Calendar.MINUTE))
    }

    private fun combineDateTime(date: Date, hour: Int, minute: Int): Date
    {
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date

        return GregorianCalendar(
            dateCalendar.get(Calendar.YEAR),
            dateCalendar.get(Calendar.MONTH),
            dateCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            minute).time
    }

    private fun getCrimeReport(): String {
        val solvedString =
            if (mCrime.mSolved) {
                getString(R.string.crime_report_solved)
            } else {
                getString(R.string.crime_report_unsolved)
            }
        val suspect =
            if (mCrime.mSuspect == null) {
                getString(R.string.crime_report_no_suspect)
            } else {
                getString(R.string.crime_report_suspect, mCrime.mSuspect)
            }
        return getString(
            R.string.crime_report,
            mCrime.mTitle, mCrime.strDateTime, solvedString, suspect
        )
    }

    private fun updatePhotoView() {
        if (!mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null)
            mPhotoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        } else {
            val bitmap = PictureUtils.getScaledBitmap(mPhotoFile.path, requireActivity())
            mPhotoView.setImageBitmap(bitmap)
            mPhotoView.contentDescription = getString(R.string.crime_photo_image_description)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }
}