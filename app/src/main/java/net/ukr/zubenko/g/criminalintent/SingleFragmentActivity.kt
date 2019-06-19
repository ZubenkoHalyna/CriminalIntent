package net.ukr.zubenko.g.criminalintent

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.annotation.LayoutRes



abstract class SingleFragmentActivity : AppCompatActivity() {
    abstract fun createFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, createFragment())
                .commit()
    }

    @LayoutRes
    protected open fun getLayoutResId(): Int {
        return R.layout.activity_fragment
    }
}