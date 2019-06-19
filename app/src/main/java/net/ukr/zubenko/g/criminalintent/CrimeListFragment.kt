package net.ukr.zubenko.g.criminalintent

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.content.Intent
import android.view.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import org.jetbrains.anko.find


class CrimeListFragment: Fragment() {
    private open inner class CrimeHolder(inflater: LayoutInflater, parent: ViewGroup,
                                         type: Int = R.layout.list_item_crime) :
        RecyclerView.ViewHolder(inflater.inflate(type, parent, false))
    {
        private val mTitleTextView = itemView.findViewById<TextView>(R.id.crime_title)
        private val mDateTextView = itemView.findViewById<TextView>(R.id.crime_date)
        private val mSolvedImageView = itemView.findViewById<ImageView>(R.id.crime_solved)

        protected lateinit var mCrime: Crime

        init {
            itemView.setOnClickListener(this::onClick)
        }

        open fun bind(crime: Crime) {
            mCrime = crime
            mTitleTextView.text = mCrime.mTitle
            mDateTextView.text = mCrime.strDateTime
            mSolvedImageView.visibility = if (mCrime.mSolved) View.VISIBLE else View.GONE
        }

        fun onClick(view: View) {
            mCallbacks?.onCrimeSelected(mCrime)
        }

        fun deleteCrime() {
            CrimeLab.remove(mCrime)
        }
    }

    private inner class HardCrimeHolder(inflater: LayoutInflater, parent: ViewGroup) :
        CrimeHolder(inflater, parent, R.layout.list_item_hard_crime)
    {
        private val mCallPoliceButton = itemView.findViewById<Button>(R.id.call_police_button)
         override fun bind(crime: Crime) {
            super.bind(crime)
             mCrime = crime

             mCallPoliceButton.visibility = if (mCrime.mSolved) View.GONE else View.VISIBLE
        }
    }

    private inner class CrimeAdapter(var mCrimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return if (viewType == 0)
                       CrimeHolder(layoutInflater, parent)
                   else HardCrimeHolder(layoutInflater, parent)
        }

        override fun getItemViewType(position: Int): Int {
            return if (mCrimes[position].mRequiresPolice) 1 else 0
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = mCrimes[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int {
            return mCrimes.size
        }
    }

    companion object {
        private const val REQUEST_CRIME = 1
        private const val SAVED_SUBTITLE_VISIBLE = "subtitle"
    }

    private lateinit var mCrimeRecyclerView: RecyclerView
    private lateinit var mAdapter: CrimeAdapter
    private lateinit var mNoCrimesText: TextView
    private var mSubtitleVisible: Boolean = false
    private var mCallbacks: Callbacks? = null

    interface Callbacks {
        fun onCrimeSelected(crime: Crime)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallbacks = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(
            R.layout.fragment_crime_list, container,
            false
        )
        mCrimeRecyclerView = view
            .findViewById(R.id.crime_recycler_view) as RecyclerView
        mCrimeRecyclerView.layoutManager = LinearLayoutManager(activity)


        val simpleItemTouchCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT ) {
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                (viewHolder as? CrimeHolder)?.deleteCrime()
                updateUI()
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView)

        savedInstanceState?.let {
            mSubtitleVisible = it.getBoolean(SAVED_SUBTITLE_VISIBLE)
        }

        mNoCrimesText = view.findViewById(R.id.no_crimes_text)

        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible)
    }

    public fun updateUI() {
        if (this::mAdapter.isInitialized) {
            mAdapter.mCrimes = CrimeLab.mCrimes
            mAdapter.notifyDataSetChanged()
        }
        else
        {
            mAdapter = CrimeAdapter(CrimeLab.mCrimes)
            mCrimeRecyclerView.adapter = mAdapter
        }

        mNoCrimesText.visibility = if (CrimeLab.mCrimes.size == 0) View.VISIBLE else View.INVISIBLE

        updateSubtitle()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)

        val subtitleItem = menu.findItem(R.id.show_subtitle)
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle)
        } else {
            subtitleItem.setTitle(R.string.show_subtitle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime("", false)
                CrimeLab.addCrime(crime)
                updateUI()
                mCallbacks?.onCrimeSelected(crime)
                true
            }
            R.id.show_subtitle -> {
                mSubtitleVisible = !mSubtitleVisible;
                requireActivity().invalidateOptionsMenu()
                updateSubtitle()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateSubtitle() {
        val crimeCount = CrimeLab.mCrimes.size
        var subtitle: String? = resources.getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount)

        if (!mSubtitleVisible) {
            subtitle = null
        }

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.subtitle = subtitle
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }
}