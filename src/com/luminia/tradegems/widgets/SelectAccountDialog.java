package com.luminia.tradegems.widgets;

import com.luminia.tradegems.R;
import com.luminia.tradegems.database.GameAccount;
import com.luminia.tradegems.database.MyDBAdapter;

import android.accounts.Account;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectAccountDialog extends DialogFragment implements OnItemClickListener {
	private static final String TAG = "SelectAccountDialog";
	private Account[] mAccountArray;
	private String[] emailsArray;
	private SelectAccountDialog(){}
	
	/**
	 * Create a new instance of this dialog by passing an array of Account objects
	 * inside a Bundle parameter.
	 * If the Bundle does not have this required array, the method will return null.
	 * @param bundle
	 * @return
	 */
	public static SelectAccountDialog newInstance(Bundle bundle) {
		if(bundle.size() == 0 || bundle.getParcelableArray("accounts") == null)
			return null;
		SelectAccountDialog dialog = new SelectAccountDialog();
		Account[] accounts = (Account[]) bundle.getParcelableArray("accounts");
		dialog.setAccountArray(accounts);
		return dialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.select_account_dialog, container, false);
		ListView accountList = (ListView) view.findViewById(R.id.AccountList);
		accountList.setOnItemClickListener(this);
		accountList.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,emailsArray));
		return view;
	}

	public Account[] getAccountArray() {
		return mAccountArray;
	}

	public void setAccountArray(Account[] mAccountArray) {
		this.mAccountArray = mAccountArray;
		emailsArray = new String[mAccountArray.length];
		for(int i = 0; i < mAccountArray.length; i++){
			emailsArray[i] = mAccountArray[i].name;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MyDBAdapter dbAdapter = MyDBAdapter.getInstance(getActivity());
		GameAccount account = new GameAccount();
		account.setEmail( (String) ((TextView)view).getText());
		Log.d(TAG,"Setting default email to: "+account.getEmail());
		dbAdapter.insertAccount(account,true);
		this.dismiss();
	}
}
