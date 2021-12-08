package com.vtrixdigital.chatr.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.ui.fragments.dashboard.DashboardFragment
import com.vtrixdigital.chatr.ui.fragments.tab_contacts.SendViaContactsFragment
import com.vtrixdigital.chatr.ui.fragments.tab_csv.SendViaCSVFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_dashboard,
    R.string.tab_csv,
    R.string.tab_contacts
)

class BulkMessagingTabAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm , BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DashboardFragment()
            1 -> SendViaCSVFragment()
            2 -> SendViaContactsFragment()
            else -> DashboardFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 4 total pages.
        return 3
    }
}