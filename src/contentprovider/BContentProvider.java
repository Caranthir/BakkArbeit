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
	private static final int LOGIN = 102;
	private static final int PHOTO = 103;
	private static final int DATAS = 104;
	private static final int PHOTO_DEL = 105;


	private static final String AUTHORITY = "BakkArbeit.contentprovider";
	private DatabaseHelper databaseHelper;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final Uri REGISTER_URI = Uri.parse("content://" + AUTHORITY+ "/register");
	public static final Uri LOGIN_URI = Uri.parse("content://" + AUTHORITY+ "/login");
	public static final Uri PHOTO_URI = Uri.parse("content://" + AUTHORITY+ "/photo");
	public static final Uri DATAS_URI = Uri.parse("content://" + AUTHORITY+ "/datas");
	public static final Uri PHOTO_DEL_URI = Uri.parse("content://" + AUTHORITY+ "/photodel");


	private static final UriMatcher uriMatcher = buildMatcher();


	private static UriMatcher buildMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(AUTHORITY, "register", REGISTER);
		matcher.addURI(AUTHORITY, "login", LOGIN);
		matcher.addURI(AUTHORITY, "photo", PHOTO);
		matcher.addURI(AUTHORITY, "datas", DATAS);
		matcher.addURI(AUTHORITY, "photodel", PHOTO_DEL);
		
		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		String finalWhere=null;
		int count;
		String table=null;
		Log.i("BContentProvider delete", "entered delete^" + uriMatcher.match(uri));
		switch (uriMatcher.match(uri)) {

		case REGISTER:
			//finalWhere = BaseColumns._ID + " = " + getId(uri);
			finalWhere = BaseColumns._ID + " = " + Integer.parseInt(selection.substring(1));
			table=DatabaseHelper.REGISTER_TABLE;
			break;
		case LOGIN:
			//finalWhere = BaseColumns._ID + " = " + getId(uri);
			finalWhere = BaseColumns._ID + " = " + Integer.parseInt(selection.substring(1));
			table=DatabaseHelper.LOGIN_TABLE;
			break;
		case PHOTO:
			//finalWhere = BaseColumns._ID + " = " + getId(uri);
		//	finalWhere = BaseColumns._ID + " = " + Integer.parseInt(selection.substring(1));
			finalWhere = selection;
			table=DatabaseHelper.PHOTO_TABLE;
			break;
		case DATAS:
			//finalWhere = BaseColumns._ID + " = " + getId(uri);
			finalWhere = BaseColumns._ID + " = " + Integer.parseInt(selection.substring(1));
			table=DatabaseHelper.DATAS_TABLE;
			break;
		case PHOTO_DEL:
			//finalWhere = BaseColumns._ID + " = " + getId(uri);
			finalWhere = BaseColumns._ID + " = " + Integer.parseInt(selection.substring(1));
			table=DatabaseHelper.PHOTO_DEL_TABLE;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		count = db.delete(table, finalWhere ,selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	public String getId(Uri uri) {
		return uri.getPathSegments().get(0);
		//IT WAS 1
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
		case LOGIN:
			id = getId(uri);
			builder.setTables(DatabaseHelper.LOGIN_TABLE);
			break;
		case PHOTO:
			id = getId(uri);
			builder.setTables(DatabaseHelper.PHOTO_TABLE);
			break;
		case DATAS:
			id = getId(uri);
			builder.setTables(DatabaseHelper.DATAS_TABLE);
			break;
		case PHOTO_DEL:
			id = getId(uri);
			builder.setTables(DatabaseHelper.PHOTO_DEL_TABLE);
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
			long _id = db.insertOrThrow(DatabaseHelper.REGISTER_TABLE, null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
			//return buildUri(HASHTAG_CONTENT_URI, String.valueOf(_id));
		}
		case LOGIN: {
			long _id = db.insertOrThrow(DatabaseHelper.LOGIN_TABLE, null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
		}
		case PHOTO:{
			long _id = db.insertOrThrow(DatabaseHelper.PHOTO_TABLE , null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
		}
		case DATAS:{
			long _id = db.insertOrThrow(DatabaseHelper.DATAS_TABLE , null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
		}
		case PHOTO_DEL:{
			long _id = db.insertOrThrow(DatabaseHelper.PHOTO_DEL_TABLE , null,
					values);
			getContext().getContentResolver().notifyChange(uri, null);
			return Uri.parse("/" +_id);
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
	/**
	 * @param selection only an ID
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		final SQLiteDatabase db = databaseHelper.getWritableDatabase();
		final int match = uriMatcher.match(uri);

		String finalWhere = BaseColumns._ID + " = " + Long.parseLong(selection.substring(1));

		switch (match) {
		case REGISTER: {
			return 0;
		}
		case LOGIN: {
			return db.update(DatabaseHelper.LOGIN_TABLE, values, finalWhere, selectionArgs);
		}
		case PHOTO:{
			return db.update(DatabaseHelper.PHOTO_TABLE, values, finalWhere, selectionArgs);
		}
		case PHOTO_DEL:{
			return db.update(DatabaseHelper.PHOTO_DEL_TABLE, values, finalWhere, selectionArgs);
		}
		case DATAS:{
			return db.update(DatabaseHelper.DATAS_TABLE, values, finalWhere, selectionArgs);
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}
}
