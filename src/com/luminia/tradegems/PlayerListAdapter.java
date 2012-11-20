package com.luminia.tradegems;

import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayerListAdapter extends BaseAdapter {
	private static final String TAG = "PlayerListAdapter";
	private JSONArray mData;
	private Context mContext;
	private LayoutInflater mInflater;
	private HashMap<String,String> mCountry2Code = new HashMap<String,String>();
	
	public PlayerListAdapter(Context c, JSONArray data){
		mContext = c;
		mData = data;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		createAssociationMap();
	}

	@Override
	public int getCount() {
		return mData.length();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		String name = null;
		String score = null;
		String country = null;
		
		if(vi == null){
			vi = mInflater.inflate(R.layout.list_row, null);
		}
//		if(position == 0){
//			vi.setBackgroundResource(R.drawable.rounded_corners_top);
//		}else if(position  == mData.length()-1){
//			vi.setBackgroundResource(R.drawable.rounded_corners_bottom);
//		}
		try {
//			Log.i(TAG,"JsonObject: "+mData);
			JSONObject data = (JSONObject) mData.get(position);
			name = data.getString("nickname");
			score = data.getString("score");
			country = data.getString(MainActivity.KEY_COUNTRY);
		} catch (JSONException e) {
			Log.e(TAG,"JSONException thrown after trying to get data from position "+position);
		}
		TextView playerNameTv = (TextView) vi.findViewById(R.id.name);
		TextView scoreTv = (TextView) vi.findViewById(R.id.score);
		ImageView flagIv = (ImageView) vi.findViewById(R.id.flag);
		TextView pos = (TextView) vi.findViewById(R.id.position);
		if(name != null)
			playerNameTv.setText(name.toString());
		if(score != null)
			scoreTv.setText(score.toString());
		if(pos != null)
			pos.setText(""+(position+1)+" - ");
		if(country != null){
			String flagResourceName = mCountry2Code.get(country);
			if(flagResourceName != null){
				// This is needed because the country code for (Google it..) is "do"
				// which cannot be the name of a drawable resource since it is a reserved
				// word in java, so it had to be renamed to "do2"
				if(flagResourceName.equals("do")) flagResourceName.concat("2");
				int flagResource = mContext.getResources().getIdentifier(flagResourceName,"drawable",mContext.getPackageName());
				flagIv.setImageResource(flagResource);
			}
		}
		return vi;
	}

	/**
	 * Method used for the purpose of relating the names sent by the server
	 */
	private void createAssociationMap() {
		Locale.setDefault(new Locale("en","US"));
		Locale[] locales = Locale.getAvailableLocales();
		String key,value;
		for(Locale locale : locales){
			key = locale.getDisplayCountry().toLowerCase();
			value = locale.getCountry().toLowerCase();
			if(!key.equals("") && !value.equals("")){
				mCountry2Code.put(key, value);
			}
		}
	}


}
