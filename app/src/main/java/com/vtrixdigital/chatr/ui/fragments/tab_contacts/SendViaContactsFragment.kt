package com.vtrixdigital.chatr.ui.fragments.tab_contacts

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.adapters.SendViaCsvAdapter
import com.vtrixdigital.chatr.database.AppDatabase
import com.vtrixdigital.chatr.database.BulkImportList
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.databinding.SendViaContactsFragmentBinding
import com.vtrixdigital.chatr.models.UriUtils
import com.vtrixdigital.chatr.utils.Constants
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE_WITHOUT_PLUS
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wafflecopter.multicontactpicker.ContactResult
import com.wafflecopter.multicontactpicker.LimitColumn
import com.wafflecopter.multicontactpicker.MultiContactPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SendViaContactsFragment : Fragment() {
    private var _binding: SendViaContactsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SendViaContactsViewModel
    private var db: AppDatabase? = null
    private val recordListAdapter = SendViaCsvAdapter(arrayListOf())
    private lateinit var dialogView: View
    private lateinit var customDialog: AlertDialog
    private val contactsPickerCode = 1001
    private lateinit var chooseContacts: TextView
    private var selectedContacts = arrayListOf<String>()
    //results for intent variables
    private lateinit var resultForChoosingImage : ActivityResultLauncher<Intent>
    private lateinit var resultForChoosingVideo : ActivityResultLauncher<Intent>
    private lateinit var resultForChoosingDocument : ActivityResultLauncher<Intent>

    private var selectedAttachmentPath: String? = null

    //choosingAttachmentsTextViews
    private lateinit var chooseImage : TextView
    private lateinit var chooseVideo : TextView
    private lateinit var chooseDocument : TextView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SendViaContactsFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        setupAdmobAds(context, root)
        viewModel = ViewModelProvider(this).get(SendViaContactsViewModel::class.java)
        db = context?.let {
            DatabaseHelper().getInstance(it)
        }
        context?.let {
            checkPermissions(
                listOf(
                    Manifest.permission.READ_CONTACTS
                ), it
            )
        }
        viewModel.refresh(db, getString(R.string.contacts_source))

        binding.usersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordListAdapter
        }
        observeViewModel()

        binding.addNewList.setOnClickListener {
            val permission = context?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1,
                    Manifest.permission.READ_CONTACTS
                )
            }
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    context,
                    "Please allow permission to read contacts",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(context , "Your android version might not be supported" , Toast.LENGTH_SHORT).show()
                }
                setupChoosingDialogView(root, context)
            }
        }
        setUpActivityResultLaunchers()
        return root
    }

    private fun setUpActivityResultLaunchers() {
        resultForChoosingImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    proceedWithSelectedAttachment(it.data?.data , chooseImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error while selecting file", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        resultForChoosingVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    proceedWithSelectedAttachment(it.data?.data , chooseVideo)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error while selecting file", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        resultForChoosingDocument = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    proceedWithSelectedAttachment(it.data?.data , chooseDocument)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error while selecting file", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun proceedWithSelectedAttachment(uri: Uri? , choosingTextView: TextView) {
        if (context != null) {
            if (uri != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val filePath = UriUtils.getPathFromUri(requireContext(), uri)
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.name != "") {
                            if (dialogView.isShown) {
                                choosingTextView.text = file.name
                            }
                        }
                        selectedAttachmentPath = file.absoluteFile.path
                    }
                } else {
                    selectedAttachmentPath = uri.toString()
                    val contentResolver = requireContext().contentResolver
                    val cursor: Cursor? = contentResolver.query(
                        uri, null, null, null, null, null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName: String =
                                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            if (dialogView.isShown) {
                                choosingTextView.text = displayName
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupChoosingDialogView(root: View, context: Context?) {
        val viewGroup: ViewGroup = root.findViewById(R.id.root_layout)
        dialogView = layoutInflater.inflate(R.layout.add_contacts_popup, viewGroup, false)
        customDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .show()
        val btDismiss = dialogView.findViewById<Button>(R.id.btDismissCustomDialog)
        val messageType = dialogView.findViewById<AppCompatSpinner>(R.id.messageType)
        val spinner = dialogView.findViewById<AppCompatSpinner>(R.id.appType)
        val add = dialogView.findViewById<Button>(R.id.add)
        val message = dialogView.findViewById<EditText>(R.id.message)
        chooseImage = dialogView.findViewById(R.id.chooseImage)
        chooseVideo = dialogView.findViewById(R.id.chooseVideo)
        chooseDocument = dialogView.findViewById(R.id.chooseDocument)
        val campaignName = dialogView.findViewById<EditText>(R.id.campaignName)
        val formLayout = dialogView.findViewById<LinearLayout>(R.id.formLayout)
        val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.buttonLayout)
        chooseContacts = dialogView.findViewById(R.id.chooseContacts)
        val animationView = dialogView.findViewById<LottieAnimationView>(R.id.animationView)

        chooseContacts.setOnClickListener {
            getContacts(context)
        }

        chooseImage.setOnClickListener{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val permission = context?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "Please allow permission to read storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    resultForChoosingImage.launch(intent)
                }
            }else{
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                resultForChoosingImage.launch(intent)
            }
        }
        chooseVideo.setOnClickListener{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val permission = context?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "Please allow permission to read storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "video/*"
                    resultForChoosingVideo.launch(intent)
                }
            }else{
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "video/*"
                resultForChoosingVideo.launch(intent)
            }
        }
        chooseDocument.setOnClickListener{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val permission = context?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "Please allow permission to read storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/pdf"
                    resultForChoosingDocument.launch(intent)
                }
            }else{
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/pdf"
                resultForChoosingDocument.launch(intent)
            }
        }

        messageType.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                when (position) {
                    0 -> {
                        message.visibility = View.VISIBLE
                        chooseImage.visibility = View.GONE
                        chooseVideo.visibility = View.GONE
                        chooseDocument.visibility = View.GONE
                    }
                    1 -> {
                        message.visibility = View.GONE
                        chooseImage.visibility = View.VISIBLE
                        chooseVideo.visibility = View.GONE
                        chooseDocument.visibility = View.GONE
                    }
                    2 -> {
                        message.visibility = View.GONE
                        chooseImage.visibility = View.GONE
                        chooseVideo.visibility = View.VISIBLE
                        chooseDocument.visibility = View.GONE
                    }
                    3 -> {
                        message.visibility = View.GONE
                        chooseImage.visibility = View.GONE
                        chooseVideo.visibility = View.GONE
                        chooseDocument.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
                message.visibility = View.VISIBLE
                chooseImage.visibility = View.GONE
                chooseVideo.visibility = View.GONE
                chooseDocument.visibility = View.GONE
            }
        }

        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
        add.setOnClickListener {
            when {
                campaignName.text.toString() == "" -> {
                    Toast.makeText(context, "Please add campaign name", Toast.LENGTH_SHORT)
                        .show()
                }
                message.text.toString().isEmpty() && messageType.selectedItemPosition == 0 -> {
                    Toast.makeText(context, "Please add a message", Toast.LENGTH_SHORT).show()
                }
                messageType.selectedItemPosition == 1 && chooseImage.text.toString() == getString(R.string.select_image) -> {
                    Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                }
                messageType.selectedItemPosition == 2 && chooseImage.text.toString() == getString(R.string.select_video) -> {
                    Toast.makeText(context, "Please select a video", Toast.LENGTH_SHORT).show()
                }
                messageType.selectedItemPosition == 3 && chooseImage.text.toString() == getString(R.string.select_document) -> {
                    Toast.makeText(context, "Please select a document", Toast.LENGTH_SHORT).show()
                }
                selectedContacts.size == 0 -> {
                    Toast.makeText(
                        context,
                        "Please select at least one contact",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                    animationView.visibility = View.VISIBLE
                    formLayout.visibility = View.GONE
                    buttonLayout.visibility = View.GONE

                    var messageTypeString = "text"
                    when {
                        messageType.selectedItemId.toInt() == 1 -> {
                            messageTypeString = "image"
                        }
                        messageType.selectedItemId.toInt() == 2 -> {
                            messageTypeString = "video"
                        }
                        messageType.selectedItemId.toInt() == 3 -> {
                            messageTypeString = "document"
                        }
                    }

                    addContacts(
                        campaignName.text.toString(),
                        spinner.selectedItemId.toString(),
                        messageTypeString,
                        message.text.toString(),
                        animationView,
                        buttonLayout,
                        formLayout
                    )
                }
            }
        }
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

    private fun addContacts(
        campaignName: String,
        appName: String,
        messageType: String = "text",
        message: String,
        animationView: LottieAnimationView?,
        buttonLayout: LinearLayout?,
        formLayout: LinearLayout?
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            for (contact in selectedContacts) {
                var countryCode: String
                var number: String

                if (contact.startsWith(COUNTRY_CODE)) {
                    val contactArray = contact.split(COUNTRY_CODE)
                    countryCode = contactArray[0].drop(0)
                    number = contactArray[1]
                } else if (contact.startsWith(COUNTRY_CODE_WITHOUT_PLUS) && contact.length >= 11) {
                    val contactArray = contact.split(COUNTRY_CODE_WITHOUT_PLUS)
                    countryCode = contactArray[0]
                    number = contactArray[1]
                } else if (contact.length == 10) {
                    countryCode = COUNTRY_CODE_WITHOUT_PLUS
                    number = contact
                } else {
                    continue
                }

                db?.bulkImportDao()?.insertBulkImport(
                    BulkImportList(
                        null,
                        countryCode,
                        number,
                        message,
                        false,
                        appName,
                        campaignName,
                        getString(R.string.contacts_source),
                        null,
                        messageType,
                        selectedAttachmentPath,
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                    )
                )
            }
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Imported successfully", Toast.LENGTH_SHORT).show()
                animationView?.visibility = View.GONE
                buttonLayout?.visibility = View.VISIBLE
                formLayout?.visibility = View.VISIBLE
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
            return@launch
        }
    }

    private fun observeViewModel() {
        viewModel.records?.observe(viewLifecycleOwner, { records ->
            records?.let {
                recordListAdapter.updateRecords(it)
                if (records.isNotEmpty()) {
                    binding.animationView.visibility = View.GONE
                    binding.usersList.visibility = View.VISIBLE
                } else {
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

    private fun checkPermissions(permissions: Collection<String>, context: Context) {
        Dexter.withContext(context)
            .withPermissions(
                permissions
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
//                        Toast.makeText(context, "Permissions Granted" , Toast.LENGTH_SHORT).show()
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(context, "Permissions Denied", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                }
            }).check()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getContacts(context: Context?) {
        if (context != null) {
            MultiContactPicker.Builder(this)
                .hideScrollbar(false) //Optional - default: false
                .showTrack(true) //Optional - default: true
                .searchIconColor(Color.WHITE) //Option - default: White
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(context.getColor(R.color.teal_200)) //Optional - default: Azure Blue
                .bubbleColor(
                    ContextCompat.getColor(
                        context,
                        R.color.teal_700
                    )
                ) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .setTitleText("Select Contacts") //Optional - default: Select Contacts
                .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .setActivityAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .showPickerForResult(contactsPickerCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == contactsPickerCode) {
            if (resultCode == RESULT_OK) {
                selectedContacts = arrayListOf()
                val results: List<ContactResult> = MultiContactPicker.obtainResult(data)
                chooseContacts.text = getString(R.string.selected_contacts , results.size)
                if (results.isNotEmpty()) {
                    for (contact in results) {
                        for (number in contact.phoneNumbers) {
                            selectedContacts.add(number.number)
                        }
                    }
                }
            }
        }
    }
}