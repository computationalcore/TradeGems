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
package com.luminia.tradegems;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
//import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
//import android.widget.ProgressBar;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;


import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.luminia.tradegems.PauseDialog;
import com.luminia.tradegems.GameView;

/**
 * This Class represents the Main Game Activity.
 *
 * @version 	1.00 18 Fev 2012
 * @author 	Vinicius Busquet - computationalcore@gmail.com
 */
public class GameActivity extends Activity implements OnClickListener, AdListener {

	private final static int PAUSE_DIALOG = 1;
	private AdView adView;	
	private static final String TAG = "GameActivity";
	
	//creates a ViewSwitcher object, to switch between Views
	//Used by to show a loading screen before game start
	private ViewSwitcher viewSwitcher;

	
	private TextView mTurns;
	private TextView mScore;
	private TextView mScoreMultiplier;
	private TextView mTimer;
	private Button mPauseButton;
	private GameView mGame;
//	private boolean mGamePaused = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		
		this.init();
	}
	
	public void init(){
		setContentView(R.layout.game);
		mTurns = (TextView) findViewById(R.id.turns);
		mScore = (TextView) findViewById(R.id.score);
		mScoreMultiplier = (TextView) findViewById(R.id.score_multiplier);
		mTimer = (TextView) findViewById(R.id.countdown_timer);
		mPauseButton = (Button) findViewById(R.id.pause_button);
		
		
		mGame = (GameView) findViewById(R.id.gameView);
		
		//Start Game
		GameActivity.this.mPauseButton.setOnClickListener(GameActivity.this);
		GameActivity.this.mGame.reset(GameActivity.this);
		GameActivity.this.mGame.startGame();
		setupAd();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View view) {

		if (view == mPauseButton) {
			mGame.pause();
			
			//to prevent user pause to see the game (and think)
			mGame.setVisibility(View.INVISIBLE);
			showDialog(PAUSE_DIALOG);
			//mActivity.dialogClosed();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == PAUSE_DIALOG) {
			return new PauseDialog(this);
		} else {
			return null;
		}
	}

	public void dialogClosed() {
		//mGame.reset(this);
		//Show the game board again
		mGame.setVisibility(View.VISIBLE);
		mGame.resume();
	}
	
	/**
	 * This method update the UI Score and Turns. 
	 *  @param  score  a long value that represents the current user Score Points
	 *  @param  scoreMultiplier  a long value that represents the current Score Multiplier
	 *  @param  turns a long value that represents the number of turns(movements) of the user
	 */
	public void updateValues(long score, long scoreMultiplier, long turns) {
		mScore.setText("" + score);
		if (scoreMultiplier > 1) mScoreMultiplier.setText(""+scoreMultiplier+"x");
		else mScoreMultiplier.setText("  ");
		mTurns.setText("" + turns + "  ");
	}
	
	public void lastSecond(Boolean state) {	
		if (state) {
			mTimer.setTextColor(Color.RED);
		}
		else{
			mTimer.setTextColor(Color.GREEN);
		}
	}
	
	/**
	 * This method update the UI Timer. 
	 *   @param  time  an int value that represent the current time in miliseconds
	 */
	public void updateTimer(long time) {
		int seconds = (int) (time / 1000);
	    long minutes = seconds / 60;
	    seconds     = seconds % 60;
	    
	    //Format the string to be set on the label correctly
	    String strMinutes = "";
	    String strSeconds="";
	    if (minutes > 0 ) {
	    	strMinutes = "" +minutes+":";
	    }
	    if (seconds < 10) {
	    strSeconds = "0"+seconds;
	    } else {
	    	strSeconds = ""+seconds;            
	    }
	    
	    //Set the timer the CountDown Timer label
		mTimer.setText(strMinutes+strSeconds);		
	}

	public Long getScore() {
		return Long.parseLong(mScore.getText().toString());
	}

	public void endGame() {
		Intent intent = new Intent(this, ScoreActivity.class);
		intent.putExtra(MainActivity.KEY_SCORE,mScore.getText());
		intent.putExtra(MainActivity.KEY_TURN,mTurns.getText());
		startActivity(intent);
		this.finish();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}
	
    @Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onDestroy() {
    	Log.i(TAG,"onDestroy");
		super.onDestroy();
//		mGame.release();
	}

	//Override the default back key behavior
    @Override
    public void onBackPressed() 
    {
    	//Finishes the current Activity
    	super.onBackPressed();
//    	mGame.release();
    }       
    
    @Override
    public void onPause() {
    	super.onPause();
    	if ( !(mGame.getState() == GameView.STOPPED) ) {
    		mGame.pause();
    	}    	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
  	    if (mGame.getState() == GameView.PAUSED) {
       	    mGame.resume();	
   	    }
    }
    
    private void setupAd(){
    	FrameLayout adContainer = (FrameLayout) findViewById(R.id.AdContainer);
    	if(adContainer != null){
    		double random = Math.random();
    		if(random > 0.66){ // Nelson
    			adView = new AdView(this,AdSize.BANNER,"a14e49e81425161"); 
    		}else if(random > 0.33){ // Leticia
    			adView = new AdView(this,AdSize.BANNER,"a14fadb95426072");    			
    		}else{ // Vinicius
    			adView = new AdView(this,AdSize.BANNER,"a14fadb121c9a8d");    			
    		}
    		if(adView != null){
    			adContainer.addView(adView);
        		adView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        		if(adView instanceof AdView) ((AdView)adView).setAdListener(this);
        		setupAndLoadAd();
    		}
    	}
    }
    
    /**
     * Method that creates an AdRequest object and loads a new ad
     */
    private void setupAndLoadAd(){
		// Creating an ad request and sending it
		AdRequest request = new AdRequest();
		request.addKeyword("game");
		request.addKeyword("bored");
		adView.loadAd(request);
		Log.d(TAG,"isRefreshing: "+((AdView)adView).isRefreshing());    	    		
    }

	@Override
	public void onDismissScreen(Ad ad) {
		EasyTracker.getTracker().trackEvent(Analytics.Category.AD, Analytics.Actions.ADMOB_ACTION, Analytics.Labels.AD_DISMISSED,1l);		
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		EasyTracker.getTracker().trackEvent(Analytics.Category.AD, Analytics.Actions.ADMOB_ACTION, Analytics.Labels.AD_FAILED,1l);
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveAd(Ad ad) {
		EasyTracker.getTracker().trackEvent(Analytics.Category.AD, Analytics.Actions.ADMOB_ACTION, Analytics.Labels.AD_RECEIVED,1l);
	}
}
