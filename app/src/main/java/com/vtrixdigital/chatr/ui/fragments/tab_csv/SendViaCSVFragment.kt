package com.vtrixdigital.chatr.ui.fragments.tab_csv

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.os.Environment.getExternalStorageDirectory
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.vtrixdigital.chatr.databinding.SendViaCSVFragmentBinding
import com.vtrixdigital.chatr.models.UriUtils.getPathFromUri
import com.vtrixdigital.chatr.utils.CSVReader
import com.vtrixdigital.chatr.utils.CSVWriter
import com.vtrixdigital.chatr.utils.Constants
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE_WITHOUT_PLUS
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class SendViaCSVFragment : Fragment() {
    private var _binding: SendViaCSVFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: SendViaCSVViewModel
    private var recordsFetched = 0
    private val recordListAdapter = SendViaCsvAdapter(arrayListOf())
    private var db: AppDatabase? = null
    private var lastSelectedFilePath: String? = null
    private var lastSelectedUri: Uri? = null
    private lateinit var dialogView: View
    private lateinit var customDialog: AlertDialog
    private lateinit var resultForSavingFile: ActivityResultLauncher<Intent>
    private lateinit var resultForImportingFile: ActivityResultLauncher<Intent>

    //results for intent variables
    private lateinit var resultForChoosingImage : ActivityResultLauncher<Intent>
    private lateinit var resultForChoosingVideo : ActivityResultLauncher<Intent>
    private lateinit var resultForChoosingDocument : ActivityResultLauncher<Intent>

    private var selectedAttachmentPath: String? = null

    //choosingAttachmentsTextViews
    private lateinit var chooseImage : TextView
    private lateinit var chooseVideo : TextView
    private lateinit var chooseDocument : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SendViaCSVFragmentBinding.inflate(inflater, container, false)

        val root = binding.root
        setupAdmobAds(context , root)
        viewModel = ViewModelProvider(this).get(SendViaCSVViewModel::class.java)
        db = context?.let {
            DatabaseHelper().getInstance(it)
        }
        viewModel.refresh(db, getString(R.string.csv_source))

        binding.usersList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordListAdapter
        }

        observeViewModel()

        binding.addNewList.setOnClickListener {
            var showDialog = true
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
                    showDialog = false
                }
            }

            if (showDialog) {
                val viewGroup: ViewGroup = root.findViewById(R.id.root_layout)
                dialogView = layoutInflater.inflate(R.layout.add_csv_with_name, viewGroup, false)
                customDialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .show()
                val btDismiss = dialogView.findViewById<Button>(R.id.btDismissCustomDialog)
                val add = dialogView.findViewById<Button>(R.id.add)
                val messageType = dialogView.findViewById<AppCompatSpinner>(R.id.messageType)
                chooseImage = dialogView.findViewById(R.id.chooseImage)
                chooseVideo = dialogView.findViewById(R.id.chooseVideo)
                chooseDocument = dialogView.findViewById(R.id.chooseDocument)
                val spinner = dialogView.findViewById<AppCompatSpinner>(R.id.appType)
                val campaignName = dialogView.findViewById<EditText>(R.id.campaignName)
                val formLayout = dialogView.findViewById<LinearLayout>(R.id.formLayout)
                val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.buttonLayout)
                val chooseCSV = dialogView.findViewById<TextView>(R.id.chooseCSV)
                val animationView = dialogView.findViewById<LottieAnimationView>(R.id.animationView)
                chooseCSV.setOnClickListener {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "text/*"
                    resultForImportingFile.launch(intent)
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
                                chooseImage.visibility = View.GONE
                                chooseVideo.visibility = View.GONE
                                chooseDocument.visibility = View.GONE
                            }
                            1 -> {
                                chooseImage.visibility = View.VISIBLE
                                chooseVideo.visibility = View.GONE
                                chooseDocument.visibility = View.GONE
                            }
                            2 -> {
                                chooseImage.visibility = View.GONE
                                chooseVideo.visibility = View.VISIBLE
                                chooseDocument.visibility = View.GONE
                            }
                            3 -> {
                                chooseImage.visibility = View.GONE
                                chooseVideo.visibility = View.GONE
                                chooseDocument.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
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
                            showToastOnUI("Please add campaign name")
                        }
                        chooseCSV.text == "Choose CSV" -> {
                            showToastOnUI("Please select a CSV file1!")
                        }
                        else -> {
                            showToastOnUI("Please Wait!")
                            animationView.visibility = View.VISIBLE
                            formLayout.visibility = View.GONE
                            buttonLayout.visibility = View.GONE
                            var appName = getString(R.string.whatsapp_package)
                            when {
                                spinner.selectedItemId.toInt() == 1 -> {
                                    appName = getString(R.string.whatsapp_business_package)
                                }
                                spinner.selectedItemId.toInt() == 2 -> {
                                    appName = getString(R.string.gbwhatsapp_package)
                                }
                            }

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

                            import(
                                campaignName.text.toString(),
                                appName,
                                messageTypeString,
                                animationView,
                                buttonLayout,
                                formLayout
                            )
                        }
                    }
                }
            }
        }

        binding.downloadSample.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                writeSampleCsvFile()
            } else {
                val permission = context?.let { it1 ->
                    ContextCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "Please allow permission to write to storage",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    writeSampleCsvFile()
                }
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

        resultForSavingFile =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK && it.data != null) {
                    try {
                        val outputStream: OutputStream = it.data!!.data?.let { it1 ->
                            context?.contentResolver?.openOutputStream(
                                it1
                            )
                        }!!
                        val bw = BufferedWriter(OutputStreamWriter(outputStream))
                        bw.write("countryCode,number,message\n")
                        bw.write("91,9999999999,hi how are you")
                        bw.write("91,9999999999,hi how are you")
                        bw.flush()
                        bw.close()
                        Toast.makeText(context, "File downloaded successfully!", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "Error while saving file", Toast.LENGTH_SHORT).show()
                }
            }
        resultForImportingFile =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK && it.data != null) {
                    try {
                        checkCsvFile(it.data?.data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error while importing file", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun proceedWithSelectedAttachment(uri: Uri? , choosingTextView: TextView) {
        if (context != null) {
            if (uri != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val filePath = getPathFromUri(requireContext(), uri)
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
    private fun writeSampleCsvFile() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_TITLE, "Sample.csv")
                resultForSavingFile.launch(intent)
            } else {
                val exportDir = File(
                    getExternalStorageDirectory(),
                    "/AutoReply"
                )// your path where you want save your file
                if (!exportDir.exists()) {
                    exportDir.mkdirs()
                }
                val file = File(
                    exportDir,
                    "sample_file.csv"
                )
                file.createNewFile()
                val csvWrite = CSVWriter(FileWriter(file))
                csvWrite.writeNext(arrayOf("countryCode", "number", "message"))
                csvWrite.writeNext(arrayOf("91", "9999999999", "hi how are you"))
                csvWrite.close()
                Toast.makeText(context, "Sample file downloaded", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error : ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun checkCsvFile(uri: Uri?) {
        if (context != null) {
            if (uri != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val filePath = getPathFromUri(requireContext(), uri)
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.name != "") {
                            if (dialogView.isShown) {
                                dialogView.findViewById<TextView>(R.id.chooseCSV).text = file.name
                            }
                        }
                        lastSelectedFilePath = file.absoluteFile.path
                    }
                } else {
                    lastSelectedUri = uri
                    val contentResolver = requireContext().contentResolver
                    val cursor: Cursor? = contentResolver.query(
                        uri, null, null, null, null, null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName: String = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            if (dialogView.isShown) {
                                dialogView.findViewById<TextView>(R.id.chooseCSV).text = displayName
                            }
                        }
                    }
                }
            }
        }
    }

    private fun import(
        campaignName: String,
        appName:String,
        messageType : String,
        animationView: LottieAnimationView,
        buttonLayout: LinearLayout,
        formLayout: LinearLayout
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val csvReader = CSVReader(FileReader(lastSelectedFilePath))
            importWithReader(
                csvReader,
                appName,
                messageType,
                campaignName,
                animationView,
                buttonLayout,
                formLayout
            )
        } else {
            if (lastSelectedUri != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    val contentResolver = requireContext().contentResolver
                    val stringBuilder = StringBuilder()
                    contentResolver.openInputStream(lastSelectedUri!!)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            val bulkImportList =
                                db?.bulkImportDao()?.getCampaignByName(campaignName)
                            if (bulkImportList == null || bulkImportList.campaignName.isBlank()) {
                                var line: String? = reader.readLine()
                                var i = 0
                                while (line != null) {
                                    val dataArray = line.split(",")
                                    if (dataArray.size != 3) {
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(
                                                context,
                                                "Column count does not matched and we found ${dataArray.size} columns",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            animationView.visibility = View.GONE
                                            buttonLayout.visibility = View.VISIBLE
                                            formLayout.visibility = View.VISIBLE
                                        }
                                        return@launch
                                    }
                                    if (i == 0) {
                                        if (dataArray[0] != "countryCode" || dataArray[1] != "number" || dataArray[2] != "message") {
                                            Handler(Looper.getMainLooper()).post {
                                                Toast.makeText(
                                                    context,
                                                    "Required columns not found",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                animationView.visibility = View.GONE
                                                buttonLayout.visibility = View.VISIBLE
                                                formLayout.visibility = View.VISIBLE
                                            }
                                            return@launch
                                        }
                                    } else {
                                        db?.bulkImportDao()?.insertBulkImport(
                                            BulkImportList(
                                                null,
                                                dataArray[0],
                                                dataArray[1],
                                                dataArray[2],
                                                false,
                                                appName,
                                                campaignName,
                                                getString(R.string.csv_source),
                                                null,
                                                messageType,
                                                selectedAttachmentPath,
                                                SimpleDateFormat(
                                                    "yyyy-MM-dd HH:mm:ss",
                                                    Locale.getDefault()
                                                ).format(Date())
                                            )
                                        )
                                        recordsFetched++
                                    }

                                    stringBuilder.append(line)
                                    line = reader.readLine()
                                    i++
                                }
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Data imported successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    animationView.visibility = View.GONE
                                    buttonLayout.visibility = View.VISIBLE
                                    formLayout.visibility = View.VISIBLE
                                    if (customDialog.isShowing) {
                                        customDialog.dismiss()
                                    }
                                }
                                return@launch
                            }else{
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Empty List!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    animationView.visibility = View.GONE
                                    buttonLayout.visibility = View.VISIBLE
                                    formLayout.visibility = View.VISIBLE
                                }
                                return@launch
                            }
                        }

                    }
                }
            } else {
                showToastOnUI("No file selected")
            }
        }

    }

    private fun importWithReader(
        csvReader: CSVReader,
        appName:String,
        messageType:String,
        campaignName: String,
        animationView: LottieAnimationView,
        buttonLayout: LinearLayout,
        formLayout: LinearLayout
    ) {
        /* path of local storage (it should be your csv file location)*/
        var nextLine: Array<String>?
        var count = 0
        val columns = StringBuilder()
        GlobalScope.launch(Dispatchers.IO) {
            val bulkImportList = db?.bulkImportDao()?.getCampaignByName(campaignName)
            if (bulkImportList == null || bulkImportList.campaignName.isBlank()) {
                do {
                    val value = StringBuilder()
                    nextLine = csvReader.readNext()
                    nextLine?.let { nextLine ->
                        for (i in nextLine.indices) {
                            if (count == 0) {
                                if (i == nextLine.size - 1) {
                                    columns.append(nextLine[i])
                                    count = 1
                                } else
                                    columns.append(nextLine[i]).append(",")
                            } else {
                                if (i == nextLine.size - 1) {
                                    value.append(nextLine[i])
                                    count = 2
                                } else
                                    value.append(nextLine[i]).append(",")
                            }
                        }
                        if (count == 2) {
                            val columnsArray = columns.split(",")
                            val valuesArray = value.split(",")
                            var countryCode = COUNTRY_CODE_WITHOUT_PLUS
                            if (columnsArray.size == 3) {
                                if (columnsArray[0] == "countryCode" && columnsArray[1] == "number" && columnsArray[2] == "message") {
                                    if (valuesArray[0] != "") {
                                        countryCode = valuesArray[0]
                                    }
                                    db?.bulkImportDao()?.insertBulkImport(
                                        BulkImportList(
                                            null,
                                            countryCode,
                                            valuesArray[1],
                                            valuesArray[2],
                                            false,
                                            appName,
                                            campaignName,
                                            getString(R.string.csv_source),
                                            null,
                                            messageType,
                                            selectedAttachmentPath,
                                            SimpleDateFormat(
                                                "yyyy-MM-dd HH:mm:ss",
                                                Locale.getDefault()
                                            ).format(Date())
                                        )
                                    )
                                    recordsFetched++
                                } else {
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(
                                            context,
                                            "Required columns not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        animationView.visibility = View.GONE
                                        buttonLayout.visibility = View.VISIBLE
                                        formLayout.visibility = View.VISIBLE
                                    }
                                    return@launch
                                }
                            } else {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Column count does not matched and we found ${columnsArray.size} columns",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    animationView.visibility = View.GONE
                                    buttonLayout.visibility = View.VISIBLE
                                    formLayout.visibility = View.VISIBLE
                                }
                                return@launch
                            }
                        }
                    } ?: run {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "Import Finished with $recordsFetched records",
                                Toast.LENGTH_SHORT
                            ).show()
                            animationView.visibility = View.GONE
                            buttonLayout.visibility = View.VISIBLE
                            formLayout.visibility = View.VISIBLE
                            if (customDialog.isShowing) {
                                customDialog.dismiss()
                            }
                        }
                        return@launch
                    }
                } while ((nextLine) != null)
            } else {
                Handler(Looper.getMainLooper()).post {
                    animationView.visibility = View.GONE
                    buttonLayout.visibility = View.VISIBLE
                    formLayout.visibility = View.VISIBLE
                    Toast.makeText(context, "Campaign with same name exist!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun showToastOnUI(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}