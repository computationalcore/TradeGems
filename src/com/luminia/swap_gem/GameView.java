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
package com.luminia.swap_gem;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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

	//Array that stores the IDs of three different gem images (they are set in the init() method
	private int mGemIds[] = new int[3];
	
	private Random mRandom = new Random();

	private GemView mSelectedGemView = null;
	private boolean mAcceptInput = true;

	//Current score
	private int mScore = 0;
	
	//Number of remaining turns
	private int mTurns = 10;

	//This reference is used to update the TextViews(Score and Turns) values of the activity
	private GameActivity mGameActivity;

	
	/**
	 * This constructor is called when the View is instantiated from XML (Required)
	 */
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * This constructor is required when the View is instantiated programmatically (Not used in this game)
	 */
	public GameView(Context context) {
		super(context);
		init();
	}

	/**
	 * Link the array of Gems if the IDs of the drawable,
	 * and set the background.
	 * Called inside the class constructor.  
	 */
	private void init() {
		setBackgroundDrawable(new Background());

		mGemIds[0] = R.drawable.red_gem;
		mGemIds[1] = R.drawable.green_gem;
		mGemIds[2] = R.drawable.blue_gem;
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
		mTurns = 10;
		mAcceptInput = true;

		removeAllViews();

		for (int c = 0; c < 5; c++) {
			for (int r = 0; r < 5; r++) {
				GemView mGemView = new GemView(getContext(), c, r, mRandom.nextInt(3));
				addView(mGemView);
			}
		}
		mGameActivity.updateValues(mScore, mTurns);
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
		int oneFifth = size / 5;

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
		
		//Decrement number of turns
		mTurns--;

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
		trans1.setDuration(500);
		trans1.setStartOffset(500);

		ScaleAnimation scaleUp1 = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp1.setDuration(500);
		scaleUp1.setStartOffset(1000);

		AnimationSet set1 = new AnimationSet(false);
		set1.addAnimation(scaleUp1);
		set1.addAnimation(trans1);

		gem1.startAnimation(set1);

		//Animate gem2
		ScaleAnimation scaleDown2 = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleDown2.setDuration(500);
		scaleDown2.setInterpolator(new AnticipateOvershootInterpolator());

		TranslateAnimation trans2 = new TranslateAnimation(0, gem1.getLeft()
				- gem2.getLeft(), 0, gem1.getTop() - gem2.getTop());
		trans2.setDuration(500);
		trans2.setStartOffset(500);

		ScaleAnimation scaleUp2 = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp2.setDuration(500);
		scaleUp2.setStartOffset(1000);

		AnimationSet set2 = new AnimationSet(false);
		set2.addAnimation(scaleDown2);
		set2.addAnimation(scaleUp2);
		set2.addAnimation(trans2);

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
				 * the other orb.
				 */
				requestLayout();
				
				checkMatches();
			}
		});
		gem2.startAnimation(set2);
	}

	protected void doneAnimating() {
		requestLayout();
		mAcceptInput = true;
		mGameActivity.updateValues(mScore, mTurns);
		if (mTurns <= 0) {
			mGameActivity.endGame();
		}
	}

	
	protected void checkMatches() {
		
		//The 2 Hashsets records all GemViews that are in matching rows or columns
		Set<GemView> matchingRows = new HashSet<GemView>();
		Set<GemView> matchingColumns = new HashSet<GemView>();

		//Interate each row and test if all of the gems are of the same type
		for (int r = 0; r < 5; r++) {
			Set<GemView> oneSet = new HashSet<GemView>();

			GemView zero = findGemView(0, r);
			boolean allSame = true;
			for (int c = 0; c < 5; c++) {
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
		for (int c = 0; c < 5; c++) {
			Set<GemView> oneSet = new HashSet<GemView>();

			GemView zero = findGemView(c, 0);
			boolean allSame = true;
			for (int r = 0; r < 5; r++) {
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
				scaleDown.setDuration(500);
				scaleDown.setFillAfter(true);

				TranslateAnimation trans = new TranslateAnimation(0, size, 0, 0);
				trans.setDuration(500);
				trans.setStartOffset(500);
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
				scaleDown.setDuration(500);
				scaleDown.setFillAfter(true);

				TranslateAnimation trans = new TranslateAnimation(0, 0, 0, size);
				trans.setDuration(500);
				trans.setStartOffset(500);
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
	 *  Update the score based on the allGems.
	 *  The size of allGems is used to update the current score, them each
	 *  GemView in allGems is assigned a new random type.
	 * 
	 * @param allGems Contain all gems that were part of a matching column or row.
	 */
	private void updateRemovedGems(Set<GemView> allGems) {
		mScore += allGems.size() * 5;
		for (GemView gemView : allGems) {
			gemView.setRandomType();
		}
		requestLayout();
		checkMatches();
	}

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
