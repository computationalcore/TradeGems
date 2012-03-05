package com.luminia.tradegems;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;


public class ScoreActivity extends Activity {
	
	private TextView mCurrentScore;
	private GameSound mSound;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score);
		
		mCurrentScore = (TextView) findViewById(R.id.current_score);
		Intent intent = getIntent();
		String score = intent.getStringExtra("score");
		mCurrentScore.setText(score);
		//mSound = new GameSound(getContext());
		//mSound.playEnd();
		
	}
	
	@Override
	public void onDestroy(){
		//mSound.release();
		super.onDestroy();
	}

}
