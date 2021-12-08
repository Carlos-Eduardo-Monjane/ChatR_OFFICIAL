package com.vtrixdigital.chatr.ui.fragments.home

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.adapters.ReplyMessageAdapter
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.database.Rules
import com.vtrixdigital.chatr.databinding.FragmentHomeBinding
import com.vtrixdigital.chatr.databinding.FragmentMainBinding
import com.vtrixdigital.chatr.notifyUtils.NotificationService
import com.vtrixdigital.chatr.ui.activities.ManageRuleActivity
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class HomeFragment : Fragment() {

    private var _binding1: FragmentHomeBinding? = null
    private val binding1 get() = _binding1!!

    private var _binding2: FragmentMainBinding? = null
    private val binding2 get() = _binding2!!

    private lateinit var root: View
    private lateinit var ruleList: List<Rules>
    private var ishome = false
    private lateinit var notificationService: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationService = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (isNotificationServiceRunning()) {
                    val navController = Navigation.findNavController(root)
                    val id = navController.currentDestination?.id
                    navController.popBackStack(id!!, true)
                    navController.navigate(id)
                }else{
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error while turning on notification service", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (isNotificationServiceRunning()) {
            ishome = true
            _binding2 = FragmentMainBinding.inflate(inflater, container, false)
            root = binding2.root

            setupAdmobAds(context, root)

            PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false)
            binding2.homeFragmentFab.setOnClickListener {
                val intent = Intent(context, ManageRuleActivity::class.java)
                startActivity(intent)
            }
            binding2.recyclerview.layoutManager = LinearLayoutManager(context)
            initiateAdapter(context)
        } else {
            _binding1 = FragmentHomeBinding.inflate(inflater, container, false)
            root = binding1.root
            setupAdmobAds(context, root)

            val button2: Button = binding1.button2
            val intent = Intent(activity, NotificationService::class.java)
            try {
                requireActivity().startService(intent) //Start service
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            button2.setOnClickListener {
                notificationService.launch(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
        return root
    }

    private fun setupAdmobAds(context: Context?, view: View) {
        context?.let {
            val mAdView = view.findViewById<AdView>(R.id.adView)
            if (Constants().showAds()) {
                MobileAds.initialize(it) {}
                val adRequest = AdRequest.Builder().build()
                mAdView.loadAd(adRequest)
            } else {
                mAdView.visibility = View.GONE
            }
        }
    }

    private fun initiateAdapter(context: Context?) {
        GlobalScope.launch {
            val db = context?.let {
                DatabaseHelper().getInstance(it)
            }
            ruleList = db?.rulesDao()?.getAllRules()!!
            Handler(Looper.getMainLooper()).post {
                if (ruleList.isNotEmpty()) {
                    binding2.animationView.visibility = View.GONE
                    binding2.recyclerview.adapter = ReplyMessageAdapter(ruleList, context)
                } else {
                    binding2.animationView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun isNotificationServiceRunning(): Boolean {
        val contentResolver: ContentResolver = requireActivity().contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName: String = requireActivity().packageName
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    companion object
}