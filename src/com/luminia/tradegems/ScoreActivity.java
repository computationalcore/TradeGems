package com.luminia.tradegems;

import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
	private static final String TAG = "ScoreActivity";
	
	private Button mConfirm;
	private Long mScore;
	private String mNickname;
	
	private MyDBAdapter mDBAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.score);

		Intent intent = getIntent();
		String score = intent.getStringExtra("score");
		mScore = new Long(score);
		checkScore(new Score(mScore));
		
		mCurrentScore = (TextView) findViewById(R.id.current_score);
		mCurrentScore.setText(score);
	}
	
	private void checkScore(Score currentScore){
		this.mDBAdapter = MyDBAdapter.getInstance(this);
		Score prevScore = mDBAdapter.getHighestScore(mDBAdapter.getDefaultAccount());
		Log.i(TAG,"prevScore: "+prevScore.getScore().longValue()+", currentScore: "+currentScore.getScore().longValue());
		if(currentScore.getScore() > prevScore.getScore() 
				&& currentScore.getScore() > 0){
			this.setContentView(R.layout.highest_score);
			mConfirm = (Button) findViewById(R.id.confirmButton);
			mConfirm.setOnClickListener(this);
			if(!hasNickname()){
				showEnterNicknameDialog();
			}else{
				Log.d(TAG,"Already have a nickname, which is: "+this.mNickname);
			}
		}else{
			this.setContentView(R.layout.regular_score);
		}
	}
	
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
		if(mNickname == null || mNickname.equals("bil"))
			return false;
		else
			return true;
	}

	@Override
	public void onClick(View view) {
		HighScore newScore = makeHighScore();
//		updateLocalHighScore(newScore);

		if (view == mConfirm) {
//			new ReportScore().execute(newScore);
			this.mDBAdapter = MyDBAdapter.getInstance(this);
			mDBAdapter.addScore(newScore);
			//mActivity.dialogClosed();
			finish();
		} else {
			//mActivity.dialogClosed();
		}
	}

//	private void updateLocalHighScore(HighScore newScore) {
//		try {
//
//			SharedPreferences settings = getBaseContext().getSharedPreferences(
//					HighScoreView.PREF_GAME, 0);
//			String json = settings.getString(HighScoreView.PREF_HIGH_SCORE,
//					HighScore.createDefaultScores());
//
//			JSONArray currentScores = new JSONArray(json);
//
//			List<HighScore> highscores = HighScore.toList(currentScores);
//			highscores.add(newScore);
//
//			JSONArray updatedScores = HighScore.toJSONArray(highscores);
//			Editor editor = settings.edit();
//
//			editor.putString(HighScoreView.PREF_HIGH_SCORE,
//					updatedScores.toString());
//			editor.putString(PREF_USER_NAME, newScore.getAccountName());
//
//			editor.commit();
//
//		} catch (JSONException e) {
//			throw new RuntimeException(e);
//		}
//	}

	private HighScore makeHighScore() {
		this.mDBAdapter = MyDBAdapter.getInstance(this);
		HighScore score = new HighScore();
		score.setAccountName(mDBAdapter.getDefaultAccount().getEmail());
		score.setDate(System.currentTimeMillis());
		score.setScore(Long.parseLong(mCurrentScore.getText().toString()));

		LocationManager locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);

		ListIterator<String> li = providers.listIterator();
		while (li.hasNext()) {
			String provider = li.next();
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				score.setLatitude(location.getLatitude());
				score.setLongitude(location.getLongitude());
				break;
			}
		}
		score.setGameName( getString(R.string.app_name) );

		return score;
	}

	private class ReportScore extends AsyncTask<HighScore, Integer, HighScore> {

		@Override
		protected HighScore doInBackground(HighScore... highscores) {
			try {
				DefaultHttpClient client = new DefaultHttpClient();

				StringBuilder fullUrl = new StringBuilder(SERVICE_URL);

				HighScore highScore = highscores[0];
				JSONObject jsonObject = highScore.toJSONObject();
				String jsonStr = jsonObject.toString();

				fullUrl.append(URLEncoder.encode(jsonStr));

				HttpGet get = new HttpGet(fullUrl.toString());
				HttpResponse response = client.execute(get);

				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					if(json.length() > 0)
						return new HighScore(new JSONObject(json));
					else
						return null;
				} else {
					String reason = response.getStatusLine().getReasonPhrase();
					throw new RuntimeException("Trouble adding score(code="
							+ statusCode + "):" + reason);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
