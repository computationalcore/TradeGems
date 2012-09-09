package com.luminia.tradegems.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private final Context mContext;
	private MyDBAdapter mDBAdapter;
	
	private static final String TAG = "DBHelper";

	
	//Constructor
	public DBHelper(Context context,MyDBAdapter dbAdapter, String name, CursorFactory factory,int version){
		super(context,name,null,version);
		mContext = context;
		mDBAdapter = dbAdapter;
	}
	
	public DBHelper(Context context, MyDBAdapter adapter){	//About to be deprecated
		super(context,MyDBAdapter.DATABASE_NAME,null,MyDBAdapter.DATABASE_VERSION);
		mContext = context;
		mDBAdapter = adapter;
	}
	
	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.w("DBHelper","onCreate called, creating datbase: "+ db);
		String createAccounts = MyDBAdapter.CREATE_ACCOUNTS_TABLE;
		String createScores = MyDBAdapter.CREATE_SCORES_TABLE;
		try{
			synchronized(this) {
				db.execSQL(createAccounts);
				db.execSQL(createScores);
			}			
		}catch(SQLiteException e){
			Log.e(TAG,"Exception caught");
			Log.e(TAG,"Msg: "+e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("DBHelper","Upgrading from version "+oldVersion+" to "+newVersion);
		// Dropping old table
//		String sql = "drop table if exists "+MyDBAdapter.TABLE_ACCOUNTS;

		// Creating new tables
//		db.execSQL(sql);
		onCreate(db);		
	}	
}
