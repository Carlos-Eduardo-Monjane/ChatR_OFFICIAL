package com.vtrixdigital.chatr.ui.fragments.policy

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.databinding.FragmentPolicyBinding
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class PolicyFragment : Fragment() {
    private var _binding: FragmentPolicyBinding? = null
    private val binding get() = _binding!!
    private lateinit var webView: WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPolicyBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context , root)
        webView = binding.policyCondition
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.overScrollMode = WebView.OVER_SCROLL_NEVER
        webView.loadUrl("file:///android_asset/privacy.html")
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
}