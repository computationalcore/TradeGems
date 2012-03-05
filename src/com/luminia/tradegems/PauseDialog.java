/*
 * @(#)PauseDialog.java        1.00 12/03/04
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

import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This Class represents the Score Dialog of the game,
 * where the user can share his score with other people
 * in Luminia Cloud.
 *
 * @version 	1.10 19 Fev 2012
 * @author 	Vinicius Busquet - computationalcore@gmail.com
 * 
 * @see android.view.View.OnClickListener
 */
public class PauseDialog extends Dialog implements OnClickListener {
	
	private TextView mPauseText;	
	
	private GameActivity mActivity;
	
	public PauseDialog(GameActivity activity) {
		super(activity);
		this.mActivity = activity;

		setContentView(R.layout.pause_dialog);
		
		//Set the dialog title
		setTitle( mActivity.getString(R.string.pause_title) );

		mPauseText = (TextView) findViewById(R.id.pause_text);

		mPauseText.setOnClickListener(this);

	}
	
	@Override
	public void onClick(View view) {

		if (view == mPauseText) {
			dismiss();
			mActivity.dialogClosed();
		}
	}

}