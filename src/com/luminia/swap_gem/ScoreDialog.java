/*
 * @(#)ScoreDialog.java        1.00 12/02/18
 *
 * Copyright (c) 2012 Luminia Software Inc.
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Luminia
 * Software, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Luminia.
 */
package com.luminia.swap_gem;

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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * This Class represents the Score Dialog of the game,
 * where the user can share his score with other people
 * in Luminia Cloud.
 *
 * @version 	1.10 19 Fev 2012
 * @author 	Vinicius Busquet - computationalcore@gmail.com
 * 
 * @see android.view.View.OnClickListener
 */
public class ScoreDialog extends Dialog implements OnClickListener {
	
	public final static String PREF_USER_NAME = "PREF_USER_NAME";
	public final static String SERVICE_URL = "http://luminia-games.appspot.com/add_high_score?highscore=";
	
	private EditText mPlayerName;
	private Button mConfirm;
	private Button mCancel;	
	
	private GameActivity mActivity;
	
	public ScoreDialog(GameActivity activity) {
		super(activity);
		this.mActivity = activity;

		setContentView(R.layout.score_dialog);
		
		//Set the dialog title
		setTitle( mActivity.getString(R.string.high_score) );

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

		ImageView imageView = new ImageView(activity);
		//imageView.setImageDrawable(bitmapDrawable);

		rootLayout.addView(imageView);

	}
	
	@Override
	public void onClick(View view) {
		HighScore newScore = makeHighScore();
		updateLocalHighScore(newScore);

		if (view == mConfirm) {
			new ReportScore().execute(newScore);
			dismiss();
			mActivity.dialogClosed();
		} else {
			dismiss();
			mActivity.dialogClosed();
		}
	}

	private void updateLocalHighScore(HighScore newScore) {
		try {

			SharedPreferences settings = getContext().getSharedPreferences(
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
		score.setScore(mActivity.getScore());

		LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
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
		score.setGameName( mActivity.getString(R.string.app_name) );

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