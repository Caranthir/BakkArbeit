package contentprovider;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.UserDictionary;

public class Processor {

	private Context mContext;
	public static String REQUESTSENT= "REQUESTSENT";
	

	public Processor(Context context)
	{
		mContext = context;
	}

	void getData(String nickname){
			ContentResolver cr=mContext.getContentResolver();

			ContentValues mNewValues = new ContentValues();

			mNewValues.put(DatabaseHelper.REG_STATUS_COL, REQUESTSENT);
			mNewValues.put(DatabaseHelper.REG_NICK_COL, nickname);


			cr.insert(BContentProvider.CONTENT_URI, mNewValues);
			
			//cr.delete(EliGContentProvider.HASHTAG_CONTENT_URI, null, null);
			/*ContentValues[] contentValues=new ContentValues[hashtagsJSON.length()];
			for (int i = 0; i < hashtagsJSON.length(); i++) {
				ContentValues cv=new ContentValues();
				cv.put(EliGDatabase.HASHTAG_DESCRIPTION_COL,hashtagsJSON.getString(i));
				contentValues[i]=cv;
				cr.insert(EliGContentProvider.HASHTAG_CONTENT_URI, cv);
			}*/

			//ServiceHelper.getInstance(context).startService(EliGActions.TWITTER_LIST_ACTION);

			/*SharedPreferences settings = context.getSharedPreferences(Clov3rConstant.PREFERENCES, 0);
			SharedPreferences.Editor editor = settings.edit();*/
			/*editor.putBoolean(Clov3rConstant.LOADED_HASHTAG_PREF, true);
			// Commit the edits!
			editor.commit();*/

	}
}
