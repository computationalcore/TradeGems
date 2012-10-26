/**
 * @(#)HighScoreView.java        1.00 12/02/19
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

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

/**
* This class is responsible for reading the current high score from the user
* preferences and then drawing them on the screen.
*
* @version 	1.00 19 Fev 2012
* 
* @author 	Vinicius Busquet - computationalcore@gmail.com
* 
*/
public class HighScoreView extends View {

	public final static String PREF_GAME = "PREF_GAME";
	public final static String PREF_HIGH_SCORE = "PREF_HIGH_SCORE";

	private List<TopScoreReport> highscores;

	public HighScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HighScoreView(Context context) {
		super(context);
		init();
	}

	private void init() {
		SharedPreferences settings = getContext().getSharedPreferences(
				PREF_GAME, 0);
		String json = settings.getString(PREF_HIGH_SCORE,
				TopScoreReport.createDefaultScores());
		try {
			JSONArray jsonArray = new JSONArray(json);
//			highscores = TopScoreReport.toList(jsonArray);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		//Draw Background
		canvas.drawColor(Color.GRAY);

		Rect innerRect = new Rect(5, 5, width - 5, height - 5);
		Paint innerPaint = new Paint();
		LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
				Color.LTGRAY, Color.DKGRAY, Shader.TileMode.MIRROR);
		innerPaint.setShader(linearGradient);

		canvas.drawRect(innerRect, innerPaint);

		//Draw Title
		Path titlePath = new Path();
		titlePath.moveTo(10, 70);
		titlePath.cubicTo(width / 3, 90, width / 3 * 2, 50, width - 10, 70);

		Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setColor(Color.RED);
		titlePaint.setTextSize(38);
		titlePaint.setShadowLayer(5, 0, 5, Color.BLACK);

		canvas.drawTextOnPath("Your High Scores", titlePath, 0, 0, titlePaint);

		//Draw Line
		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(10);
		linePaint.setColor(Color.WHITE);
		linePaint.setStrokeCap(Cap.ROUND);

		float[] direction = new float[] { 0, -5, -5 };
		EmbossMaskFilter maskFilter = new EmbossMaskFilter(direction, .5f, 8, 3);
		linePaint.setMaskFilter(maskFilter);

		canvas.drawLine(15, 100, width - 15, 100, linePaint);

		//Draw Scores
		Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scorePaint.setShadowLayer(5, 0, 5, Color.BLACK);
		scorePaint.setTextSize(20);

		RadialGradient radialGradient = new RadialGradient(width / 2,
				height / 2, width, Color.WHITE, Color.GREEN, TileMode.MIRROR);
		scorePaint.setShader(radialGradient);

		int index = 0;
		for (TopScoreReport score : highscores) {
//			canvas.drawText(score.getAccountName(), 40, 150 + index * 30,scorePaint);
			canvas.drawText("" + score.getScore(), width - 115,
					150 + index * 30, scorePaint);

			index++;
		}

	}
}
