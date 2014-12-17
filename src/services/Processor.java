package services;

import java.io.File;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import contentprovider.BContentProvider;
import contentprovider.DatabaseHelper;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class Processor {

	private Context mContext;
	public static String REQUESTSENT= "REQUESTSENT";
	public static String STATUS_SUCCESS = "SUCCESS";
	public static String STATUS_FAIL= "FAIL";

	private static JSONObject objectDatas ;
	public Processor(Context context)
	{
		mContext = context;
	}

	/** Gets the request from LoadService and forwards the request to the HTTPClient. It also updates
	 * the ContentProvider about the status of the Register request. When the request is finished, it calls
	 * the ServiceHelper and informs it about the result
	 * 
	 * Registers the user on the server
	 * 
	 * @param params It has the information for the request, like nickname and password
	 * @param rCallback is the method to call when the request is finished.
	 */
	public void register(List<NameValuePair> params, Callback rCallback){
		ContentResolver cr=mContext.getContentResolver();

		ContentValues mNewValues = new ContentValues();

		mNewValues.put(DatabaseHelper.REG_STATUS_COL, REQUESTSENT);
		mNewValues.put(DatabaseHelper.REG_NICK_COL, "ERSETZE DAS");

		Uri newEntry =cr.insert(BContentProvider.REGISTER_URI, mNewValues);

		HTTPClient caller = new HTTPClient();

		JSONObject object = caller.register("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person",params);
		Log.i("Processor", newEntry.getPath());
		cr.delete(BContentProvider.REGISTER_URI, newEntry.getPath(), null);
		if(object!=null){
			rCallback.send(1);
		}else 
			rCallback.send(0);
	}
	
	/** Gets the request from LoadService and forwards the request to the HTTPClient. It also updates
	 * the ContentProvider about the status of the Register request. When the request is finished, it calls
	 * the ServiceHelper and informs it about the result
	 * 
	 * logs in the user on the server.
	 * @param params It has the information for the request, like nickname and password
	 * @param rCallback is the method to call when the request is finished.
	 */
	public void login(List<NameValuePair> params, Callback rCallback){
		ContentResolver cr=mContext.getContentResolver();

		ContentValues mNewValues = new ContentValues();

		mNewValues.put(DatabaseHelper.LOGIN_STATUS_COL, REQUESTSENT);
		mNewValues.put(DatabaseHelper.LOGIN_NICK_COL, params.get(0).getValue());

		Uri newEntry =cr.insert(BContentProvider.LOGIN_URI, mNewValues);

		HTTPClient caller = new HTTPClient();

		JSONObject object = caller.login("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person/login",params);
		Log.i("Processor", newEntry.getPath());
		ContentValues values = new ContentValues();


		if(object!=null){
			values.put(DatabaseHelper.LOGIN_STATUS_COL, STATUS_SUCCESS);
			rCallback.send(1);
		}else {
			values.put(DatabaseHelper.LOGIN_STATUS_COL, STATUS_FAIL);
			rCallback.send(0);
		}
		cr.update(BContentProvider.LOGIN_URI, values , newEntry.getPath(), null);

	}
	/** Gets the request from LoadService and forwards the request to the HTTPClient. It also updates
	 * the ContentProvider about the status of the Register request. When the request is finished, it calls
	 * the ServiceHelper and informs it about the result
	 * 
	 * uploads an image to the server
	 * @param params It has the information for the request, like nickname and password
	 * @param rCallback is the method to call when the request is finished.
	 */
	public void uploadPhoto(List<NameValuePair> params, String path,
			Callback rCallback, long reqId) {
		ContentResolver cr=mContext.getContentResolver();

		ContentValues mNewValues = new ContentValues();

		mNewValues.put(DatabaseHelper.PHOTO_STATUS_COL, REQUESTSENT);
		mNewValues.put(DatabaseHelper.PHOTO_PATH_COL, path);
		String MD5s = MD5.calculateMD5(new File(path));
		Log.i("Processor path", path);
		Log.i("Processor md5", MD5s);

		mNewValues.put(DatabaseHelper.PHOTO_MD5_COL, MD5s);
		mNewValues.put(BaseColumns._ID, reqId);

		Uri newEntry =cr.insert(BContentProvider.PHOTO_URI, mNewValues);

		HTTPClient caller = new HTTPClient();

		String object = caller.uploadPhoto("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person/upload",params , path);
		Log.i("Processor", newEntry.getPath());
		ContentValues values = new ContentValues();

		if(object!=null){
			values.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_SUCCESS);
			rCallback.send(1);
		}else {
			values.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_FAIL);
			rCallback.send(0);
		}
		cr.update(BContentProvider.PHOTO_URI, values , newEntry.getPath(), null);
	}
	
	
	/**
	 * Downloads a photo from the server
	 * @param params Information like username and password
	 * @param dataID Server-id of the file
	 * @param nameOfFile Name of the file
	 * @return true if the file was downloaded successfully, false if it was unsuccessful.
	 */
	private boolean downloadPhoto(List<NameValuePair> params, String dataID, String nameOfFile) {

		HTTPClient caller = new HTTPClient();
		return caller.downloadPhoto("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person/"+ dataID, params, nameOfFile);
	}
	
	/** Gets the request from LoadService and forwards the request to the HTTPClient. It also updates
	 * the ContentProvider about the status of the Register request. When the request is finished, it calls
	 * the ServiceHelper and informs it about the result
	 * 
	 * This Method downloads all the Information about the files by this user from the Server. It compares these files
	 * with the local database. If there are files, that are missing, these files will be downloaded.
	 * @param params It has the information for the request, like nickname and password
	 * @param rCallback is the method to call when the request is finished.
	 */
	public void getDatas(List<NameValuePair> params, String path,
			Callback rCallback) {
		ContentResolver cr=mContext.getContentResolver();
		ContentValues mNewValues = new ContentValues();

		mNewValues.put(DatabaseHelper.DATAS_STATUS_COL, REQUESTSENT);

		Uri newEntry =cr.insert(BContentProvider.DATAS_URI, mNewValues);

		HTTPClient caller = new HTTPClient();

		objectDatas = caller.downloadDatas("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person/dataslist",params );
		if(objectDatas==null){
			ContentValues values = new ContentValues();
			values.put(DatabaseHelper.DATAS_STATUS_COL, STATUS_FAIL);
			rCallback.send(0);
			return;
		}
		Object object1;
		try {
			object1 = objectDatas.get("datas");

			if (object1 instanceof JSONArray) {

				JSONArray array = (JSONArray) object1;
				final String[] EVENT_PROJECTION = new String[] {
						DatabaseHelper.PHOTO_MD5_COL
				};
				Cursor cur = null;
				Uri uri = BContentProvider.PHOTO_URI;   
				String selection = "((" + DatabaseHelper.PHOTO_MD5_COL + " = ?))";
				for(int i = 0; i < array.length() ; i++){
					String datasMD5=  (String) array.getJSONObject(i).get("MD5");
					String picName = array.getJSONObject(i).getString("name");
					// Run query				
					String[] selectionArgs = new String[] {datasMD5}; 
					// Submit the query and get a Cursor object back. 
					cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
					if(!cur.moveToNext()){
						Log.i("Processor cursor", cur.toString());
						boolean bool = downloadPhoto(params, (String) array.getJSONObject(i).get("id"), picName);
						if(bool){
							ContentValues mNewValues1 = new ContentValues();
							mNewValues1.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_SUCCESS);
							mNewValues1.put(DatabaseHelper.PHOTO_MD5_COL, datasMD5);
							mNewValues1.put(DatabaseHelper.PHOTO_PATH_COL, "/storage/sdcard/bakk/"  + array.getJSONObject(i).get("name"));
							cr.insert(BContentProvider.PHOTO_URI, mNewValues1);
						}
					}
				}
			}

			else if (object1 instanceof JSONObject) {
				JSONObject object2 = (JSONObject) object1;
				final String[] EVENT_PROJECTION = new String[] {
						DatabaseHelper.PHOTO_MD5_COL
				};
				Cursor cur = null;
				Uri uri = BContentProvider.PHOTO_URI;   
				String selection = "((" + DatabaseHelper.PHOTO_MD5_COL + " = ?))";
				//JSONObject object1 = object.getJSONObject("datas");
				String datasMD5=  (String) object2.get("MD5");
				String picName = object2.getString("name");
				// Run query				
				String[] selectionArgs = new String[] {datasMD5}; 
				// Submit the query and get a Cursor object back. 
				cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
				Log.i("Processor MD5", datasMD5);
				if(!cur.moveToNext()){
					Log.i("Processor cursor", cur.toString());
					boolean bool = downloadPhoto(params, (String) object2.getString("id"),picName);
					if(bool){
						ContentValues mNewValues1 = new ContentValues();
						mNewValues1.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_SUCCESS);
						mNewValues1.put(DatabaseHelper.PHOTO_MD5_COL, datasMD5);
						mNewValues1.put(DatabaseHelper.PHOTO_PATH_COL, "/storage/sdcard/bakk/"  + object2.getString("name"));
						cr.insert(BContentProvider.PHOTO_URI, mNewValues1);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i("Processor", newEntry.getPath());
		ContentValues values = new ContentValues();


		if(objectDatas!=null){
			values.put(DatabaseHelper.DATAS_STATUS_COL, STATUS_SUCCESS);

			rCallback.send(1);
		}
		cr.update(BContentProvider.DATAS_URI, values , newEntry.getPath(), null);
	}
	
	/** Gets the request from LoadService and forwards the request to the HTTPClient. It also updates
	 * the ContentProvider about the status of the Register request. When the request is finished, it calls
	 * the ServiceHelper and informs it about the result
	 * 
	 * Deletes the image from the server.
	 * @param params It has the information for the request, like nickname and password
	 * @param rCallback is the method to call when the request is finished.
	 */
	public void deletePhoto(List<NameValuePair> params, String path,
			Callback rCallback) {
		ContentResolver cr=mContext.getContentResolver();

		ContentValues mNewValues = new ContentValues();

		mNewValues.put(DatabaseHelper.PHOTO_STATUS_COL, REQUESTSENT);
		mNewValues.put(DatabaseHelper.PHOTO_PATH_COL, path);
		String MD5s = MD5.calculateMD5(new File(path));
		mNewValues.put(DatabaseHelper.PHOTO_DEL_ID_COL, getServerDataID(MD5s));
		mNewValues.put(DatabaseHelper.PHOTO_MD5_COL, MD5s);

		Log.i("Processor Deleted Photo Path", path);
		Log.i("Processor Deleted File MD5", MD5s);

		Uri newEntry =cr.insert(BContentProvider.PHOTO_DEL_URI, mNewValues);

		HTTPClient caller = new HTTPClient();

		String object = caller.deletePhoto("https://10.0.2.2:8443/BakkArbeit.Server2/rest/person/delete/"+ getServerDataID(MD5s) , params);
		Log.i("Processor", newEntry.getPath());
		ContentValues values = new ContentValues();

		if(object!=null){

			String mSelectionClause = DatabaseHelper.PHOTO_MD5_COL + "=?";
			String[] mSelectionArgs = {MD5s};

			cr.delete(BContentProvider.PHOTO_URI, mSelectionClause, mSelectionArgs);

			values.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_SUCCESS);
			rCallback.send(1);
		}else {
			values.put(DatabaseHelper.PHOTO_STATUS_COL, STATUS_FAIL);
			rCallback.send(0);
		}
		cr.update(BContentProvider.PHOTO_DEL_URI, values , newEntry.getPath(), null);
	}	

	/**Determines the Id of this file on the Server. The Server-id of the file is needed to access
	 * a file on the server
	 * @param MD5 MD5 of the file, of which the id is needed.
	 * @return the id of the file. -1 if it wasn't found.
	 */
	private int getServerDataID(String MD5){
		try {
			Object object1 = objectDatas.get("datas");
			if (object1 instanceof JSONArray) {

				JSONArray array = (JSONArray) object1;
				for(int i = 0; i < array.length() ; i++){
					String datasMD5=  (String) array.getJSONObject(i).get("MD5");
					if(datasMD5.equals(MD5)){
						int picID = array.getJSONObject(i).getInt("id");
						return picID;
					}
				}
			}

			else if (object1 instanceof JSONObject) {
				JSONObject object2 = (JSONObject) object1;
				String datasMD5=  object2.getString("MD5");
				if(datasMD5.equals(MD5)){
					int picID = object2.getInt("id");
					return picID;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		}
		return -1;

	}
}
