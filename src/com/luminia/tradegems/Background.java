/*
 * @(#)Background.java        1.00 12/02/19
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

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

/**
* This Drawable Class represents the main game Background.
*
*@see android.graphics.drawable.LayerDrawable
*
* @version 	1.00 19 Fev 2012
* @author 	Vinicius Busquet - computationalcore@gmail.com
*/
public class Background extends LayerDrawable {

	/**
	 * The constructor create an array of two Drawables, a ColorDrawable
	 * to handle background color and GridDrawable, a custom drawable, 
	 * inner class of Background.
	 */
	public Background() {
		super(new Drawable[] { new ColorDrawable(Color.WHITE),
				new GridDrawable() });
	}

	/**
	 * Set the color, stroke width ans style of the grids,
	 * and create it's pattern.
	 * 
	 * @see android.graphics.drawable.ShapeDrawable
	 *   
	 */
	private static class GridDrawable extends ShapeDrawable {
		private GridDrawable() {
			super(createGridPath());
			getPaint().setColor(Color.GRAY);
			getPaint().setStrokeWidth(1.0f);
			getPaint().setStyle(Paint.Style.FILL);
		}
	}

	
	/**
	 * Creates a PathShaped used by GridDrawable
	 * 
	 * @return a pathShape object that describe the sahpe in term of paths
	 * 
	 * @see android.graphics.drawable.shapes.PathShape
	 * 
	 */
	private static PathShape createGridPath() {
		float size = 1000;
		float colOrRowSize = size / 5.0f;
		float fivePercent = size * 0.05f;

		float onePercent = size * 0.01f;

		Path lines = new Path();
		for (int i = 0; i < 5; i++) {
			float x = i * colOrRowSize + colOrRowSize;

			lines.moveTo(x - onePercent, fivePercent);
			lines.lineTo(x + onePercent, fivePercent);
			lines.lineTo(x + onePercent, size - fivePercent);
			lines.lineTo(x - onePercent, size - fivePercent);
			lines.close();
		}
		for (int i = 0; i < 5; i++) {
			float y = i * colOrRowSize + colOrRowSize;

			lines.moveTo(fivePercent, y - onePercent);
			lines.lineTo(fivePercent, y + onePercent);
			lines.lineTo(size - fivePercent, y + onePercent);
			lines.lineTo(size - fivePercent, y - onePercent);
			lines.close();
		}

		return new PathShape(lines, size, size);
	}

}
