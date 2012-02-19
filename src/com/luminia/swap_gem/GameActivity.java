/*
 * @(#)GameActivity.java        1.00 12/02/18
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

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * This Class represents the Main Game Activity.
 *
 * @version 	1.00 18 Fev 2012
 * @author 	Vinicius Busquet - computationalcore@gmail.com
 */
public class GameActivity extends Activity {

	private final static int DIALOG_CONFIRM_SHARE = 10;

	private TextView mTurns;
	private TextView mScore;
	private GameView mGame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title from activity - Change this to XML
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.game);

		mTurns = (TextView) findViewById(R.id.turns);
		mScore = (TextView) findViewById(R.id.score);

		mGame = (GameView) findViewById(R.id.gameView);

		mGame.reset(this);
	}

	/**
	 * This method update the UI Score and Turns. 
	 *   
	                          
	@param  score  an int value that represent the current user Score Points
	 *  
	                          
	@param  turns an int value that represent the number of turns(movements) of the user
	 */
	public void updateValues(int score, int turns) {
		mScore.setText("" + score);
		mTurns.setText("" + turns + "  ");
	}

	public Long getScore() {
		return Long.parseLong(mScore.getText().toString());
	}

	public void endGame() {
		showDialog(DIALOG_CONFIRM_SHARE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONFIRM_SHARE) {
			return new ScoreDialog(this);
		} else {
			return null;
		}
	}

	public void dialogClosed() {
		mGame.reset(this);
	}
}
