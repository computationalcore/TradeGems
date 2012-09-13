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
	public static final String TABLE_STATE = "state_table";
		
	// Accounts table definition
	public static final String COL_EMAIL = "email";
	public static final String COL_DEFAULT = "def_account";
	
	// Scores table definition
	public static final String COL_ACCOUNT_ID = "account_id";
	public static final String COL_SCORE = "score";
	
	// State table definition
	public static final String COL_ROW = "row";
	public static final String COL_COL = "col";
	public static final String COL_GEM_TYPE = "gem_type";
		
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
	
	// Sql statement used to create a new state table
	public static final String CREATE_STATE_TABLE = "CREATE TABLE IF NOT EXISTS "
	+TABLE_STATE+" ("
	+COL_ROW+" INTEGER NOT NULL,"
	+COL_COL+" INTEGER NOT NULL,"
	+COL_GEM_TYPE+" INTEGER NOT NULL);";
	
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
			this.close();
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
			this.close();
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
				score = new Score(new Long(cursor.getLong(0)));
			}
		}catch(SQLException e){
			Log.e(TAG,"SQLException caught. Msg: "+e.getMessage());
		}finally{
			cursor.close();
			mydb.endTransaction();
			this.close();
		}
		return score;
	}
	
	/**
	 * Method that sets the score passes as argument into the database for the 
	 * current default user.
	 * @param score The score to be entered into the database.
	 */
	public void addScore(Score currentScore){
		if(!this.isOpen()){
			this.open();
		}
		ContentValues contentValues = new ContentValues();
		contentValues.put(COL_EMAIL, currentScore.getAccountName());
		contentValues.put(COL_SCORE,currentScore.getScore().longValue());
		mydb.beginTransaction();
		try{
			mydb.insertOrThrow(TABLE_SCORES, null, contentValues);
		}catch(SQLException e){
			Log.e(TAG,"SQLException caught!. Msg: "+e.getMessage());
		}finally{
			mydb.endTransaction();
			this.close();
		}
	}
	
	/**
	 * This method should be used to check if there is some state currently persisted in 
	 * the database.
	 * @return True if there is some information about state persisted in the database,
	 * false otherwise.
	 * 
	 * @author Nelson R. Perez - bilthon@gmail.com 
	 */
	public boolean isStateSaved(){
		if(!this.isOpen()){
			this.open();
		}
		boolean result = false;
		mydb.beginTransaction();
		try{
			String sql = "SELECT COUNT(*) FROM "+TABLE_STATE;
			Cursor cursor = mydb.rawQuery(sql, null);
			result = cursor.moveToFirst();
		}catch(SQLException e){
			Log.e(TAG,"SQLException caught!. Msg: "+e.getMessage());
		}finally{
			mydb.endTransaction();
			this.close();
		}
		return result;
	}
	
	/**
	 * Method that saves a specific state of the game into the database for later retrieval.
	 * Calling this method once there already is data persisted will result in the loss of the
	 * previously stored data.
	 * 
	 * @param matrix The matrix representing the current state of the game
	 * @param row The number of rows from the game state matrix
	 * @param col The number of columns from the game state matrix
	 * 
	 * @author Nelson R. Perez - bilthon@gmail.com 
	 */
	public void saveState(int[][] matrix, int row, int col){
		if(!this.isOpen()){
			this.open();
		}
		if(isStateSaved()) clearState();
		ContentValues contentValues = new ContentValues();
		mydb.beginTransaction();
		try{
			for(int i = 0; i < row; i++){
				for(int j = 0; j < col; j++){
					contentValues.put(COL_ROW,i);
					contentValues.put(COL_COL,j);
					contentValues.put(COL_GEM_TYPE, matrix[i][j]);
					mydb.insertOrThrow(TABLE_STATE, null, contentValues);
					contentValues.clear();
				}
			}			
		}catch(SQLException e){
			Log.e(TAG,"Exception while saving game state in database");
			Log.e(TAG,"Msg: "+e.getMessage());
		}finally{
			mydb.endTransaction();
			this.close();
		}
	}
	
	/**
	 * Clears any data about the game state that might be present in the database
	 * 
	 * @author Nelson R. Perez - bilthon@gmail.com 
	 */
	public void clearState(){
		if(!this.isOpen()){
			this.open();
		}
		String statement = "DELETE * FROM "+TABLE_STATE;
		mydb.beginTransaction();
		try{
			mydb.execSQL(statement);
		}catch(SQLException e){
			Log.e(TAG,"Exception while clearing the game state in database");
			Log.e(TAG,"Msg: "+e.getMessage());
		}finally{
			mydb.endTransaction();
			this.close();
		}
	}
	
	/**
	 * Method that returns the state matrix stored in the database.
	 * The information is stored in the database as a sequence of integers, so it is important
	 * for the method to correctly reconstruct a matrix to know its dimensions.
	 * The dimensions then are provided as an argument and are checked with the data stored in the
	 * database. This method will only return a valid matrix if the stored data can be used to do so.
	 * If no data is present, null will be returned. If there is some data, but the size does not
	 * match the arguments given an IllegalArgumentException will be thrown.
	 * @param row The number of rows expected from the resulting matrix
	 * @param col The number of columns expected from the resulting matrix
	 * @return A matrix of integers representing the game state
	 * @throws IllegalArgumentException
	 * 
	 * @author Nelson R. Perez - bilthon@gmail.com 
	 */
	public int[][] getState(int row, int col) throws IllegalArgumentException{
		if(!this.isOpen()){
			this.open();
		}
		int[][] state = new int[row][col];
		mydb.beginTransaction();
		try{
			String[] columns = {COL_ROW, COL_COL, COL_GEM_TYPE};
			Cursor cursor = mydb.query(TABLE_STATE, columns, null, null, null, null, null);
			if(!cursor.moveToFirst())
				return null;
			if(cursor.getCount() != row*col){
				throw new IllegalArgumentException("Arguments row*col must equals the number of gems " +
						"in the matrix! The number of elements apparently is "+cursor.getCount());
			}
			for(int i = 0; i < row; i++){
				for(int j = 0; j < col; j++){
					state[i][j] = cursor.getInt(2);
					cursor.moveToNext();
				}
			}
		}catch(SQLException e){
			Log.e(TAG,"Exception while selecting the game state in database");
			Log.e(TAG,"Msg: "+e.getMessage());
		}finally{
			mydb.endTransaction();
			this.close();
		}
		return state;
	}
}