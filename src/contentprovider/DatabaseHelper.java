package contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	

	public static final String DATABASE="back";
	private static final int VERSION_CODE=4;
	
	public static final String REGISTER_TABLE="Register";//Register Table
	public static final String REG_STATUS_COL="Status";//Register Status coloumn
	public static final String REG_NICK_COL="Nickname";//Register Nickname coloumn
	
	public static final String LOGIN_TABLE="Login";//Login Table
	public static final String LOGIN_STATUS_COL="Status";//Login Status coloumn
	public static final String LOGIN_NICK_COL = "Nickname"; //Login nickname coloumn

	public static final String PHOTO_TABLE="Photo"; //Photo Table
	public static final String PHOTO_STATUS_COL="Status"; // Photo Status Table
	public static final String PHOTO_PATH_COL = "Path"; // Photo Path Table
	public static final String PHOTO_MD5_COL = "MD5"; // Photo MD5 Table. 
	
	public static final String DATAS_TABLE="Datas"; // Datas Table
	public static final String DATAS_STATUS_COL="Status"; // Datas Status Coloumn
	
	public static final String PHOTO_DEL_TABLE="Photo_del"; // Photo Delete Requests Table
	public static final String PHOTO_DEL_ID_COL="Photo_ID"; // This id represents the id in the PHOTO_TABLE


	public DatabaseHelper(Context context) {
		super(context,DATABASE,null,VERSION_CODE);
	}



	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+ REGISTER_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				REG_STATUS_COL +" TEXT NOT NULL, "+
				REG_NICK_COL +" TEXT NOT NULL,"+
				"UNIQUE (" + REG_NICK_COL + ") ON CONFLICT REPLACE )");
		
		db.execSQL("CREATE TABLE "+ LOGIN_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				LOGIN_STATUS_COL +" TEXT NOT NULL, "+
				LOGIN_NICK_COL +" TEXT NOT NULL,"+
				"UNIQUE (" + LOGIN_NICK_COL + ") ON CONFLICT REPLACE )");
		
		db.execSQL("CREATE TABLE "+ PHOTO_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				PHOTO_STATUS_COL +" TEXT NOT NULL, "+
				PHOTO_MD5_COL +" TEXT NOT NULL, "+
				PHOTO_PATH_COL +" TEXT NOT NULL,"+
				"UNIQUE (" + PHOTO_PATH_COL + ") ON CONFLICT REPLACE )");
		
		db.execSQL("CREATE TABLE "+ PHOTO_DEL_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY , "+
				PHOTO_STATUS_COL +" TEXT NOT NULL, "+
				PHOTO_MD5_COL +" TEXT NOT NULL, "+
				PHOTO_PATH_COL +" TEXT NOT NULL,"+
				PHOTO_DEL_ID_COL +" INTEGER NOT NULL,"+
				"UNIQUE (" + PHOTO_PATH_COL + ") ON CONFLICT REPLACE )");
		
		db.execSQL("CREATE TABLE "+ DATAS_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				DATAS_STATUS_COL +" TEXT NOT NULL)");
		
		Log.i("DatabaseHelper", "Database Created");
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion<newVersion){
			db.execSQL("DROP TABLE IF EXISTS "+REGISTER_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+LOGIN_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+PHOTO_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+DATAS_TABLE);
			onCreate(db);
		}
	}


}
