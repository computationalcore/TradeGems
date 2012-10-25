package com.luminia.tradegems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.luminia.tradegems.database.MyDBAdapter;
import com.luminia.tradegems.database.Score;
import com.luminia.tradegems.widgets.SelectNicknameDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences.Editor;


public class ScoreActivity extends FragmentActivity implements OnClickListener {
	
	
	public final static String PREF_USER_NAME = "PREF_USER_NAME";
	private TextView mCurrentScore;
	
	public final static String SERVICE_URL = "http://trade-gems.appspot.com/add_high_score?highscore=";
	public final static String PUBLISH_URL = "http://trade-gems.appspot.com/publish";
	private static final String TAG = "ScoreActivity";
	
	// UI references
	private Button mConfirm;
	
	// Data to send
	private String mEmail;
	private String mNickname;
	private Long mScore;
	private Integer mTurn;
	private Float mLat = null;
	private Float mLon = null;
	private String mCity;
	private String mState;
	private String mCountry;
	private String mProvider;
	private Float mAccuracy;
	
	private MyDBAdapter mDBAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String score = intent.getStringExtra(MainActivity.KEY_SCORE);
		String turns = intent.getStringExtra(MainActivity.KEY_TURN);
		mScore = Long.valueOf(score.trim());
		mTurn = Integer.valueOf(turns.trim());

		checkScore(new Score(mScore));
		
