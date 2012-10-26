package com.luminia.tradegems.widgets;

import com.luminia.tradegems.MainActivity;
import com.luminia.tradegems.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SelectNicknameDialog extends DialogFragment {
	protected static final String TAG = "SelectNicknameDialog";
	private Button mSave;
	private EditText mNicknameEditText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.select_nickname_dialog, container, false);
		mNicknameEditText = (EditText) view.findViewById(R.id.editNickname);
		mSave = (Button) view.findViewById(R.id.select_nickname_bt);
		
		mSave.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = getActivity().getSharedPreferences(MainActivity.KEY_PREFERENCES,Context.MODE_PRIVATE).edit();
				String nickname = mNicknameEditText.getText().toString();
				if(nickname == null || !nickname.equals("")){
					nickname = MainActivity.DEFAULT_NICKNAME;
				}
				Log.i(TAG,"Saving nickname: "+nickname);
				editor.putString(MainActivity.KEY_NICKNAME, nickname);
				editor.commit();
				dismiss();

			}
		});
		return view;
	}

}
