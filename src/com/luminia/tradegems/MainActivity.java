package com.luminia.tradegems;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{

	private Button playGameButton;
	private Button highScoreButton;
	private Button aboutButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		playGameButton = (Button) findViewById(R.id.playGameButton);
		highScoreButton = (Button) findViewById(R.id.highScoreButton);
		aboutButton = (Button) findViewById(R.id.aboutButton);

		playGameButton.setOnClickListener(this);
		highScoreButton.setOnClickListener(this);
		aboutButton.setOnClickListener(this);
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
		}
		//unknown button.
	}
}