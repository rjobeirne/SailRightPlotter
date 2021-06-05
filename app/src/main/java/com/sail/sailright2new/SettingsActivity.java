package com.sail.sailright2new;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

//    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

//    Resources resources = this.getResources();

   public static final String MyPREFERENCES = "preferences" ;
   String value = "pref_seek_val";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // below line is used to check if
        // frame layout is empty or not.
        if (findViewById(R.id.idFrameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // below line is to inflate our fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.idFrameLayout, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

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

            Preference button = findPreference(getString(R.string.restore_defaults));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    restoreDefaultSettings();;
                    return false;
                }
            });
        }

        private void restoreDefaultSettings() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, true);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }
    }
}
