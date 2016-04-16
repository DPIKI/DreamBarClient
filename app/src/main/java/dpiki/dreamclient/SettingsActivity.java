package dpiki.dreamclient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import dpiki.dreamclient.Network.NetworkService;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        findPreference(getString(R.string.s_pref_key_ip)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return NetworkService.parseIp(newValue.toString());
            }
        });

        findPreference(getString(R.string.s_pref_key_port)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int port = Integer.parseInt(newValue.toString());
                    return (port > 1024 && port < 0xFFFF);
                }
                catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        findPreference(getString(R.string.s_pref_key_ip)).setSummary(pref.getString(getString(R.string.s_pref_key_ip), "IP адрес не задан"));
        findPreference(getString(R.string.s_pref_key_port)).setSummary(pref.getString(getString(R.string.s_pref_key_port), "Порт не задан"));
        findPreference(getString(R.string.s_pref_key_name)).setSummary(pref.getString(getString(R.string.s_pref_key_name), "Имя не задано"));
        findPreference(getString(R.string.s_pref_key_password)).setSummary(pref.getString(getString(R.string.s_pref_key_password), "Пароль не задан"));
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.s_pref_key_ip))) {
                findPreference(key).setSummary(sharedPreferences.getString(key, "IP адрес не задан"));
            } else if (key.equals(getString(R.string.s_pref_key_port))) {
                findPreference(key).setSummary(sharedPreferences.getString(key, "Порт не задан"));
            } else if (key.equals(getString(R.string.s_pref_key_name))) {
                findPreference(key).setSummary(sharedPreferences.getString(key, "Имя не задано"));
            } else if (key.equals(getString(R.string.s_pref_key_password))) {
                findPreference(key).setSummary(sharedPreferences.getString(key, "Пароль не задан"));
            }
        }
    };
}
