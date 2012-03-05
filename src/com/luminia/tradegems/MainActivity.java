package com.luminia.tradegems;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
	
	public final static String SERVICE_URL = "http://trade-gems.appspot.com/";

	private Button playGameButton;
	private Button highScoreButton;
	private Button aboutButton;
	
	private Button topTenButton;
	private Button usersOfGameButton;
	private Button locationButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		playGameButton = (Button) findViewById(R.id.playGameButton);
		highScoreButton = (Button) findViewById(R.id.highScoreButton);
		aboutButton = (Button) findViewById(R.id.aboutButton);
		
		topTenButton = (Button) findViewById(R.id.viewTopTen);
		usersOfGameButton = (Button) findViewById(R.id.usersOfGame);
		locationButton = (Button) findViewById(R.id.viewLocation);

		playGameButton.setOnClickListener(this);
		highScoreButton.setOnClickListener(this);
		aboutButton.setOnClickListener(this);
		
		topTenButton.setOnClickListener(this);
		usersOfGameButton.setOnClickListener(this);
		locationButton.setOnClickListener(this);
    }
    
    @Override
	public void onClick(View button) {
		if (button == playGameButton) {
			Intent intent = new Intent(this, GameActivity.class);
			startActivity(intent);
		} else if (button == highScoreButton) {
			Intent intent = new Intent(this, HighScoreActivity.class);
			startActivity(intent);
		} else if (button == aboutButton) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (button == topTenButton) {
			Intent intent = new Intent(this, TopTenActivity.class);
			startActivity(intent);
		} else if (button == usersOfGameButton) {
			Intent intent = new Intent(this, UsersOfGameActivity.class);
			startActivity(intent);
		} else if (button == locationButton) {
			Intent intent = new Intent(this, UsersLocationActivity.class);
			startActivity(intent);
		}
		
		//unknown button.
	}
}