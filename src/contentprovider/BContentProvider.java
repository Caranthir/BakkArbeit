package contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BContentProvider extends ContentProvider {
	
	private static final int REGISTER = 100;
	private static final int REGISTER_ID = 101;

	
	private static final String AUTHORITY = "contentprovider";
	private DatabaseHelper databaseHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private static final UriMatcher uriMatcher = buildMatcher();

	
	private static UriMatcher buildMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(AUTHORITY, "register", REGISTER);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String finalWhere=null;
		int count;
		String table=null;
		switch (uriMatcher.match(uri)) {

		case REGISTER:
			table=databaseHelper.REGISTER_TABLE;
			break;
		case REGISTER_ID:
			finalWhere = BaseColumns._ID + " = " + getId(uri);
			if (selection != null) {
				finalWhere = finalWhere + " AND " + selection;
			}
			table=databaseHelper.REGISTER_TABLE;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.delete(table, finalWhere,selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}
	
	public String getId(Uri uri) {
		return uri.getPathSegments().get(1);
	}
	
	private SQLiteQueryBuilder buildExpandedSelection(Uri uri, int match) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		String id = null;
		switch (match) {
		case REGISTER:
			builder.setTables(DatabaseHelper.REGISTER_TABLE);
			break;

		case REGISTER_ID:
			id = getId(uri);
			builder.setTables(DatabaseHelper.REGISTER_TABLE);
			builder.appendWhere(BaseColumns._ID + "=" + id);
			break;
		}
		return builder;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final int match = uriMatcher.match(uri);
		
		switch (match) {
		case REGISTER: {
			long _id = db.insertOrThrow(DatabaseHelper.DATABASE, null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
			//return buildUri(HASHTAG_CONTENT_URI, String.valueOf(_id));
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	@Override
	public boolean onCreate() {
		databaseHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		final int match = uriMatcher.match(uri);

		final SQLiteDatabase db = databaseHelper.getReadableDatabase();
		
		final SQLiteQueryBuilder builder = buildExpandedSelection(uri, match);
		Cursor c = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);

		return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
