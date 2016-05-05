package dpiki.dreamclient.SettingsActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import dpiki.dreamclient.Network.NetworkService;
import dpiki.dreamclient.R;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private void initToolbar(){
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar,root,false);
        toolbar.setTitle("Настройки");
        root.addView(toolbar, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /*Для работы стрелки back*/
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

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
        initToolbar();
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
