package com.luminia.tradegems;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.luminia.tradegems.database.GameAccount;
import com.luminia.tradegems.database.MyDBAdapter;
import com.luminia.tradegems.widgets.SelectAccountDialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.luminia.tradegems.GameSound;

public class MainActivity extends FragmentActivity implements OnClickListener, LocationListener{
	
	public final static String SERVICE_URL = "http://trade-gems.appspot.com/";
	//public final static String SERVICE_URL = "http://192.168.0.157:8080/";
	
	private static final String TAG = "MainActivity";
	
	// Shared preferences Keys
	public static final String KEY_PREFERENCES = "preferences";
	public static final String KEY_NICKNAME = "nickname";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_SCORE = "score";
	public static final String KEY_TURN = "turns";
	public static final String KEY_LATITUDE = "lat";
	public static final String KEY_LONGITUDE = "lon";
	public static final String KEY_CITY = "city";
	public static final String KEY_STATE = "state";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_PARENT = "parent_key";
	public static final String DEFAULT_NICKNAME = "user";
	public static final String DEFAULT_EMAIL = "anon@luminiasoft.com";
	

	private int mMusicResource = R.raw.dispersion_relation;
	private MyDBAdapter dbAdapter;
	private Button playGameButton;
	private Button aboutButton;
	private Button topTenButton;
	private Button settingsButton;
	
	// Location related attributes
	LocationManager mLocationManager;
	Location mCurrentLocation;
	ReverseGeocodeLookupTask reverseGeocodeLookupTask = new ReverseGeocodeLookupTask();
	
	/* Debug variable, remove this! */
	private long timestamp;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		playGameButton = (Button) findViewById(R.id.playGameButton);
		aboutButton = (Button) findViewById(R.id.aboutButton);		
		topTenButton = (Button) findViewById(R.id.viewTopTen);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		playGameButton.setOnClickListener(this);
		aboutButton.setOnClickListener(this);		
		topTenButton.setOnClickListener(this);
		settingsButton.setOnClickListener(this);

		dbAdapter = MyDBAdapter.getInstance(this);
		
		//Play music for this Activity
		//GameSound.playMusic(getApplicationContext(), R.raw.no_good_layabout);
		
		checkAccounts();
		detectUserLocation();
    }
    
    private void checkAccounts(){
    	GameAccount gameAccount = dbAdapter.getDefaultAccount();
    	if(gameAccount.getEmail() == DEFAULT_EMAIL){
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
		} else if (button == aboutButton) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (button == topTenButton) {
			Intent intent = new Intent(this, TopTenActivity.class);
			startActivity(intent);
		} else if(button == settingsButton){
			Intent intent = new Intent(this, GamePreferencesActivity.class);
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
    	timestamp = System.currentTimeMillis();
    	mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		criteria.setAltitudeRequired(false);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		String providerName = mLocationManager.getBestProvider(criteria, true);
		Log.d(TAG,"Provider selected based on given criteria: "+providerName);
		if(providerName != null)
			mLocationManager.requestLocationUpdates(providerName, 10000, 10, this);
		else
			Log.w(TAG,"No provider");
    }

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,"onLocationChanged");
		mCurrentLocation = location;
		SharedPreferences.Editor editor = getSharedPreferences(MainActivity.KEY_PREFERENCES,Context.MODE_PRIVATE).edit();
		editor.putFloat(KEY_LATITUDE,(float) location.getLatitude());
		editor.putFloat(KEY_LONGITUDE, (float) location.getLongitude());
		editor.putFloat(KEY_ACCURACY, location.getAccuracy());
		editor.putString(KEY_PROVIDER, location.getProvider());
		editor.commit();
		Log.d(TAG,"Latitude: "+location.getLatitude());
		Log.d(TAG,"Longitude: "+location.getLongitude());
		Log.d(TAG,"Accuracy: "+location.getAccuracy());
		Log.d(TAG,"Provider: "+location.getProvider());
		this.reverseGeocodeLookupTask.execute();
		mLocationManager.removeUpdates(this);
		long delta = System.currentTimeMillis() - timestamp;
		Log.d(TAG,"Location took: "+delta+" milliseconds");
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
	
	/**
	 * AsyncTask that will contact Google's server to request a reverse geocode
	 * asynchronously.
	 * 
	 * @author Nelson R. Perez - bilthon@gmail.com
	 */
	private class ReverseGeocodeLookupTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			String currentAddress = "";
			if(mCurrentLocation != null){
				Geocoder geocoder = new Geocoder(MainActivity.this,
						new Locale("en","US"));
				try {
					List<Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(), 
							mCurrentLocation.getLongitude(), 
							10);
					
					SharedPreferences.Editor editor = getSharedPreferences(KEY_PREFERENCES,Context.MODE_PRIVATE).edit();
					String city = null;
					String state = null;
					String country = null;
					for(Address address : addressList){
						if(city == null && address.getLocality() != null)
							city = address.getLocality();
						if(state == null && address.getAdminArea() != null)
							state = address.getAdminArea();
						if(country == null && address.getCountryName() != null)
							country = address.getCountryName();
						
						if(city != null && state != null && country != null) break;
					}
					editor.putString(KEY_CITY, city);
					editor.putString(KEY_STATE, state);
					editor.putString(KEY_COUNTRY, country);
					editor.commit();
					
				}catch(IllegalArgumentException e){
					Log.e(TAG,"IllegalArgumentException caught while getting location from Geocoder");
					Log.e(TAG,"Msg: "+e.getMessage());
				}catch (IOException e) {
					Log.e(TAG,"IOException caught while getting location from Geocoder");
					Log.e(TAG,"Msg: "+e.getMessage());
				}
			}
			return currentAddress;
		}
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	GameSound.stopMusic();
    }
    
    @Override
    public void onResume() {
    	super.onPause();
    	GameSound.playMusic(getApplicationContext(), mMusicResource);
    }

	@SuppressLint("ParserError")
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy");
		GameSound.releaseMediaPlayer();
		super.onDestroy();
	}
}