/*
 * @(#)GameView.java        1.00 12/02/18
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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
* This Class represents the Main Game Controller.
*
* @version 	1.00 18 Fev 2012
* @author 	Vinicius Busquet - computationalcore@gmail.com
* 
* @see android.view.ViewGroup
* @see android.view.View.OnClickListener
* 
*/
public class GameView extends ViewGroup implements OnClickListener {

	
	//Time of the update CountDownTimer from the UI (in milliseconds)
	private static final int UPDATE_UI_TIMER = 100;

	private static final String TAG = "GameView";
	
	//Array that stores the IDs of three different gem images 
	//(they are set in the init() method
	private int mGemIds[] = new int[3];
	
	//Number of rows of the game
	private int numRows = 6;
	//Number of columns of the game
	private int numColumns = 6; 
	
	private Random mRandom = new Random();

	private GemView mSelectedGemView = null;
	
	//The value of this variable will never be cached thread-locally: all reads and writes will go straight to "main memory";
	//Access to the variable acts as though it is enclosed in a synchronized block, synchronized on itself.
	private volatile boolean mAcceptInput = true;

	//Current score (the ideal was use a unsigned long, but java do not
	//have this, so long is good 2^63)
	private long mScore = 0;
	
	//Number of turns (the ideal was use a unsigned long, but java do not
	//have this, so long is good 2^63)
	private long mTurns = 0;
	
	//Number of remaining time (in milisecods
	//Start with 1 min 
	//TODO: Set this back to 60 seconds. 1000 miliseconds is just for debug
	// Nelson R. Perez
	private long mGameTimer = 1000;

	//CountDown Timer object (the class is defined here)
	private GameCountDownTimer mGameCountDownTimer;
	
	//This reference is used to update the TextViews(Score and Turns) values of the activity
	private GameActivity mGameActivity;
	
	private GameSound mSound;
	
	private volatile Boolean isLastSeconds = false;
	