		mCurrentScore = (TextView) findViewById(R.id.current_score);
		mCurrentScore.setText(score);
	}
	
	/**
	 * This method checks the current score and determines whether to use the
	 * layout for the top score (with the option to send the score to the cloud) 
	 * or the alternate layout just displaying it.
	 * 
	 * This decision is made after checking the local database for the currently top
	 * score of this user.
	 * 
	 * @param currentScore Score object with the current score
	 */
	private void checkScore(Score currentScore){
		this.mDBAdapter = MyDBAdapter.getInstance(this);
		Score prevScore = mDBAdapter.getHighestScore(mDBAdapter.getDefaultAccount());

		if(currentScore.getScore() > prevScore.getScore() 
				&& currentScore.getScore() > 0){
			// If this score is the highest ever for the current user
			this.setContentView(R.layout.highest_score);
			mConfirm = (Button) findViewById(R.id.confirmButton);
			mConfirm.setOnClickListener(this);
			if(!hasNickname()){
				showEnterNicknameDialog();
			}else{
				Log.d(TAG,"Already have a nickname, which is: "+this.mNickname);
			}
		}else{
			// If is just a regular score
			this.setContentView(R.layout.regular_score);
		}
	}
	
	/**
	 * Method that will display a dialog prompting the user to enter a user name.
	 * If he refuses, no problem as a default one will be filled later on
	 */
	private void showEnterNicknameDialog() {
		SelectNicknameDialog selectNicknameDialog = new SelectNicknameDialog();
    	Fragment prev = this.getSupportFragmentManager().findFragmentByTag("dialog");

    	// Removing any currently shown dialog
    	FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
    	if (prev != null) {
            ft.remove(prev);
        }
    	selectNicknameDialog.show(getSupportFragmentManager(), "nickname-tag");
	}

	/**
	 * Private method that checks the preferences to see if there is a registered
	 * nickname
	 * @return true if there is a nickname, false otherwise
	 */
	private boolean hasNickname() {
		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		mNickname = sharedPreferences.getString(MainActivity.KEY_NICKNAME,"");
		if(mNickname == null || mNickname.equals(""))
			return false;
		else
			return true;
	}

	@Override
	public void onClick(View view) {
		TopScoreReport newScore = makeScoreReport();

		if (view == mConfirm) {
			new ReportScore().execute(newScore);
			this.mDBAdapter = MyDBAdapter.getInstance(this);
			mDBAdapter.addScore(newScore);
			finish();
		} else {
			
		}
	}

	/**
	 * Creates a new TopScoreReport
	 * @return a newly created TopScoreRecord with the default user account and the score
	 */
	private TopScoreReport makeScoreReport() {
		this.mDBAdapter = MyDBAdapter.getInstance(this);		
		TopScoreReport score = new TopScoreReport();
		score.setAccountName(mDBAdapter.getDefaultAccount().getEmail());
		score.setScore(Long.parseLong(mCurrentScore.getText().toString()));
		return score;
	}
	
	private void checkPostFields() {
		SharedPreferences pref = getSharedPreferences(MainActivity.KEY_PREFERENCES,Context.MODE_PRIVATE);
		if(mNickname == null || mNickname == "")
			mNickname = pref.getString(MainActivity.KEY_NICKNAME,"anon");
		if(mLat == null)
			mLat = pref.getFloat(MainActivity.KEY_LATITUDE,0.0f);
		if(mLon == null)
			mLon = pref.getFloat(MainActivity.KEY_LONGITUDE, 0.0f);
		if(mCity == null || mCity == "")
			mCity = pref.getString(MainActivity.KEY_CITY,"Gotham City");
		if(mState == null)
			mState = pref.getString(MainActivity.KEY_STATE,"Fake State");
		if(mCountry == null || mCountry == "")
			mCountry = pref.getString(MainActivity.KEY_COUNTRY,"Banana Republic");
		if(mProvider == null || mProvider == "")
			mProvider = pref.getString(MainActivity.KEY_PROVIDER,"Telepathy");
		if(mAccuracy == null || mAccuracy == 0.0f)
			mAccuracy = pref.getFloat(MainActivity.KEY_ACCURACY,Float.MAX_VALUE);
		if(mEmail == null || mEmail == "")
			mEmail = mDBAdapter.getDefaultAccount().getEmail();
	}


	private class ReportScore extends AsyncTask<TopScoreReport, Integer, TopScoreReport> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			checkPostFields();
		}

		@Override
		protected TopScoreReport doInBackground(TopScoreReport... highscores) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(PUBLISH_URL);
			try{
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_NICKNAME,mNickname));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_EMAIL,mEmail));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_SCORE,mScore.toString()));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_TURN,mTurn.toString()));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_LATITUDE,mLat.toString()));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_LONGITUDE,mLon.toString()));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_CITY,mCity));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_STATE,mState));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_COUNTRY,mCountry));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_PROVIDER,mProvider));
				nameValuePairs.add(new BasicNameValuePair(MainActivity.KEY_ACCURACY,mAccuracy.toString()));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				Log.d(TAG,"nickname: "+mNickname);
				Log.d(TAG,"email: "+mEmail);
				Log.d(TAG,"score: "+mScore.toString());
				Log.d(TAG,"turns: "+mTurn.toString());
				Log.d(TAG,"lat: "+mLat.toString());
				Log.d(TAG,"lon: "+mLon.toString());
				Log.d(TAG,"city: "+mCity);
				Log.d(TAG,"state: "+mState);
				Log.d(TAG,"country: "+mCountry);
				Log.d(TAG,"provider: "+mProvider);
				Log.d(TAG,"accuracy: "+mAccuracy);
				Log.d(TAG,"Inspecting http post entity");
				BufferedReader r = new BufferedReader(new InputStreamReader(httpPost.getEntity().getContent()));
				for(String line = r.readLine(); line != null; line = r.readLine()){
					Log.d(TAG,"Line: "+line);
				}
				
				HttpResponse response = client.execute(httpPost);
				Log.d(TAG,"Response: "+response.getStatusLine());
				InputStreamReader ir = new InputStreamReader(response.getEntity().getContent());
				BufferedReader reader = new BufferedReader(ir);
				for(String line = reader.readLine(); line != null; line = reader.readLine()){
					Log.d(TAG,"Line: "+line);
				}
//				if(response.getStatusLine().getStatusCode() == 200){
//					Log.d(TAG,"")
//				}
			}catch(ClientProtocolException e){
				Log.e(TAG,"ClientProtocolException caught");
				Log.e(TAG,"Msg: "+e.getMessage());
			}catch(IOException e){
				Log.e(TAG,"IOException caught");
				Log.e(TAG,"Msg: "+e.getMessage());
			}
			return null;
		}
	}
}