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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences.Editor;


public class ScoreActivity extends Activity implements OnClickListener {
	
	
	public final static String PREF_USER_NAME = "PREF_USER_NAME";
	private TextView mCurrentScore;
	
	public final static String SERVICE_URL = "http://trade-gems.appspot.com/add_high_score?highscore=";
	
	private EditText mPlayerName;
	private Button mConfirm;
	private Button mCancel;	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score);
		
		mCurrentScore = (TextView) findViewById(R.id.current_score);
		Intent intent = getIntent();
		String score = intent.getStringExtra("score");
		mCurrentScore.setText(score);
		//mSound = new GameSound(getContext());
		//mSound.playEnd();
		
		mPlayerName = (EditText) findViewById(R.id.playerName);
		mConfirm = (Button) findViewById(R.id.confirmButton);
		mCancel = (Button) findViewById(R.id.confirmButton);

		//Get user previous data, or use a default value if it is empty
		//and fill the playerName EditText field
		//SharedPreferences settings = getContext().getSharedPreferences(
		//		HighScoreView.PREFS_ORB_QUEST, 0);
		//String username = settings.getString(PREF_USER_NAME, mActivity.getString(R.string.username) );
		//mPlayerName.setText(username);

		mConfirm.setOnClickListener(this);
		mCancel.setOnClickListener(this);

		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.dialogRoot);

		//BitmapDrawable bitmapDrawable = (BitmapDrawable) mActivity.getResources().getDrawable(R.drawable.dialog_graphic);

		
		
	}
	
	@Override
	public void onClick(View view) {
		HighScore newScore = makeHighScore();
		updateLocalHighScore(newScore);

		if (view == mConfirm) {
			new ReportScore().execute(newScore);
			//mActivity.dialogClosed();
		} else {
			//mActivity.dialogClosed();
		}
	}

	private void updateLocalHighScore(HighScore newScore) {
		try {

			SharedPreferences settings = getBaseContext().getSharedPreferences(
					HighScoreView.PREF_GAME, 0);
			String json = settings.getString(HighScoreView.PREF_HIGH_SCORE,
					HighScore.createDefaultScores());

			JSONArray currentScores = new JSONArray(json);

			List<HighScore> highscores = HighScore.toList(currentScores);
			highscores.add(newScore);

			JSONArray updatedScores = HighScore.toJSONArray(highscores);
			Editor editor = settings.edit();

			editor.putString(HighScoreView.PREF_HIGH_SCORE,
					updatedScores.toString());
			editor.putString(PREF_USER_NAME, newScore.getUsername());

			editor.commit();

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private HighScore makeHighScore() {
		HighScore score = new HighScore();
		score.setUsername(mPlayerName.getEditableText().toString());
		score.setDate(System.currentTimeMillis());
		score.setScore(Long.parseLong(mCurrentScore.toString()));

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
					return new HighScore(new JSONObject(json));
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
