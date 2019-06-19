package net.ukr.zubenko.g.criminalintent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import java.util.*

class CrimePagerActivity: AppCompatActivity(), CrimeFragment.Callbacks {
    private lateinit var mViewPager: ViewPager

    companion object {
        const val EXTRA_CRIME_ID = "android.criminalintent.crime_id"

        fun newIntent(packageContext: Context, crimeId: UUID): Intent {
            val intent = Intent(packageContext, CrimePagerActivity::class.java)
            intent.putExtra(EXTRA_CRIME_ID, crimeId)
            return intent
        }
    }

    override fun onCrimesUpdated() {
    }

    override fun onDeleteCrime() {
        onBackPressed()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime_pager)

        val crimeId = intent.getSerializableExtra(EXTRA_CRIME_ID) as? UUID

        mViewPager = findViewById(R.id.crime_view_pager)
        mViewPager.adapter = object: FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int) = CrimeFragment.newInstance(CrimeLab.mCrimes[position].mId)

            override fun getCount() = CrimeLab.mCrimes.size
        }

        crimeId?.let {
            mViewPager.currentItem = CrimeLab.indexOf(crimeId)
        }


        val mLastButton = findViewById<Button>(R.id.go_to_last_element_button)
        mLastButton.setOnClickListener {
            mViewPager.currentItem = CrimeLab.mCrimes.size - 1
        }

        val mFirstButton = findViewById<Button>(R.id.go_to_first_element_button)
        mFirstButton.setOnClickListener {
            mViewPager.currentItem = 0
        }
    }
}