package com.luminia.tradegems;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class TopTenActivity extends Activity {
	public HttpClient client = new DefaultHttpClient();
//	private TableLayout tableLayout;
	
	private ListView list;
	private PlayerListAdapter adapter;
	private static final String TAG = "TopTenActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top_ten_list);
		list = (ListView) findViewById(R.id.TopTenList);
		if(isNetworkAvailable()){
			new GetTopTen().execute(10);
		}else{
			setContentView(R.layout.no_internet);
			// Place a message in the background telling the user to switch on the network
		}
	}

	private class GetTopTen extends AsyncTask<Integer, Integer, JSONArray> {

		@Override
		protected JSONArray doInBackground(Integer... counts) {
			JSONArray result = null;
			try {
				StringBuilder fullUrl = new StringBuilder(
						MainActivity.SERVICE_URL);

				fullUrl.append("ranking");
				//fullUrl.append(counts[0]);

				HttpGet get = new HttpGet(fullUrl.toString());
				HttpResponse response = client.execute(get);

				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					result = new JSONArray(json);
				} else {
					Log.e(TAG,"Error");
					Log.e(TAG,"Response line: "+response.getStatusLine());
				}
			}catch(IOException e){
				Log.e(TAG,"IOException caught in GetTopTen");
				Log.e(TAG,"Msg: "+e.getMessage());
			}catch (Exception e) {
				Log.e(TAG, "Exception caught in GetTopTen");
				Log.e(TAG,"Msg: "+e.getMessage());
			}
			return result;
		}

		protected void onPostExecute(final JSONArray result) {
			Log.d(TAG,"Result in PostExecute: "+result);
			if(result != null){
				adapter = new PlayerListAdapter(TopTenActivity.this,result);
				list.setAdapter(adapter);
				list.invalidate();
			}else{
				//TODO: Set an empty view for the empty ListView
//				list.setEmptyView(emptyView);
			}
		}
	}

//	protected void displayResults(JSONArray result) throws JSONException {
//		if(result == null){
//			Log.e(TAG,"AsyncTask returned a null result");
//		}
//		tableLayout.removeAllViews();
//
//		TableRow row = new TableRow(this);
//		row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//				LayoutParams.FILL_PARENT));
//
//		TextView userTitleView = new TextView(this);
//		userTitleView.setText("Username:");
//		userTitleView.setTextSize(18);
//		userTitleView.setPadding(10, 10, 100, 2);
//		row.addView(userTitleView);
//
//		TextView scoreTitleView = new TextView(this);
//		scoreTitleView.setText("Score:");
//		scoreTitleView.setTextSize(18);
//		row.addView(scoreTitleView);
//		
//		tableLayout.addView(row, new TableLayout.LayoutParams(
//				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//
//		for (int i = 0; i < result.length(); i++) {
//			TopScoreReport highscore = new TopScoreReport(result.getJSONObject(i));
//
//			row = new TableRow(this);
//			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
//					LayoutParams.FILL_PARENT));
//
//			TextView userView = new TextView(this);
//			userView.setText(highscore.getAccountName());
//			userView.setTextSize(16);
//			userView.setPadding(10, 10, 100, 2);
//			row.addView(userView);
//
//			TextView scoreView = new TextView(this);
//			scoreView.setText("" + highscore.getScore());
//			scoreView.setTextSize(16);
//			row.addView(scoreView);
//			tableLayout.addView(row, new TableLayout.LayoutParams(
//					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		}
//	}
	
	/**
	 * Method that will check for any network connectivity
	 * @return True if there is a network connection available, false otherwise
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if(activeNetworkInfo != null)
	    	Log.d(TAG,"activeNetworkInfo: "+activeNetworkInfo.describeContents());
	    else
	    	Log.d(TAG,"No network");
	    return activeNetworkInfo != null;
	}
}
