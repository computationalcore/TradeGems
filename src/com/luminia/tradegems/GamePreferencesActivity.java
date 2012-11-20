package com.luminia.tradegems;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class GamePreferencesActivity extends PreferenceActivity {

	private static final String TAG = "GamePreferencesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.game_preferences);
		String key = getResources().getString(R.string.key_account);
		ListPreference listPreference = (ListPreference)findPreference(key);
		Account[] accounts = AccountManager.get(this).getAccounts();
		CharSequence[] entries = new CharSequence[accounts.length];
		CharSequence[] entryValues = new CharSequence[accounts.length];
		for(int i = 0; i < accounts.length; i++){
			entries[i] = accounts[i].name;
			entryValues[i] = accounts[i].name;
		}
		listPreference.setEntries(entries);
		listPreference.setEntryValues(entryValues);
	}

/* Just to check */
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		super.onPause();
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//		String key = getResources().getString(R.string.key_account);
//		Log.d(TAG,"Account: " + preferences.getString(key, "defValue"));
//	}
}
