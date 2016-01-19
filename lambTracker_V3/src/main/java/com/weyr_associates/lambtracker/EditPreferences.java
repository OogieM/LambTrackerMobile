package com.weyr_associates.lambtracker;

//import java.util.ArrayList;
//import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
//import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import com.weyr_associates.lambtracker.R;

public class EditPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
				
		//Check if there is a Bluetooth device. If not, disable option for Receiver settings
		Preference pr = findPreference("readersettings");
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		if(bta == null) {
			pr.setEnabled(false);
			pr.setSummary("No Bluetooth Device Found");
		} else {
			pr.setEnabled(true);	
			pr.setSummary("");
		}
		
        // Display the current values
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

	}

	@Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Do something. A preference value changed
		updatePrefSummary(findPreference(key));
    }
	private void initSummary(Preference p){
		if (p instanceof PreferenceScreen) {
			PreferenceScreen pScr = (PreferenceScreen) p;
			for (int i = 0; i < pScr.getPreferenceCount(); i++) {
				initSummary(pScr.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
     }
	
	private void updatePrefSummary(Preference p){
		updatePrefSummary(p, true);
	}
	private void updatePrefSummary(Preference p, boolean updatevis){
		if (p instanceof ListPreference) {
        	ListPreference listPref = (ListPreference) p;
        	p.setSummary(listPref.getEntry());
        	
        } else if (p instanceof EditTextPreference) {
        	EditTextPreference editTextPref = (EditTextPreference) p; 
        	p.setSummary(editTextPref.getText());
        }
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
