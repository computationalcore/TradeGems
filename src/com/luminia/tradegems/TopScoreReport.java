/*
 * @(#)HighScore.java        1.00 12/02/19
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
package com.luminia.tradegems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.luminia.tradegems.database.Score;

public class TopScoreReport extends Score {

	private String nickname;
	private String city;
	private String state;
	private String country;
	private String provider;
	private Double longitude;
	private Double latitude;

	public TopScoreReport() {

	}

	public TopScoreReport(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("nickname")) {
			accountname = jsonObject.getString("nickname");
		}
		if (jsonObject.has("score")) {
			score = jsonObject.getLong("score");
		}
		if (jsonObject.has("longitude")) {
			longitude = jsonObject.getDouble("lon");
		}
		if (jsonObject.has("latitude")) {
			latitude = jsonObject.getDouble("lat");
		}
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject result = new JSONObject();
		result.put("accountname", accountname);
		result.put("score", score);
		result.put("longitude", longitude);
		result.put("latitude", latitude);
		return result;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public static String createDefaultScores() {
		try {
			JSONArray result = new JSONArray();
			for (int i = 0; i < 10; i++) {
				TopScoreReport highScore = new TopScoreReport();
				highScore.setAccountName("No Score");
				highScore.setScore((long) i);
				result.put(highScore.toJSONObject());
			}

			return result.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<TopScoreReport> toList(JSONArray jsonArray) {
		try {
			List<TopScoreReport> result = new ArrayList<TopScoreReport>();
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				TopScoreReport score = new TopScoreReport(jsonObject);
				result.add(score);
			}

			Collections.sort(result, new HighScoreComparator());
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONArray toJSONArray(List<TopScoreReport> highscores) {
		try {
			JSONArray result = new JSONArray();
			Collections.sort(highscores, new HighScoreComparator());

			while (highscores.size() > 10) {
				highscores.remove(10);
			}

			for (TopScoreReport score : highscores) {
				result.put(score.toJSONObject());
			}

			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static class HighScoreComparator implements Comparator<TopScoreReport> {

		@Override
		public int compare(TopScoreReport score1, TopScoreReport score2) {
			return score2.getScore().compareTo(score1.getScore());//Descending order
		}

	}

}
