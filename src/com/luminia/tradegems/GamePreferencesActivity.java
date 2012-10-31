package com.luminia.tradegems;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GamePreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.game_preferences);
	}

}
