package com.luminia.tradegems;

import java.util.regex.Pattern;

import com.luminia.tradegems.database.GameAccount;
import com.luminia.tradegems.database.MyDBAdapter;
import com.luminia.tradegems.widgets.SelectAccountDialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements OnClickListener, LocationListener{
	
	public final static String SERVICE_URL = "http://trade-gems.appspot.com/";

	private static final String TAG = "MainActivity";
	
	// Shared preferences Keys
	private static final String KEY_LATITUDE = "PREF_LATITUDE";
	private static final String KEY_PROVIDER = "PREF_PROVIDER";
	private static final String KEY_LONGITUDE = "PREF_LONGITUDE";
	private static final String KEY_ACCURACY = "PREF_ACCURARY";

	private MyDBAdapter dbAdapter;
	private Button playGameButton;
	private Button highScoreButton;
	private Button aboutButton;
	private Button topTenButton;
	private Button usersOfGameButton;
	private Button locationButton;
	
	LocationManager mLocationManager;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		playGameButton = (Button) findViewById(R.id.playGameButton);
		highScoreButton = (Button) findViewById(R.id.highScoreButton);
		aboutButton = (Button) findViewById(R.id.aboutButton);
		
		topTenButton = (Button) findViewById(R.id.viewTopTen);
		usersOfGameButton = (Button) findViewById(R.id.usersOfGame);
		locationButton = (Button) findViewById(R.id.viewLocation);

		playGameButton.setOnClickListener(this);
		highScoreButton.setOnClickListener(this);
		aboutButton.setOnClickListener(this);
		
		topTenButton.setOnClickListener(this);
		usersOfGameButton.setOnClickListener(this);
		locationButton.setOnClickListener(this);
		
		dbAdapter = MyDBAdapter.getInstance(this);
		checkAccounts();
		detectUserLocation();
    }
    
    private void checkAccounts(){
    	GameAccount gameAccount = dbAdapter.getDefaultAccount();
    	if(gameAccount == null){
    		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
    		Account[] accounts = AccountManager.get(this).getAccounts();
    		// If there is more than one account in the device, we need to ask
    		// the user to select one
    		if(accounts.length > 1){
        		for (Account account : accounts) {
        		    if (emailPattern.matcher(account.name).matches()) {
        		        String possibleEmail = account.name;
        		        Log.d(TAG,"Possible email: "+possibleEmail);
        		    }
        		}
    			Bundle bundle = new Bundle();
    			bundle.putParcelableArray("accounts",accounts);
    			showAccountSelectionDialog(bundle);
    		}else if(accounts.length == 1){
    			
    		}else{
    			// TODO: No account detected, what to do?
    			Log.w(TAG,"No account detected in device");
    		}
    	}
    }
    
    @Override
	public void onClick(View button) {
		if (button == playGameButton) {
			Intent intent = new Intent(this, GameActivity.class);
			startActivity(intent);
		} else if (button == highScoreButton) {
			Intent intent = new Intent(this, HighScoreActivity.class);
			startActivity(intent);
		} else if (button == aboutButton) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (button == topTenButton) {
			Intent intent = new Intent(this, TopTenActivity.class);
			startActivity(intent);
		} else if (button == usersOfGameButton) {
			Intent intent = new Intent(this, UsersOfGameActivity.class);
			startActivity(intent);
		} else if (button == locationButton) {
			Intent intent = new Intent(this, UsersLocationActivity.class);
			startActivity(intent);
		}
		
		//unknown button.
	}
    
    private void showAccountSelectionDialog(Bundle bundle){
		SelectAccountDialog selectAccountDialog = SelectAccountDialog.newInstance(bundle);
    	Fragment prev = this.getSupportFragmentManager().findFragmentByTag("dialog");

    	// Removing any currently shown dialog
    	FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
    	if (prev != null) {
            ft.remove(prev);
        }
    	selectAccountDialog.show(getSupportFragmentManager(), "accounts-tag");
    }
    
    /**
     * This method will try to determine the user's current location without interfering 
     * with the gameplay.
     */
    private void detectUserLocation(){
    	mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		criteria.setAltitudeRequired(false);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		String providerName = mLocationManager.getBestProvider(criteria, true);
		Log.d(TAG,"Provider selected based on given criteria: "+providerName);
//		locationManager.requestSingleUpdate(criteria, this, null);
		mLocationManager.requestLocationUpdates(providerName, 10000, 10, this);
    }

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,"onLocationChanged");
		SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putFloat(KEY_LATITUDE,(float) location.getLatitude());
		editor.putFloat(KEY_LONGITUDE, (float) location.getLongitude());
		editor.putFloat(KEY_ACCURACY, location.getAccuracy());
		editor.putString(KEY_PROVIDER, location.getProvider());
		editor.commit();
		Log.d(TAG,"Latitude: "+location.getLatitude());
		Log.d(TAG,"Longitude: "+location.getLongitude());
		Log.d(TAG,"Accuracy: "+location.getAccuracy());
		Log.d(TAG,"Provider: "+location.getProvider());
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}