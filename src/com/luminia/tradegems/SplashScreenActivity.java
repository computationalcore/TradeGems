/*
 * @(#)SplashScreenActivity.java        1.00 12/02/29
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
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class SplashScreenActivity extends Activity {
	
	private boolean mActive = true;
    private int mSplashTime = 3000; // time to display the splash screen in ms

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {

            @Override
            public void run() {
                try {
                    int waited = 0;
                    while ( mActive && (waited < mSplashTime) ) {
                        sleep(100);
                        if (mActive) waited += 100;
                    }
                } 
                catch (InterruptedException e) {
                    // do nothing
                } 
                finally {
                    finish();
                    startActivity(new Intent("com.luminia.tradegems.MainActivity"));
            		stop();
                }
            }
        };
        splashTread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mActive = false;
        }
        return true;
    }

}
