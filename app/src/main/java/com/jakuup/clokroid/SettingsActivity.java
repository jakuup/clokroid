package com.jakuup.clokroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.titleActivitySettings);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_header, rootKey);
        }
    }

    public static class BluetoothFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener,Preference.OnPreferenceChangeListener {

        private SwitchPreferenceCompat switchAutoConnect;
        private EditTextPreference editConnectInterval;
        private PreferenceCategory categoryDevice;
        private ListPreference listDevices;

        private CharSequence[] getPairedDevices(Context context) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String pairedDevices = preferences.getString("bth_paired_devices", "");

            if (pairedDevices.length() > 0) {
                List<String> list = Arrays.asList(pairedDevices.split(";"));
                CharSequence[] devices = new CharSequence[list.size()];
                int i = 0;
                for (String element : list) {
                    devices[i++] = element;
                }
                return devices;
            }
            return null;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (switchAutoConnect.equals(preference)) {
                boolean visible = switchAutoConnect.isChecked();
                editConnectInterval.setVisible(visible);
                categoryDevice.setVisible(visible);
            }
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (editConnectInterval.equals(preference)) {
                String value = newValue.toString();
                if (value.length() > 0) {
                    editConnectInterval.setSummary(value + " " + getString(R.string.seconds));
                }
                else {
                    editConnectInterval.setSummary("");
                }
            }
            else if (listDevices.equals(preference)) {
                categoryDevice.setSummary(newValue.toString());
            }
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager preferenceManager = getPreferenceManager();
            Context context = preferenceManager.getContext();
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

            switchAutoConnect = new SwitchPreferenceCompat(context);
            switchAutoConnect.setKey("bth_auto_connect");
            switchAutoConnect.setTitle(R.string.prefsBthAutoConnect);
            switchAutoConnect.setOnPreferenceClickListener(this);
            screen.addPreference(switchAutoConnect);
            boolean visible = switchAutoConnect.isChecked();

            String interval = preferences.getString("bth_auto_connect_interval", "");

            editConnectInterval = new EditTextPreference(context);
            editConnectInterval.setKey("bth_auto_connect_interval");
            editConnectInterval.setTitle(R.string.prefsBthAutoConnectionInterval);
            editConnectInterval.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            onPreferenceChange(editConnectInterval, interval);
            editConnectInterval.setOnPreferenceChangeListener(this);
            editConnectInterval.setVisible(visible);
            screen.addPreference(editConnectInterval);

            String device = preferences.getString("bth_auto_connect_device", getString(R.string.prefsBthDeviceNone));

            categoryDevice = new PreferenceCategory(context);
            categoryDevice.setTitle(R.string.prefsBthDevice);
            categoryDevice.setSummary(device);
            categoryDevice.setVisible(visible);
            screen.addPreference(categoryDevice);

            CharSequence[] devices = getPairedDevices(context);
            if (devices != null) {
                listDevices = new ListPreference(context);
                listDevices.setKey("bth_auto_connect_device");
                listDevices.setTitle(R.string.prefsBthChangeDevice);
                listDevices.setEntries(devices);
                listDevices.setEntryValues(devices);
                listDevices.setOnPreferenceChangeListener(this);
                categoryDevice.addPreference(listDevices);
            }
            else {
                categoryDevice.setSummary(R.string.prefsBthDeviceEmpty);
            }
            setPreferenceScreen(screen);
        }
    }
}
