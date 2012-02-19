/**
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
package com.luminia.swap_gem;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


/**
* Sprite class represents an abstraction to 2D animations.
*
* @version 	1.00 19 Fev 2012
* @author 	Vinicius Busquet - computationalcore@gmail.com
* 
* @see android.graphics.drawable.Drawable
* 
*/
public class Sprite extends Drawable {
	public static Random sRandom = new Random();

	//Represent the center of the Sprite in the screen
	private float mX;
	private float mY;
	//Radious of the sprite(max distance to the center)
	private float mRadius;

	/* How much the previous variables should change (Delta mean "variation")
	 * for every frame of animation (eg. per call to update)
	 */
	private float mDeltaX;
	private float mDeltaY;
	private float mDeltaRadius;

	//Drawable used to draw the Sprite
	private Drawable mDrawable;

	/**
	 * The constructor of the Sprite class uses the static variable
	 * random to randomize the location, direction, speed, and how 
	 * much the sprite scale.
	 * 
	 * @param drawable
	 * @param width
	 * @param height
	 */
	public Sprite(Drawable drawable, float width, float height) {
		mDrawable = drawable;

		//Randomize radius
		mRadius = 10 + sRandom.nextFloat() * 30;

		//Randomize Location
		mX = mRadius + sRandom.nextFloat() * (width - mRadius);
		mY = mRadius + sRandom.nextFloat() * (height - mRadius);

		//Randomize Direction
		double direction = sRandom.nextDouble() * Math.PI * 2;
		float speed = sRandom.nextFloat() * .3f + .7f;

		mDeltaX = (float) Math.cos(direction) * speed;
		mDeltaY = (float) Math.sin(direction) * speed;

		//Randomize 
		if (sRandom.nextBoolean()) {
			mDeltaRadius = sRandom.nextFloat() * .2f + .1f;
		} else {
			mDeltaRadius = sRandom.nextFloat() * -.2f - .1f;
		}

	}

	/**
	 * Applies changes on the Sprite location
	 * with some random animations
	 * 
	 * @param width
	 * @param height
	 */
	public void update(int width, int height) {
		if (mRadius > 40 || mRadius < 15) {
			mDeltaRadius *= -1;
		}
		mRadius += mDeltaRadius;

		if (mX + mRadius > width) {
			mDeltaX *= -1;
			mX = width - mRadius;
		} else if (mX - mRadius < 0) {
			mDeltaX *= -1;
			mX = mRadius;
		}

		if (mY + mRadius > height) {
			mDeltaY *= -1;
			mY = height - mRadius;
		} else if (mY - mRadius < 0) {
			mDeltaY *= -1;
			mY = mRadius;
		}
		mX += mDeltaX;
		mY += mDeltaY;

	}

	/**
	 * Use the center and radius of the Sprite to
	 * define the bounds of the drawable.
	 * 	
	 */
	@Override
	public void draw(Canvas canvas) {

		Rect bounds = new Rect(Math.round(mX - mRadius), Math.round(mY - mRadius),
				Math.round(mX + mRadius), Math.round(mY + mRadius));
		mDrawable.setBounds(bounds);

		mDrawable.draw(canvas);
	}

	
	/*
	 * Setter and Getters methods
	 */
	
	@Override
	public int getOpacity() {
		return mDrawable.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		mDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mDrawable.setColorFilter(cf);
	}

	public float getX() {
		return mX;
	}

	public void setX(float x) {
		mX = x;
	}

	public float getY() {
		return mY;
	}

	public void setY(float y) {
		mY = y;
	}
    
	public Drawable getDrawable() {
		return mDrawable;
	}

	public void setDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	public float getRadius() {
		return mRadius;
	}

	public void setRadius(float radius) {
		mRadius = radius;
	}

}
