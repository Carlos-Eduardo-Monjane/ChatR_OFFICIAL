package com.vtrixdigital.chatr.ui.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.os.Environment.*
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arthurivanets.bottomsheets.BottomSheet
import com.vtrixdigital.chatr.R
import com.vtrixdigital.chatr.database.DatabaseHelper
import com.vtrixdigital.chatr.ui.bottom_sheets.BottomSheetImportExport
import com.vtrixdigital.chatr.utils.Constants
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE
import com.vtrixdigital.chatr.utils.Constants.Companion.COUNTRY_CODE_WITHOUT_PLUS
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.limerse.iap.IapConnector
import ir.androidexception.roomdatabasebackupandrestore.Backup
import ir.androidexception.roomdatabasebackupandrestore.Restore
import java.io.File
import java.util.*
import com.limerse.iap.DataWrappers
import com.limerse.iap.PurchaseServiceListener
import android.view.ViewGroup




class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var iapConnector: IapConnector
    private var doubleBackToExitPressedOnce = false
    private lateinit var dialogView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.white))
        setSupportActionBar(toolbar)
        initInApp()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_sentMessages,
                R.id.nav_messageANumber,
                R.id.nav_bulk_sender,
                R.id.nav_terms,
                R.id.nav_policy,
                R.id.nav_rateus
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                showToast(this, "Please allow storage permission!")
                checkPermissions(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), this
                )
            }
        }
        changeCountryCode()
        if(Constants().showPremiumPopup()){
            showPopup()
        }
    }

    private fun showPopup() {
        val viewGroup =
            (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        dialogView = LayoutInflater.from(this).inflate(R.layout.buy_premium_dialog, viewGroup, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

        dialogView.findViewById<Button>(R.id.close).setOnClickListener {
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.buy_now).setOnClickListener {
            alertDialog.dismiss()
            removeAds()
        }
    }

    private fun initInApp() {
        val nonConsumablesList = arrayListOf(Constants.productId)
        val consumablesList = arrayListOf<String>()
        val subsList = arrayListOf<String>()
        iapConnector =  IapConnector(
            context = this, // activity / context
            nonConsumableKeys = nonConsumablesList, // pass the list of non-consumables
            consumableKeys = consumablesList, // pass the list of consumables
            subscriptionKeys = subsList, // pass the list of subscriptions
            key = Constants.licenseKey,
            enableLogging = true // to enable / disable logging
        )
        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
//                showToast(this@MainActivity, "onPricesUpdated")
            }
            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                if (purchaseInfo.sku == Constants.productId && purchaseInfo.purchaseState == 1) {
                    showToast(this@MainActivity, "Thanks for purchasing!")
                    Constants.isPurchased = true
                }else{
                    showToast(this@MainActivity, "Your purchase was unsuccessful!")
                    Constants.isPurchased = false
                }
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                Constants.isPurchased = purchaseInfo.purchaseState != 1
            }
        })
    }

    private fun changeCountryCode() {
        val countryId :  String
        var countryCode = ""

        val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        countryId = manager.simCountryIso.toUpperCase(Locale.ROOT)
        val rl = this.resources.getStringArray(R.array.CountryCodes)
        for (i in rl.indices) {
            val g = rl[i].split(",").toTypedArray()
            if (g[1].trim { it <= ' ' } == countryId.trim { it <= ' ' }) {
                countryCode = g[0]
                break
            }
        }
        if(countryCode != ""){
            if(countryCode.startsWith("+")){
                COUNTRY_CODE = countryCode
                COUNTRY_CODE_WITHOUT_PLUS = countryCode.drop(0)
            }else{
                COUNTRY_CODE_WITHOUT_PLUS = countryCode
                COUNTRY_CODE = "+$countryCode"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(!Constants.isPurchased)
            menu.add(0, 8, 8, "Get Premium")
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> gotoSettings()
            R.id.action_import_export -> importExportDb()
            8 -> removeAds()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun removeAds() {
        iapConnector.purchase(this, Constants.productId)
    }

    private fun importExportDb() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            showToast(this, "Please allow storage permission!")
        }else{
            val bottomSheet = BottomSheetImportExport(this).also(BottomSheet::show)
            bottomSheet.findViewById<Button>(R.id.import_rules).setOnClickListener{
                importDB(bottomSheet)
            }
            bottomSheet.findViewById<Button>(R.id.export_rules).setOnClickListener{
                exportDB(bottomSheet)
            }
        }
    }

    private fun getLatestFileFromDir(dirPath: String): File? {
        val dir = File(dirPath)
        val files = dir.listFiles()
        if (files == null || files.isEmpty()) {
            Log.d("importDB", "NULL at : $dirPath")
            return null
        }
        var lastModifiedFile = files[0]
        for (i in 1 until files.size) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i]
            }
        }
        return lastModifiedFile
    }

    private fun importDB(bottomSheet: BottomSheetImportExport) {
        val database = DatabaseHelper().getInstance(this)
        val file = getLatestFileFromDir(getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS + File.separator + "AutoReply" + File.separator + "Backups" + File.separator).absolutePath.toString())
        if(file != null){
            Restore.Init()
                .database(database)
                .backupFilePath(file.absolutePath.toString())
                .secretKey("qwerty@123") // if your backup file is encrypted, this parameter is required
                .onWorkFinishListener { success, message ->
                    bottomSheet.dismiss()
                    if(success){
                        showToast(this, "Imported successfully!")
                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        showToast(this, "An error occurred!")
                    }
                    Log.d("importDB", message.toString())
                }
                .execute()
        }else{
            showToast(this, "No backup found!")
        }

    }

    private fun exportDB(bottomSheet: BottomSheetImportExport) {
        if(getExternalStorageState() == MEDIA_MOUNTED){
            val database = DatabaseHelper().getInstance(this)
            val fileName = Date().time.toString() + ".txt"
            val newFile = File(
                getExternalStorageDirectory() , "AutoReply" + File.separator + "Backups")
            if(!newFile.exists()){
                newFile.mkdirs()
            }

            Backup.Init()
                .database(database)
                .path(newFile.absolutePath.toString())
                .fileName(fileName)
                .secretKey("qwerty@123") //optional
                .onWorkFinishListener { success, message ->
                    // do anything
                    bottomSheet.dismiss()
                    if(success){
                        showToast(this, "Backup created successfully!")
                    }else{
                        showToast(this, message)
                    }
                }
                .execute()
        }else{
            showToast(this, "Can't access storage")
        }
    }

    private fun gotoSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkPermissions(permissions: Collection<String>, context: Context) {
        Dexter.withContext(context)
            .withPermissions(
                permissions
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        showToast(context, "Permissions Granted")
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {

                }
            }).check()
    }

    fun showToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Double press back button to exit!", Toast.LENGTH_SHORT)
            .show()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            doubleBackToExitPressedOnce = false
        },1000)
    }
}