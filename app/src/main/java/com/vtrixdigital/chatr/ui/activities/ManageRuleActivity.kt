package com.vtrixdigital.chatr.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.BottomSheet
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.api.APIService
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.database.Rules
import com.vtrixdigital.chatr.databinding.ManageRulesLayoutBinding
import com.vtrixdigital.chatr.ui.bottom_sheets.BottomSheetChipInfo
import com.vtrixdigital.chatr.ui.bottom_sheets.BottomSheetExactMatch
import com.vtrixdigital.chatr.ui.bottom_sheets.BottomSheetSimilarMatch
import com.vtrixdigital.chatr.utils.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit

class ManageRuleActivity : AppCompatActivity() {
    private lateinit var binding: ManageRulesLayoutBinding
    private var isUpdating = false
    private var messageId = 0
    private var similarMatch = "false"
    private var tag = "TestActivity"
    private var bottomSheet1: BaseBottomSheet? = null
    private var bottomSheet2: BaseBottomSheet? = null
    private var bottomSheet3: BaseBottomSheet? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ManageRulesLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupAdmobAds(this, view)
        isUpdating = intent.getBooleanExtra("isUpdating", false)

        if (intent != null && isUpdating) {
            binding.deleteButton.visibility = View.VISIBLE
            messageId = intent.getIntExtra("ruleId" , 0)
        }
        initDb()
        initOnClicks()
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

    private fun initDb() {
        GlobalScope.launch {
            val db = DatabaseHelper().getInstance(this@ManageRuleActivity)
            if(isUpdating && messageId > 0){
                val rule : Rules = db.rulesDao().getRuleById(messageId)
                updateUi(rule)
            }
        }
    }

