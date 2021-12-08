package com.vtrixdigital.chatr.ui.fragments.sent

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.adapters.SentMessageAdapter
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.FragmentSentBinding
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class SentFragment : Fragment() {
    private var _binding: FragmentSentBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: SentMessageViewModel
    private val recordListAdapter = SentMessageAdapter(arrayListOf())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSentBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context , root)
        viewModel = ViewModelProvider(this).get(SentMessageViewModel::class.java)
        val db = context?.let {
            DatabaseHelper().getInstance(it)
        }
        viewModel.refresh(db)

        binding.usersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordListAdapter
        }

        observeViewModel()

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterRecords(s.toString())
            }
        })
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
        viewModel.records?.observe(viewLifecycleOwner, { users ->
            users?.let {
                recordListAdapter.updateRecords(it)
                if(users.isNotEmpty()){
                    binding.animationView.visibility = View.GONE
                    binding.usersList.visibility = View.VISIBLE
                }else{
                    binding.animationView.visibility = View.VISIBLE
                    binding.usersList.visibility = View.GONE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}