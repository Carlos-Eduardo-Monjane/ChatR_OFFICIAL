package com.vtrixdigital.chatr.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.databinding.MessageANumberBinding
import com.vtrixdigital.chatr.utils.Constants
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.net.URLEncoder

class MessageANumber : Fragment() {
    private var _binding: MessageANumberBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = MessageANumberBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context, root)
        binding.sendWhatsappNumber.setOnClickListener{

            var number = binding.numberWithCode.text.toString()
            if(number.length in 10..14){
                if(number.length <= 10){
                    number = "$COUNTRY_CODE$number"
                }
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    val url = "https://api.whatsapp.com/send?phone="+number+"&text = " + URLEncoder.encode("Hi", "UTF-8")
                    i.setPackage("com.whatsapp")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(i)
                    }else{
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else{
                Toast.makeText(context , "Invalid No" , Toast.LENGTH_LONG).show()
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
}