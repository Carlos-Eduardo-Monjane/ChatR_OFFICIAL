package com.vtrixdigital.chatr.ui.bottom_sheets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.config.BaseConfig
import com.arthurivanets.bottomsheets.config.Config
import com.vtrixdigital.chatr.R

class BottomSheetChipInfo(
    hostActivity: Activity,
    config: BaseConfig = Config.Builder(hostActivity).build()): BaseBottomSheet(hostActivity,config){
    override fun onCreateSheetContentView(context : Context) : View {
        return LayoutInflater.from(context).inflate(
            R.layout.bottom_sheet_chip_info,
            this,
            false
        )
    }
}