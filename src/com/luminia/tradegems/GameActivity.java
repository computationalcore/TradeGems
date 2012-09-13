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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;


import com.luminia.tradegems.PauseDialog;
import com.luminia.tradegems.GameView;

/**
 * This Class represents the Main Game Activity.
 *
 * @version 	1.00 18 Fev 2012
 * @author 	Vinicius Busquet - computationalcore@gmail.com
 */
public class GameActivity extends Activity implements OnClickListener {

	private final static int PAUSE_DIALOG = 1;
	
	//creates a ViewSwitcher object, to switch between Views
	//Used by to show a loading screen before game start
	private ViewSwitcher viewSwitcher;

	
	private TextView mTurns;
	private TextView mScore;
	private TextView mTimer;
	private Button mPauseButton;
	private GameView mGame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initialize a LoadViewTask object and call the execute() method
		//This is a subclass of this class
        new LoadViewTask().execute();
	}
	
	public void init(){
		mTurns = (TextView) findViewById(R.id.turns);
		mScore = (TextView) findViewById(R.id.score);
		mTimer = (TextView) findViewById(R.id.countdown_timer);
		mPauseButton = (Button) findViewById(R.id.pause_button);

		mPauseButton.setOnClickListener(this);
		
		mGame = (GameView) findViewById(R.id.gameView);

		mGame.reset(this);
		mGame.startGame();
	}
	
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
	
	//To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
    	//A TextView object and a ProgressBar object
    	private TextView tv_progress;
    	private ProgressBar pb_progressBar;
    	
    	//Before running code in the separate thread
		@Override
		protected void onPreExecute() 
		{
			//Initialize the ViewSwitcher object
	        viewSwitcher = new ViewSwitcher(GameActivity.this);
	        /* Initialize the loading screen with data from the 
	         * 'loadingscreen.xml' layout xml file. 
	         * Add the initialized View to the viewSwitcher.*/
			viewSwitcher.addView(ViewSwitcher.inflate(GameActivity.this, R.layout.loading_screen, null));
			
			//Initialize the TextView and ProgressBar instances - 
			//IMPORTANT: call findViewById() from viewSwitcher.
			tv_progress = (TextView) viewSwitcher.findViewById(R.id.tv_progress);
			pb_progressBar = (ProgressBar) viewSwitcher.findViewById(R.id.pb_progressbar);
			//Sets the maximum value of the progress bar to 100 			
			pb_progressBar.setMax(100);
			
			//Set ViewSwitcher instance as the current View.
			setContentView(viewSwitcher);
			
			/* Initialize the application's main interface from the 'main.xml' layout xml file. 
	         * Add the initialized View to the viewSwitcher.*/
			viewSwitcher.addView(ViewSwitcher.inflate(GameActivity.this, R.layout.game, null));

		}

		//The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) 
		{
			/* This is just a code that delays the thread execution 4 times, 
			 * during 850 milliseconds and updates the current progress. This 
			 * is where the code that is going to be executed on a background
			 * thread must be placed. 
			 */			
			try 
			{
				//Get the current thread's token
				synchronized (this) 
				{
					//Initialize an integer (that will act as a counter) to zero
					int counter = 0;
					//While the counter is smaller than four
					while(counter <= 5)
					{
						//Wait 850 milliseconds
						this.wait(200);
						//Increment the counter 
						counter++;
						//Set the current progress. 
						//This value is going to be passed to the onProgressUpdate() method.
						publishProgress(counter*25);
					}
				}
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			return null;
		}

		//Update the TextView and the progress at progress bar
		@Override
		protected void onProgressUpdate(Integer... values) 
		{
			//Update the progress at the UI if progress value is smaller than 100
			if(values[0] <= 100)
			{
				tv_progress.setText("Progress: " + Integer.toString(values[0]) + "%");
				pb_progressBar.setProgress(values[0]);
			}
		}
		
		//After executing the code in the thread
		@Override
		protected void onPostExecute(Void result) 
		{
			//Switch the Views
			viewSwitcher.showNext();
			GameActivity.this.init();
		}
    }
	

	/**
	 * This method update the UI Score and Turns. 
	 *  @param  score  an int value that representing the current user Score Points
	 *  @param  turns an int value that represent the number of turns(movements) of the user
	 */
	public void updateValues(long score, long turns) {
		mScore.setText("" + score);
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
		Intent intent = new Intent(GameActivity.this, ScoreActivity.class);
		intent.putExtra("score",mScore.getText());
		intent.putExtra("turns",mTurns.getText());
		startActivity(intent);
		this.finish();
	}
	
    //Override the default back key behavior
    @Override
    public void onBackPressed() 
    {
    	//Emulate the progressDialog.setCancelable(false) behavior
    	//If the first view is being shown
    	if(viewSwitcher.getDisplayedChild() == 0)
    	{
    		//Do nothing
    		return;
    	}
    	else
    	{
    		//Finishes the current Activity
    		super.onBackPressed();
    		mGame.release();
    	}
    }               
	
}
