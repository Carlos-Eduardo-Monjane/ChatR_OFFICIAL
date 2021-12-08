package com.vtrixdigital.chatr.ui.fragments.rate

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.databinding.FragmentRateusBinding
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class RateFragment : Fragment() {
    private var _binding: FragmentRateusBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRateusBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context , root)
        binding.rateStars.setOnClickListener{
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + requireActivity().packageName)
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().packageName)
                    )
                )
            }
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}