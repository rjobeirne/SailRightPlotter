package com.sail.sailrightplotter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

//    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);

//    Resources resources = this.getResources();

   public static final String MyPREFERENCES = "preferences" ;

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

                //Locate go back button
        TextView goBack = findViewById(R.id.back_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

                //Locate help button
        TextView helpPage = findViewById(R.id.help_button);
        helpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, HelpActivity.class);
                startActivity(i);
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            EditTextPreference editTextPreference1 =
                    getPreferenceManager().findPreference("prefs_default_start_time");
            EditTextPreference editTextPreference2 =
                    getPreferenceManager().findPreference("prefs_bow_to_gps");
            EditTextPreference editTextPreference3 =
                    getPreferenceManager().findPreference("prefs_speed_smooth");
            EditTextPreference editTextPreference4 =
                    getPreferenceManager().findPreference("prefs_heading_smooth");
            EditTextPreference editTextPreference5 =
                    getPreferenceManager().findPreference("prefs_proximity_dist");
            EditTextPreference editTextPreference6 =
                    getPreferenceManager().findPreference("prefs_start_margin");
            EditTextPreference editTextPreference7 =
                    getPreferenceManager().findPreference("prefs_delta_bearing");
            ListPreference orientation =
                    getPreferenceManager().findPreference("prefs_orientation");
            ListPreference division =
                    getPreferenceManager().findPreference("prefs_division");

            editTextPreference1.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference2.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference3.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference4.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference5.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference6.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            editTextPreference7.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                    });

            orientation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return true;
                }
            });

            division.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return true;
                }
            });

            Preference button = findPreference(getString(R.string.restore_defaults));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    restoreDefaultSettings();
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
