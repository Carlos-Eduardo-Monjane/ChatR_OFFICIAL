package com.vtrixdigital.chatr.ui.fragments.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.DashboardFragmentBinding
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class DashboardFragment : Fragment() {
    private var _binding: DashboardFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private var db: AppDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context, root)
        viewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        db = context?.let {
            DatabaseHelper().getInstance(it)
        }
        viewModel.refresh(db , getString(R.string.csv_source) , getString(R.string.contacts_source))
        observeViewModel()
        binding.settings.setOnClickListener{
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        return root
    }

    private fun setupAdmobAds(context: Context?, view: View) {
        context?.let {
            val mAdView = view.findViewById<AdView>(R.id.adView)
            if(Constants().showAds()) {
                MobileAds.initialize(it) {}
                val adRequest = AdRequest.Builder().build()
                mAdView.loadAd(adRequest)
            }else{
                mAdView.visibility = View.GONE
            }
        }
    }

    private fun observeViewModel() {
        viewModel.csvImported.observe(viewLifecycleOwner , {
            if(it != null)
                binding.csvImported.text = it.toString()
        })
        viewModel.contactsImported.observe(viewLifecycleOwner , {
            if(it != null)
                binding.contactsImported.text = it.toString()
        })
        viewModel.sent.observe(viewLifecycleOwner , {
            if(it != null)
                binding.sent.text = it.toString()
        })
        viewModel.received.observe(viewLifecycleOwner , {
            if(it != null)
                binding.received.text = it.toString()
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh(db , getString(R.string.csv_source) , getString(R.string.contacts_source))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}