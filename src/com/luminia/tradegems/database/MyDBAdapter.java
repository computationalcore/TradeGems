package com.luminia.tradegems.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MyDBAdapter {
	private static final String TAG = "MyDBAdapter";
	
	/* Database Name and version */
	public static final String DATABASE_NAME = "tradegems.db";
	public static final int DATABASE_VERSION = 1;
	
	/* The index (key) column name for use in where clauses */
	public static final String KEY_ID = "_id";
	
	/* Database Tables */
	/* Column names for Cameras table*/
	public static final String TABLE_ACCOUNTS = "accounts_table";
	public static final String TABLE_SCORES = "scores_table";
		
	// Accounts table definition
	public static final String COL_EMAIL = "email";
	public static final String COL_DEFAULT = "def_account";
	
	// Scores table definition
	public static final String COL_ACCOUNT_ID = "account_id";
	public static final String COL_SCORE = "score";
		
	// Sql statement to create a new accounts table
	public static final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS "
	+TABLE_ACCOUNTS+" ("
	+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
	+COL_EMAIL+" VARCHAR NOT NULL,"
    +COL_DEFAULT+" INTEGER NOT NULL);";

	
	// Sql statement to create a new cameras table
	public static final String CREATE_SCORES_TABLE = "CREATE TABLE IF NOT EXISTS "
	+TABLE_SCORES+" ("
	+COL_ACCOUNT_ID+" INTEGER NOT NULL," 
	+COL_SCORE+" INTEGER NOT NULL,"
	+"FOREIGN KEY ("+COL_ACCOUNT_ID+") REFERENCES "+TABLE_ACCOUNTS+"("+KEY_ID+")"
	+");";
	
	// Variable to hold a database instance
	private static SQLiteDatabase mydb;
	
	//Database open/upgrade helper
	private DBHelper dbHelper;
	
	private final Context context;

	/**
	 * Singleton instance of the MyDBAdapter class
	 */
	protected static MyDBAdapter dbAdapter;
	
	public static MyDBAdapter getInstance(Context context){
		if(dbAdapter == null || dbAdapter.getContext() != context){
			dbAdapter = new MyDBAdapter(context);
		}
		return dbAdapter;
	}
	
	public Context getContext(){
		return this.context;
	}
	
	private MyDBAdapter(Context cont){
		context = cont;
		dbHelper = new DBHelper(context, this, DATABASE_NAME, null, DATABASE_VERSION);
		open();
	}
	
	public synchronized SQLiteDatabase open() throws SQLException {
		if(mydb == null || !mydb.isOpen()){
			Log.w(TAG,"Getting a new writable database connection!!");
			mydb = dbHelper.getWritableDatabase();			
		}
		return mydb;
	}
	
	public synchronized boolean isOpen(){
		return mydb.isOpen();
	}

	public synchronized void close(){
		Log.w(TAG,"Closing database connection from thread: "+Thread.currentThread());
		mydb.close();
	}
	
	public synchronized SQLiteDatabase getDatabase(){
		if(mydb ==  null || !mydb.isOpen()){
			mydb = dbHelper.getWritableDatabase();
		}
		return mydb;
	}

	// I/O Methods
	/**
	 * Method that inserts a camera into the database
	 */
	public void insertAccount(GameAccount account, boolean isDefault){
		ContentValues contentValues = new ContentValues();
		contentValues.put(COL_EMAIL, account.getEmail());
		contentValues.put(COL_DEFAULT,isDefault);
		if(!this.isOpen())
			this.open();
		mydb.beginTransaction();
		try{
			mydb.insertOrThrow(TABLE_ACCOUNTS, null, contentValues);
			mydb.setTransactionSuccessful();
		}catch(SQLException ex){
			Log.e(TAG,"SQLException caught!! Msg: "+ex.getMessage());
		}finally{
			mydb.endTransaction();
		}
	}
	
	public GameAccount getDefaultAccount(){
		if(!this.isOpen())
			this.open();
		String[] columns = {COL_EMAIL};
		String selection = ""+COL_DEFAULT+"=1";
		Cursor cursor = null;
		GameAccount account = null;
		mydb.beginTransaction();
		try{
			cursor = mydb.query(TABLE_ACCOUNTS, columns, selection, null, null, null, null);
			if(cursor != null && cursor.moveToFirst()){
				account = new GameAccount();
				account.setEmail(cursor.getString(0));
			}
		}catch(SQLException e){
			Log.e(TAG,"SQLException caught. Msg: "+e.getMessage());
		}finally{
			if(cursor != null) cursor.close();
			mydb.endTransaction();
		}
		return account;
	}
	
	public Score getHighestScore(GameAccount account){
		if(!this.isOpen())
			this.open();
		String sql = "SELECT max("+COL_SCORE+") FROM "+TABLE_SCORES+";";
		Score score = null;
		Cursor cursor = null;
		mydb.beginTransaction();
		try{
			cursor = mydb.rawQuery(sql,null);
			if(cursor != null && cursor.moveToFirst()){
				score = new Score();
				score.setScore(new Long(cursor.getLong(0)));
			}
		}catch(SQLException e){
			Log.e(TAG,"SQLException caught. Msg: "+e.getMessage());
		}finally{
			cursor.close();
			mydb.endTransaction();
		}
		return score;
	}
}