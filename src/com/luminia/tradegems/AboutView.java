/*
 * @(#)AboutView.java        1.00 12/02/18
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
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
* This View class represents the About.
*
* @version 	1.00 19 Fev 2012
* @author 	Vinicius Busquet - computationalcore@gmail.com
* 
* @see android.view.SurfaceHolder.Callback
* @see android.view.SurfaceView
* 
*/
public class AboutView extends SurfaceView implements Callback {
	
	public static Random random = new Random();

	private boolean mAnimating = true;
	private AnimationThread mThread;

	private List<Sprite> mSprites = new ArrayList<Sprite>();

	/**
	 * This constructor is called when the View is instantiated from XML (Required)
	 */
	public AboutView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SurfaceHolder surfaceHolder = getHolder();
		
		/*This callback cause the methods surfaceCreated, surfaceChanged, and surfaceDestroyed 
		*to be called when the Surface is created, change size or destroyed
		*/
		surfaceHolder.addCallback(this);
		
		mThread = new AnimationThread(surfaceHolder);
	}

	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//called when the size of the surface changes, we are not handling this case.
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mAnimating = true;
		
		/*The thread starts here (instead of the the class constructor) to prevent trying to draw a surface
		*that does not yet exist.
		*/
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		mAnimating = false;
		while (retry) {
			try {
				
				/*Cause the calling thread to block until the
				 * AnimationThread stops execution. 
				 * Otherwise the application can crash if the surface
				 * is destroyed and the AnimationThread is running. 
				 */
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}
	
	/**
	* Thread Class that handle animations.
	*
	* @version 	1.00 19 Fev 2012
	* @author 	Vinicius Busquet - computationalcore@gmail.com
	* 
	* @see java.lang.Thread
	* 
	*/
	private class AnimationThread extends Thread {
		
		private SurfaceHolder mSurfaceHolder;

		/**
		 * Class constructor
		 * 
		 * @param surfaceHolder
		 */
		AnimationThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;
		}

		/**
		 * Runs the animation thread
		 */
		@Override
		public void run() {
			while (mAnimating) {
				Canvas c = null;
				try {
					//Prevent SurfaceView to Create, modify or destroy the Canvas
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						doDraw(c);
					}
				} finally {
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}	
			}
		}

		/**
		 * 
		 * 
		 * @param canvas
		 */
		public void doDraw(Canvas canvas) {
			addSprites();

			canvas.drawColor(Color.WHITE);

			for (Sprite sprite : mSprites) {
				sprite.update(getWidth(), getHeight());
				sprite.draw(canvas);
			}

		}

		/**
		 * Create gems sprites object with the drawable resources,
		 * if it have nothing inside the mSprites array.
		 * 
		 *@see com.luminia.tradegems.Sprite
		 */
		private void addSprites() {
			if (mSprites.size() == 0) {
				Drawable redGem = getResources().getDrawable(R.drawable.red_gem);
				Drawable greenGem = getResources().getDrawable(R.drawable.green_gem);
				Drawable blueGem = getResources().getDrawable(R.drawable.blue_gem);

				int width = getWidth();
				int height = getHeight();

				mSprites.add(new Sprite(redGem, width, height));
				mSprites.add(new Sprite(greenGem, width, height));
				mSprites.add(new Sprite(blueGem, width, height));
			}
		}
	}
}