    private fun initOnClicks() {
        binding.doneButton.setOnClickListener {
            validateAndSaveRule()
        }
        binding.deleteButton.setOnClickListener {
            deleteRule()
        }
        binding.radiaId1.setOnClickListener {
            similarMatch = "false"
            bottomSheet1 = BottomSheetExactMatch(this).also(BottomSheet::show)
        }
        binding.radiaId2.setOnClickListener{
            similarMatch = "true"
            bottomSheet2 = BottomSheetSimilarMatch(this).also(BottomSheet::show)
        }
        binding.chipInfo.setOnClickListener {
            bottomSheet3 = BottomSheetChipInfo(this).also(BottomSheet::show)
        }
        binding.chipFname.setOnClickListener {
            binding.messageReply.setText(
                binding.messageReply.text.toString().plus(resources.getString(R.string.fname_tag))
            )
        }
        binding.chipLname.setOnClickListener {
            binding.messageReply.setText(
                binding.messageReply.text.toString().plus(resources.getString(R.string.lname_tag))
            )
        }
        binding.chipDate.setOnClickListener {
            binding.messageReply.setText(
                binding.messageReply.text.toString().plus(resources.getString(R.string.date_tag))
            )
        }
        binding.chipTime.setOnClickListener {
            binding.messageReply.setText(
                binding.messageReply.text.toString().plus(resources.getString(R.string.time_tag))
            )
        }
        binding.messageReceived.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.expectedMsgTxt.text = s?.toString()
            }

        })
        binding.messageReply.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.replyMsgTxt.text = s?.toString()
            }

        })
        binding.serverReplySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupAdmobIntAds()
                binding.serverReplyLayout.visibility = View.VISIBLE
            } else {
                binding.serverReplyLayout.visibility = View.GONE
            }
        }

        binding.testServerRules.setOnClickListener {
            val serverUrl = binding.serverUrl.text.toString()
            val headerName = binding.headerName.text.toString()
            val headerValue = binding.headerValue.text.toString()
            if (serverUrl != "") {
                if(!URLUtil.isValidUrl(serverUrl)){
                    showSnackBar("Please enter a valid server URL")
                }else {
                    if (headerName == "" && headerValue == "")
                        testServerRulesWithOutHeaders(serverUrl)
                    else
                        testServerRulesWithHeaders(serverUrl, headerName, headerValue)
                }
            }
            else
                Toast.makeText(
                    this@ManageRuleActivity,
                    "Please enter a valid URL",
                    Toast.LENGTH_LONG
                ).show()
        }
    }

    private fun setupAdmobIntAds() {
        if(Constants().showAds()){
            Handler(Looper.getMainLooper()).post {
                val adRequest = AdRequest.Builder().build()
                var mInterstitialAd: InterstitialAd?
                InterstitialAd.load(
                    this,
                    getString(R.string.admob_interstitial_ad_id),
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            mInterstitialAd = null
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            mInterstitialAd = interstitialAd
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show(this@ManageRuleActivity)
                            }
                        }
                    })
            }
        }
    }

    private fun testServerRulesWithHeaders(
        url: String,
        header_name: String?,
        header_value: String?
    ) {
        binding.progressBar.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl("https://technews4u1.in/")
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("app_name", "Whatsapp")
        jsonObject.put("message", "This is a test message")
        jsonObject.put("sender", "")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()
        // Create RequestBody ( We're not using any converter, like GsonConverter, MoshiConverter e.t.c, that's why we use RequestBody )
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        CoroutineScope(Dispatchers.IO).launch {
            // Do the POST request and get response
            val myMap: Map<String, String> =
                mapOf(header_name.toString() to header_value.toString())
            val response: Response<ResponseBody> =
                service.testApiWithHeaders(url, myMap, requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    if (response.headers()["Content-Type"] != null) {
                        if (response.body()?.contentType().toString() == "application/json") {
                            try {
                                val json = JSONObject(
                                    response.body()?.string()!!
                                ) // String instance holding the above json
                                val status = json.getString("reply")
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@ManageRuleActivity,
                                    "Reply is : $status",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@ManageRuleActivity,
                                    "Reply is : ${e.message.toString()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this@ManageRuleActivity,
                                "Incorrect Headers Received",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ManageRuleActivity,
                            "Invalid Headers Received",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ManageRuleActivity, "Incorrect Request", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun testServerRulesWithOutHeaders(url: String) {
        binding.progressBar.visibility = View.VISIBLE
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://technews4u.in/")
                .build()

            val service = retrofit.create(APIService::class.java)

            val jsonObject = JSONObject()
            jsonObject.put("app_name", "Whatsapp")
            jsonObject.put("message", "This is a test message")
            jsonObject.put("sender", "")

            val jsonObjectString = jsonObject.toString()
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                val response: Response<ResponseBody> =
                    service.testApiWithOutHeaders(url, requestBody)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (response.headers()["Content-Type"] != null) {
                            if (response.body()?.contentType().toString() == "application/json"
                            ) {

                                try {
                                    val json = JSONObject(
                                        response.body()?.string()!!
                                    )
                                    val status = json.getString("reply")
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this@ManageRuleActivity,
                                        "Reply is : $status",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Log.d("testServerRules11" , e.message.toString())
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        this@ManageRuleActivity,
                                        "Reply is : ${e.message.toString()}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@ManageRuleActivity,
                                    "Incorrect Headers Received",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this@ManageRuleActivity,
                                "Invalid Headers Received",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ManageRuleActivity,
                            "Incorrect Request",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(
                this@ManageRuleActivity,
                "Incorrect Request",
                Toast.LENGTH_LONG
            ).show()
            Log.e(tag, e.message.toString())
        }

    }

    private fun deleteRule() {
        GlobalScope.launch {
            val db = DatabaseHelper().getInstance(this@ManageRuleActivity)
            if(isUpdating && messageId > 0){
                db.rulesDao().deleteRuleById(messageId)
            }
            val intent = Intent(this@ManageRuleActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun updateUi(rule : Rules) {
        binding.messageReply.setText(rule.reply_message)
        binding.messageReceived.setText(rule.incoming_message)
        if (rule.matchType == "similarly") {
            binding.radiaId2.isChecked = true
            binding.radiaId1.isChecked = false
        } else {
            binding.radiaId1.isChecked = true
            binding.radiaId2.isChecked = false
        }

        if(rule.isServerReply){
            binding.serverReplyLayout.visibility = View.VISIBLE
            binding.serverReplySwitch.isChecked = true
            binding.serverUrl.setText(rule.serverUrl)
            binding.headerName.setText(rule.headerKey)
            binding.headerValue.setText(rule.headerValue)
        }else{
            binding.serverReplyLayout.visibility = View.GONE
            binding.serverReplySwitch.isChecked = false
            binding.serverUrl.setText("")
            binding.headerName.setText("")
            binding.headerValue.setText("")
        }
    }

    private fun validateAndSaveRule() {
        if(validateRule()) {
            GlobalScope.launch {
                val db = DatabaseHelper().getInstance(this@ManageRuleActivity)

                if(!isUpdating){
                    val exist : List<Rules> = db.rulesDao().getRuleByIncomingMessage(binding.messageReceived.text.toString())
                    if (exist.isNotEmpty()){
                        showSnackBar("Given Incoming message already exists in rules")
                        return@launch
                    }
                }

                var matchType = "similarly"
                var serverReply = false
                var serverUrl = ""
                var headerKey = ""
                var headerValueString = ""
                if(binding.radiaId1.isChecked)
                    matchType = "exactly"

                if(binding.serverReplySwitch.isChecked){
                    serverReply = true
                    serverUrl = binding.serverUrl.text.toString()
                    headerKey = binding.headerName.text.toString()
                    headerValueString = binding.headerValue.text.toString()
                }

                if (isUpdating) {
                    db.rulesDao().updateRule(Rules(
                        messageId, binding.messageReceived.text.toString(), binding.messageReply.text.toString(),
                        matchType, serverReply, serverUrl, headerKey, headerValueString
                    ))
                } else {
                    db.rulesDao().insertRule(
                        Rules(
                            null, binding.messageReceived.text.toString(), binding.messageReply.text.toString(),
                            matchType, serverReply, serverUrl, headerKey, headerValueString
                        )
                    )
                }
                val intent = Intent(this@ManageRuleActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun validateRule(): Boolean {
        if(binding.messageReceived.text.toString() == ""){
            showSnackBar("Please enter incoming message")
            return false
        }
        if(binding.messageReply.text.toString() == ""  && !binding.serverReplySwitch.isChecked){
            showSnackBar("Please enter replying message")
            return false
        }
        if(!binding.radiaId1.isChecked && !binding.radiaId2.isChecked){
            showSnackBar("Please select a matching pattern")
            return false
        }

        if(binding.radiaId2.isChecked){
            if(!binding.messageReceived.text.contains("*")){
                showSnackBar("Incoming message must contains * anywhere")
                return false
            }
        }

        if(binding.serverReplySwitch.isChecked){
            if(binding.serverUrl.text.toString() == ""){
                showSnackBar("Please enter a server URL")
                return false
            }
            if(!URLUtil.isValidUrl(binding.serverUrl.text.toString())){
                showSnackBar("Please enter a valid server URL")
                return false
            }
        }
        return true
    }

    private fun showSnackBar(title : String){
        val snackBar = Snackbar.make(findViewById(R.id.root_layout), title,
            Snackbar.LENGTH_LONG).setAction("Action", null)
        snackBar.setActionTextColor(Color.BLACK)
        snackBar.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
