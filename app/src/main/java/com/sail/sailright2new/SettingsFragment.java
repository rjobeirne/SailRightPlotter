package com.sail.sailright2new;



import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // below line is used to add preference
//        // fragment from our xml folder.
//         addPreferencesFromResource(R.xml.preferences);
//    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
         setPreferencesFromResource(R.xml.preferences, rootKey);

    androidx.preference.EditTextPreference editTextPreference1 =
            getPreferenceManager().findPreference("prefs_default_start_time");
    androidx.preference.EditTextPreference editTextPreference2 =
            getPreferenceManager().findPreference("prefs_bow_to_gps");
    androidx.preference.EditTextPreference editTextPreference3 =
            getPreferenceManager().findPreference("prefs_speed_smooth");
    androidx.preference.EditTextPreference editTextPreference4 =
            getPreferenceManager().findPreference("prefs_heading_smooth");
    androidx.preference.EditTextPreference editTextPreference5 =
            getPreferenceManager().findPreference("prefs_proximity_dist");
    androidx.preference.EditTextPreference editTextPreference6 =
            getPreferenceManager().findPreference("prefs_start_margin");

        editTextPreference1.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        editTextPreference2.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        editTextPreference3.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        editTextPreference4.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        editTextPreference5.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });

        editTextPreference6.setOnBindEditTextListener(
                new androidx.preference.EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });
    }


}
