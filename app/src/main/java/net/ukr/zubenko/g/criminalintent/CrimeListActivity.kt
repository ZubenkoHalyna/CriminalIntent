package net.ukr.zubenko.g.criminalintent

import android.view.View


class CrimeListActivity: SingleFragmentActivity(), CrimeListFragment.Callbacks, CrimeFragment.Callbacks {
    override fun createFragment(): CrimeListFragment {
        CrimeLab.init(applicationContext)
        return CrimeListFragment()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_masterdetail
    }

    override fun onCrimeSelected(crime: Crime) {
        if (findViewById<View>(R.id.detail_fragment_container) == null) {
            val intent = CrimePagerActivity.newIntent(this, crime.mId)
            startActivity(intent)
        } else {
            val newDetail = CrimeFragment.newInstance(crime.mId)
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_fragment_container, newDetail)
                .commit()
        }
    }

    override fun onCrimesUpdated() {
        val listFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? CrimeListFragment
        listFragment?.updateUI()
    }

    override fun onDeleteCrime() {
        val crimeFragment = supportFragmentManager.findFragmentById(R.id.detail_fragment_container)
        crimeFragment?.let {
            supportFragmentManager.beginTransaction()
                .detach(crimeFragment)
                .commit()
        }
    }
}