package com.vtrixdigital.chatr.ui.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.adapters.BulkMessagingTabAdapter
import com.vtrixdigital.chatr.databinding.ActivityBulkMessagingBinding

class BulkMessagingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBulkMessagingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBulkMessagingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bulkMessagingTabAdapter = BulkMessagingTabAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = bulkMessagingTabAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.toolbarText.text = getString(R.string.bulk_messaging)
                    }
                    1 -> {
                        binding.toolbarText.text = getString(R.string.send_via_csv)
                    }
                    2 -> {
                        binding.toolbarText.text = getString(R.string.send_from_contacts)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}