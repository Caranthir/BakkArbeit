package contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	

	public static final String DATABASE="back";
	private static final int VERSION_CODE=4;
	
	public static final String REGISTER_TABLE="Register";//Register Table
	public static final String REG_STATUS_COL="Status";//Register Status coloumn
	public static final String REG_NICK_COL="Nickname";//Register Nickname coloumn


	/*public static final String TWEET_TABLE="Tweet";//Twitter Table
	public static final String HASHTAG_DESCRIPTION_COL="Description";
	
	public static final String TWEET_COL="Tweet";
	public static final String TWEET_USER_COL="User";//@NECRONET
	public static final String TWEET_ID_COL="TweetId";
	public static final String TWEET_IMAGE_URL_COL="ImageUrl";
	public static final String TWEET_CREATED_AT_COL="CreatedAt";
	public static final String TWEET_USER_NAME_COL = "UserNameTweet";//Jose Ayerdis
	public static final String TWEET_USER_TABLE = "UserTweet";
	
	public static final String ID_INFORMATION_TABLE = "IdInformation";
	public static final String CEDULA_COL="Cedula";//Cedula
	public static final String NOMBRE_COMPLETO_COL="FullName";//NOMBRE completo
	public static final String JRV_COL="JRV";//Junta receptora de voto
	public static final String CV_COL="CV";//Centro de votacion
	public static final String DEPARTAMENTO_COL="Departamento";//Departamento
	public static final String MUNICIPIO_COL="Municipio";//Municipio
	public static final String DISTRITO_COL="Distrito";//DISTRITO
	public static final String UBICACION_COL="Ubicacion";//Ubicacion
	public static final String DIRECCION_COL="Direccion";//Direccion*/
	
	
	public DatabaseHelper(Context context) {
		super(context,DATABASE,null,VERSION_CODE);
	}



	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "+ REGISTER_TABLE +" (" + 
				BaseColumns._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				REG_STATUS_COL +" TEXT NOT NULL, "+
				"UNIQUE (" + REG_NICK_COL + ") ON CONFLICT REPLACE )");
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion<newVersion){
			db.execSQL("DROP TABLE IF EXISTS "+REGISTER_TABLE);
			onCreate(db);
		}
	}


}
