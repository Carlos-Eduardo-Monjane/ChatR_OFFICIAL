package com.vtrixdigital.chatr.ui.activities

import android.os.Bundle
//import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.vtrixdigital.chatr.R


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            /*val editTextPreference =
                preferenceManager.findPreference<EditTextPreference>("bulk_message_daily_limit")
                editTextPreference!!.setOnBindEditTextListener { editText ->
                editText.inputType =  InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            }

            val editTextPreference1 =
                preferenceManager.findPreference<EditTextPreference>("auto_reply_daily_limit")
                editTextPreference1!!.setOnBindEditTextListener { editText ->
                editText.inputType =  InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            }*/
        }
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            super.onDisplayPreferenceDialog(preference)
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            return when (preference?.key) {
                getString(R.string.key_darkmode) -> {
                    if((preference as SwitchPreferenceCompat).isChecked){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }else{
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    true
                }
                else -> {
                    super.onPreferenceTreeClick(preference)
                }
            }
        }
    }
}