	/**
	 * This constructor is called when the View is instantiated from XML (Required)
	 */
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG,"GameView");
		init();
	}

	/**
	 * This constructor is required when the View is instantiated programmatically (Not used in this game)
	 */
	public GameView(Context context) {
		super(context);
		Log.i(TAG,"GameView");
		init();
	}

	/**
	 * Link the array of Gems if the IDs of the drawable,
	 * set the background, sounds, etc.
	 * Called inside the class constructor.  
	 */
	private void init() {
		Log.d(TAG,"init");
		mSound = new GameSound(this.getContext());
		
		Background background = new Background();
		background.setAlpha(65);
		setBackgroundDrawable(background);

		mGemIds[0] = R.drawable.red_gem;
		mGemIds[1] = R.drawable.green_gem;
		mGemIds[2] = R.drawable.blue_gem;
	}

	/**
	 * Release memory, stop all tasks and threads
	 */
	public void release() {
		mGameCountDownTimer.cancel();
		mSound.release();
	}
	
	public void pause() {
		mGameCountDownTimer.cancel();
		mSound.pauseGameMusic();
	}
	
	public void resume() {
		mGameCountDownTimer = new GameCountDownTimer(mGameTimer, UPDATE_UI_TIMER);
		mGameCountDownTimer.start();
		mSound.resumeGameMusic();
	}
	
	
	/**
	 * The method is called whenever a new game is started(including the first time)
	 * and is responsible for setting up the game state.
	 * 
	 * Beyond resetting the score and the number of turns, 25 GemViews are added as children 
	 * to the GameView, one of each element of the game matrix.
	 * 
	 * @param gameActivity The main activity of the game.
	 * 
	 */
	public void reset(GameActivity gameActivity) {
		mGameActivity = gameActivity;
		mScore = 0;
		mTurns = 0;
		mAcceptInput = true;

		removeAllViews();

		//Fill the board
		for (int c = 0; c < numRows; c++) {
			for (int r = 0; r < numColumns; r++) {
				GemView mGemView = new GemView(getContext(), c, r, mRandom.nextInt(3));
				addView(mGemView);
			}
		}
		mGameActivity.updateValues(mScore, mTurns);
		
		
		
		//At start check if any gem column matches
		requestLayout();
		checkMatches();
		mGameCountDownTimer = new GameCountDownTimer(mGameTimer, UPDATE_UI_TIMER);
		
	}
	
	public void startGame() {
		
		mGameCountDownTimer.start();
		mSound.playGameMusic();
	}
	
	public class GameCountDownTimer extends CountDownTimer{
		
		public GameCountDownTimer(long millisInFuture, long countDownInterval) {
	            super(millisInFuture, countDownInterval);
	    }
	 
	    @Override
	    public void onFinish() {
	    	
	    	mSound.stopLastSeconds();
	    	mSound.stopGameMusic();
	    	
	    	mSound.release();
	    	
	    	mGameActivity.endGame();
	    }
	 
	    @Override
	    public void onTick(long millisUntilFinished) {
	    	mGameTimer = millisUntilFinished;
	        mGameActivity.updateTimer(millisUntilFinished);
	        
	        if (mGameTimer <= 10000 && !isLastSeconds) {
	        	mSound.playLastSeconds();
	        	mGameActivity.lastSecond(true);
	        	isLastSeconds = true;
	        }
	        
	        if (mGameTimer >10000 && isLastSeconds) {
	        	mSound.stopLastSeconds();
	        	mGameActivity.lastSecond(false);
	        	isLastSeconds = false;
	        }
	    }
	}

	
	/**
	 * Override to make GameView square and to set a 20-pixel border(padding, aesthetics reason only).
	 * It is called beforethe GAmeView is laid out.
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		//The specs passed for both dimension is AT_MOST
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int size = Math.min(parentHeight - 20, parentWidth - 20);
		
		//Required, or it will cause a runtime exception.
		this.setMeasuredDimension(size, size);
	}
	
	/**
	 * 
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int size = getWidth();
		int oneFifth = size / numRows;

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			GemView gemView = (GemView) getChildAt(i);
			int left = oneFifth * gemView.getColumn();
			int top = oneFifth * gemView.getRow();
			int right = oneFifth * gemView.getColumn() + oneFifth;
			int bottom = oneFifth * gemView.getRow() + oneFifth;
			gemView.layout(left, top, right, bottom);
		}
	}

	/**
	 * This method is called when a user clicks (or touches) a gem. 
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		
		//This variable is set to false whenever there is an animation in progress
		//and set back to true  when the animation is over.
		if (mAcceptInput) {
			if (v instanceof GemView) {
				GemView gemView = (GemView) v;
				if (mSelectedGemView == null) {
					mSelectedGemView = gemView;
					Animation scale = AnimationUtils.loadAnimation(
							getContext(), R.anim.scale_down);

					gemView.startAnimation(scale);
				} else {
					/* If the user has selected the same orb twice, we want to scale the gem
					* back up and set the selectedGem to null. This basically allows
					* users to change their minds about which orb they want to swap.
					*/
					if (gemView != mSelectedGemView) {
						swapGems(mSelectedGemView, gemView);
						mSelectedGemView = null;
					} else {
						Animation scale = AnimationUtils.loadAnimation(
								getContext(), R.anim.scale_up);

						gemView.startAnimation(scale);
						mSelectedGemView = null;
					}

				}
			}
		}
	}

	
	/**
	 * 
	 * 
	 */
	protected void swapGems(GemView gem1, GemView gem2) {
		
		//Increment number of turns
		mTurns++;

		mAcceptInput = false;
		
		
		//swap locations
		int col1 = gem1.getColumn();
		int row1 = gem1.getRow();
		int col2 = gem2.getColumn();
		int row2 = gem2.getRow();

		gem1.setColumn(col2);
		gem1.setRow(row2);
		gem2.setColumn(col1);
		gem2.setRow(row1);

		//Animate Gem1
		TranslateAnimation trans1 = new TranslateAnimation(0, gem2.getLeft()
				- gem1.getLeft(), 0, gem2.getTop() - gem1.getTop());
		trans1.setDuration(250);
		trans1.setStartOffset(250);

		ScaleAnimation scaleUp1 = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp1.setDuration(250);
		scaleUp1.setStartOffset(500);

		AnimationSet set1 = new AnimationSet(false);
		set1.addAnimation(scaleUp1);
		set1.addAnimation(trans1);

		gem1.startAnimation(set1);

		//Animate gem2
		ScaleAnimation scaleDown2 = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleDown2.setDuration(250);
		scaleDown2.setInterpolator(new AnticipateOvershootInterpolator());
 
		
		TranslateAnimation trans2 = new TranslateAnimation(0, gem1.getLeft()
				- gem2.getLeft(), 0, gem1.getTop() - gem2.getTop());
		trans2.setDuration(250);
		trans2.setStartOffset(250);
		

		ScaleAnimation scaleUp2 = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp2.setDuration(250);
		scaleUp2.setStartOffset(500);

		AnimationSet set2 = new AnimationSet(false);
		set2.addAnimation(scaleDown2);
		set2.addAnimation(scaleUp2);
		set2.addAnimation(trans2);
		mSound.playSwap();
		
		set2.setAnimationListener(new RunAfter() {
			@Override
			public void run() {
				
				/* This call to requestLayout() is required because
				 * animations are strange in Android. While it is true
				 * that they move where a View is drawn, they DO NOT,
				 * for some reason, CHANGE where the View RECEIVE EVENTS.
				 * If this call is not made at the end of animation, the 
				 * gems would appear in their new location, but clicking 
				 * one of them would result in an event being generated for
				 * the other gem.
				 */
				requestLayout();	
				checkMatches();
			}
		});
		gem2.startAnimation(set2);
	}

	protected void doneAnimating() {
		requestLayout();
		mGameActivity.updateValues(mScore, mTurns);
		mAcceptInput = true;
		
	}

	
	protected void checkMatches() {
		
		//The 2 Hashsets records all GemViews that are in matching rows or columns
		Set<GemView> matchingRows = new HashSet<GemView>();
		Set<GemView> matchingColumns = new HashSet<GemView>();

		//Interate each row and test if all of the gems are of the same type
		for (int r = 0; r < numRows; r++) {
			Set<GemView> oneSet = new HashSet<GemView>();

			GemView zero = findGemView(0, r);
			boolean allSame = true;
			for (int c = 0; c < numColumns; c++) {
				GemView gemView = findGemView(c, r);
				if (gemView.getGemType() != zero.getGemType()) {
					allSame = false;
					break;
				}
				oneSet.add(gemView);
			}

			if (allSame) {
				matchingRows.addAll(oneSet);
			}
		}

		//Interate each column and test if all of the gems are of the same type
		for (int c = 0; c < numColumns; c++) {
			Set<GemView> oneSet = new HashSet<GemView>();

			GemView zero = findGemView(c, 0);
			boolean allSame = true;
			for (int r = 0; r < numRows; r++) {
				GemView gemView = findGemView(c, r);
				if (gemView.getGemType() != zero.getGemType()) {
					allSame = false;
					break;
				}
				oneSet.add(gemView);
			}

			if (allSame) {
				for (GemView gem : oneSet) {
					if (!matchingRows.contains(gem)) {
						matchingColumns.add(gem);
					}
				}
			}
		}

		//If the orbs row or column are not from the same type
		if (matchingRows.size() == 0 && matchingColumns.size() == 0) {
			doneAnimating();
			return;
		}

		//If they are an animation sequence is created for each GemView to 
		//produce a sequence
		int size = getWidth();
		boolean runAfterSet = false;

		final Set<GemView> allGems = new HashSet<GameView.GemView>(matchingColumns);
		allGems.addAll(matchingRows);

		if (matchingRows.size() != 0) {
			for (GemView gemView : matchingRows) {
				
				ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.5f, 1.0f,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleDown.setDuration(250);
				scaleDown.setFillAfter(true);
				
				
				//Play the swap sound
				mSound.playAllMatch();
				
				TranslateAnimation trans = new TranslateAnimation(0, size, 0, 0);
				trans.setDuration(250);
				trans.setStartOffset(250);
				trans.setFillAfter(true);

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(scaleDown);
				set.addAnimation(trans);

				if (!runAfterSet) {
					runAfterSet = true;
					set.setAnimationListener(new RunAfter() {
						@Override
						public void run() {
							updateRemovedGems(allGems);
						}
					});
				}

				gemView.startAnimation(set);
			}
		}

		if (matchingColumns.size() != 0) {
			for (GemView gemView : matchingColumns) {
				ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.5f, 1.0f,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleDown.setDuration(250);
				scaleDown.setFillAfter(true);

				//Play the swap sound
				mSound.playAllMatch();
				
				TranslateAnimation trans = new TranslateAnimation(0, 0, 0, size);
				trans.setDuration(250);
				trans.setStartOffset(250);
				trans.setFillAfter(true);

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(scaleDown);
				set.addAnimation(trans);

				if (!runAfterSet) {
					runAfterSet = true;
					set.setAnimationListener(new RunAfter() {
						@Override
						public void run() {
							updateRemovedGems(allGems);
						}
					});
				}

				gemView.startAnimation(set);
			}
		}

	}

	/**
	 *  Update the score based on the allGems parameter,
	 *  a Set of GemView objects that represent all gems that were part of
	 *  a matching column or row.
	 *  The size of allGems is used to update the current score, them each
	 *  at the end all GemView in the set are assigned to new random types.
	 * 
	 * @param allGems A set of GemView objects .
	 */
	private void updateRemovedGems(Set<GemView> allGems) {
		//General method to update Scores
		mScore += allGems.size() * 5;
		
		long timeBonus;
		if (mTurns/20 < 5 ){
			//time bonus (in milliseconds)
			timeBonus = (allGems.size() - mTurns/20) * 1000;
		}
		else {
			timeBonus = 1000;
		}
		
		mGameCountDownTimer.cancel();
		mGameCountDownTimer = new GameCountDownTimer((mGameTimer+timeBonus), UPDATE_UI_TIMER);
		mGameCountDownTimer.start();
		
		for (GemView gemView : allGems) {
			gemView.setRandomType();
		}
		requestLayout();
		checkMatches();
	}

	
	/**
	 * Return the GemView object of a given position (row ,column).
	 * Return null if no object is found at the given position.
	 * 
	 * @param col  Represent the column of the GemView object
	 * @param row Represent the ro of the GemView object
	 * @return GameView object (null if nothing is found).
	 */
	protected GemView findGemView(int col, int row) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if (v instanceof GemView) {
				GemView gemView = (GemView) v;
				if (gemView.getColumn() == col && gemView.getRow() == row) {
					return gemView;
				}
			}
		}
		return null;
	}

	
	protected class GemView extends ImageView {
		
		private int mGemType;
		private int mColumn;
		private int mRow;

		protected GemView(Context context, int column, int row, int gemType ) {
			super(context);
			mColumn = column;
			mRow = row;
			mGemType = gemType;
			
			Drawable image = getResources().getDrawable(mGemIds[mGemType]);
			image.setAlpha(200);
			setImageDrawable(image);
			setClickable(true);
			setOnClickListener(GameView.this);
		}
		
		public int getGemType() {
			return mGemType;
		}

		public void setRandomType() {
			mGemType = mRandom.nextInt(3);
			Drawable image = getResources().getDrawable(mGemIds[mGemType]);
			setImageDrawable(image);
		}

		public int getColumn() {
			return mColumn;
		}

		public int getRow() {
			return mRow;
		}

		public void setColumn(int column) {
			mColumn = column;
		}

		public void setRow(int row) {
			mRow = row;
		}
	}

	/**
	 * Utility class used to call methods after an animation is complete.
	 * 
	 *  
	 * @author Vinicius Busquet
	 * 
	 * @see android.view.animation.AnimationListener
	 * @see java.lang.Runnable
	 */
	private abstract class RunAfter implements AnimationListener, Runnable {

		@Override
		public void onAnimationEnd(Animation animation) {
			run();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

}
