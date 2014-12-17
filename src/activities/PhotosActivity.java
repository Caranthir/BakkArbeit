package activities;

import java.io.File;
import java.util.ArrayList;

import services.LoadService;
import services.MD5;
import services.Processor;
import services.ServiceHelper;

import ws13.bakkarbeit.R;

import contentprovider.BContentProvider;
import contentprovider.DatabaseHelper;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/** This activity manages the photos, that are on the 
 * @author caranthir
 *
 */
public class PhotosActivity extends Activity {

	private static final int RESULT_LOAD_IMAGE = 0;
	AccountManager mAccountManager;
	private BroadcastReceiver requestReceiver;
	private BroadcastReceiver requestReceiverDatas;
	private BroadcastReceiver requestReceiverDelete;

	ArrayList<String> uploadedPhotos = new ArrayList<String>(); //Photos will be saved with their path 
	public ArrayList<String> toBeUploadedPhotos = new ArrayList<String>(); //Photos will be saved with their path
	public ArrayList<Long> uploadingPhotos = new ArrayList<Long>();
	public long datasReqID= -1; // If this is -1, it means there are no waiting datasRequest
	GridView gridview;
	ImageAdapter adapter;


	/** Method called when this actiVity is created.
	 * It sets the onItemClickListener for the GridView. When an item on the GridView is clicked, the Activity
	 * asks the user if this item should be deleted. If yes photoDeleteTask is called to delete this item.
	 * 
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mAccountManager = AccountManager.get(getBaseContext());		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		gridview = (GridView) findViewById(R.id.gridview);
		adapter = new ImageAdapter(this);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
				if(!ServiceHelper.isInternetAvailable(getBaseContext())){
					showToast("No Internet Connection Available");
					return;
				}
				AlertDialog alertDialog = new AlertDialog.Builder(PhotosActivity.this).create();
				alertDialog.setTitle("This photo will be removed.");
				alertDialog.setMessage("Are you sure?");

				class ClickListener implements OnClickListener{
					public void onClick(DialogInterface dialog, int which) {
						if(which == DialogInterface.BUTTON_POSITIVE){
							photoDeleteTask photodeleteTask = new photoDeleteTask();
							photodeleteTask.execute(ImageAdapter.photosPath.get(position));
							ImageAdapter.photosPath.remove(position);
							adapter.notifyDataSetChanged();
						}
					}

				};

				ClickListener clicklistener = new ClickListener();
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", clicklistener );
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",clicklistener);		


				//alertDialog.setIcon(R.drawable.icon);
				alertDialog.show();
				Toast.makeText(PhotosActivity.this, "" + position +" deleted "+ id, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/** It updates the ImageAdapter of the ImageView, if there are any changes in the database.
	 * 
	 */
	private void updateImageAdapter(){
		Uri uri = BContentProvider.PHOTO_URI;   
		Log.i("PhotosActivity", "entered updateImageAdapter" );
		ContentResolver cr= getContentResolver();

		String selection = "((" + DatabaseHelper.PHOTO_STATUS_COL + " = ?))";
		String[] selectionArgs = new String[] {Processor.STATUS_SUCCESS}; 
		// Run query				
		// Submit the query and get a Cursor object back. 
		Cursor cur = cr.query(uri, null, selection, selectionArgs, "_id DESC");
		//cur.moveToFirst();
		Log.i("PhotosActivity photos in dbUP", cur.getCount()+uploadingPhotos.size()+ " " + ImageAdapter.photosPath.size() );
		
		//if photo in the db and the count of uploading photos(that will be added to db later) is the same as the photos on the Screen,
		//we dont need to check it
		if(cur.getCount()+uploadingPhotos.size() == ImageAdapter.photosPath.size()){
			return;
		}
		while(cur.moveToNext()){
			int columnIndex = cur.getColumnIndex(DatabaseHelper.PHOTO_PATH_COL);
			String picturePath = cur.getString(columnIndex);
			if(!ImageAdapter.photosPath.contains(picturePath)){
				Log.i("PhotosActivity photos in PAth", picturePath);
				ImageAdapter.photosPath.add(picturePath);
				//ImageAdapter.photos.add(Uri.parse(new File(picturePath).toString()));
				uploadedPhotos.add(picturePath);
			}
		}
		adapter.notifyDataSetChanged();
	}
	
	
	
	
	/** First it registers three BroadcastReceivers
	 * One of them is to get the result of the image upload process , one is for receiving the result of the Delete
	 * process and one of them is for receiving the result of the Datas download process.
	 * Then it checks if there is already an ongoing Datas download process or image upload process.If yes, the result
	 * of this process received through the Database and shown to the user.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(ServiceHelper.ACTION_PHOTO_RESULT);
		updateImageAdapter();

		requestReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				long resultRequestId = intent
						.getLongExtra(ServiceHelper.EXTRA_REQUEST_ID, 0);
				Log.i("PhotoActivity", "Active 1 "+ resultRequestId);

				//final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);


				int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);
				String path = intent.getStringExtra("path");

				Log.i("PhotoActivity", "Result code = " + resultCode);


				if (resultCode == 1) {
					uploadedPhotos.add(path);
					uploadingPhotos.remove(resultRequestId);
					toBeUploadedPhotos.remove(path);
					showToast("Photo " + resultRequestId +" uploaded");
				} else {
					ImageAdapter.photosPath.remove(path);
					adapter.notifyDataSetChanged();
					showToast("Unsuccessful");
				}

			}
		};

		this.registerReceiver(requestReceiver, filter);
		
		IntentFilter filter2 = new IntentFilter(ServiceHelper.ACTION_DELETE_RESULT);

		requestReceiverDelete = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				long resultRequestId = intent
						.getLongExtra(ServiceHelper.EXTRA_REQUEST_ID, 0);
				Log.i("PhotoActivity", "Active 1 "+ resultRequestId);

				//final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);


				int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);
				String path = intent.getStringExtra("path");

				Log.i("PhotoActivity", "Result code = " + resultCode);


				if (resultCode == 1) {
					showToast("Photo " + resultRequestId +" deleted");
				} else {
					ImageAdapter.photosPath.add(path);
					adapter.notifyDataSetChanged();
					showToast("Unsuccessful delete");
				}

			}
		};

		this.registerReceiver(requestReceiverDelete, filter2);

		IntentFilter filter1 = new IntentFilter(ServiceHelper.ACTION_DATAS_RESULT);

		requestReceiverDatas = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				datasReqID= -1;
				Log.i("PhotoActivity", "Broadcast Receiver Datas ");

				int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);

				Log.i("PhotoActivity", "Result code = " + resultCode);

				if (resultCode == 1) {
					updateImageAdapter();				
					showToast("datas Received");
				} else {
					showToast("Unsuccessful");
				}
			}
		};

		ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
		
		// In case the the Activity was paused before the Datas were downloaded, it checks if there is a datas Req
		//in Process or finished and if so gets the result 
		
		if(datasReqID>-1){
			if(sh.isRequestPending(datasReqID)){
				this.registerReceiver(requestReceiverDatas, filter1);
			}else{
				datasReqID=-1;
				Uri uri = BContentProvider.PHOTO_URI;   

				ContentResolver cr= getContentResolver();

				String selection = "((" + DatabaseHelper.PHOTO_STATUS_COL + " = ?))";
				String[] selectionArgs = new String[] {Processor.STATUS_SUCCESS}; 
				Cursor cur = cr.query(uri, null, selection, selectionArgs, "_id DESC");
				Log.i("PhotosActivity photos in db", cur.getCount()+ " ");
				while(cur.moveToNext()){
					int columnIndex = cur.getColumnIndex(DatabaseHelper.PHOTO_PATH_COL);
					String picturePath = cur.getString(columnIndex);
					if(!ImageAdapter.photosPath.contains(picturePath)){
						ImageAdapter.photosPath.add(picturePath);
						uploadedPhotos.add(picturePath);
					}
				}
				adapter.notifyDataSetChanged();
			}
		}else{
			this.registerReceiver(requestReceiverDatas, filter1);
		}

		datasDownloadTask datasTask = new datasDownloadTask();
		datasTask.execute();

	
		Log.i("PhotosActivity uplPhotos", uploadingPhotos.size() + " ");

		if(!uploadingPhotos.isEmpty()){
			Log.i("PhotosActivity", "onResume uploadingPhotos not Empty");
			ArrayList<Long> uploadingPhotosCopy = (ArrayList<Long>) uploadingPhotos.clone();
			ContentResolver cr= getContentResolver();
			for(long requestId: uploadingPhotosCopy){
				Log.i("PhotosActivity uplPhotos", "checking if "+ requestId + "is Pending");

				if(!sh.isRequestPending(requestId)){
					Log.i("PhotosActivity uplPhotos", requestId + "is NOT Pending");

					final String[] EVENT_PROJECTION = new String[] {DatabaseHelper.PHOTO_PATH_COL, DatabaseHelper.PHOTO_STATUS_COL};
					Uri uri = BContentProvider.PHOTO_URI;   
					String selection  ="((" + BaseColumns._ID + " = ?))";
					// Run query		

					String[] selectionArgs = new String[] {(requestId+"")}; 
					// Submit the query and get a Cursor object back. 
					Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
					cur.moveToFirst();
					Log.i("PhotosActivity uplPhotos", requestId + " is "+ cur.getString(1));

					if(cur.moveToFirst()){
						if(cur.getString(1).equals(Processor.STATUS_SUCCESS)){
							showToast("Photo " +cur.getString(1) + " was uploaded succesfully");
							uploadingPhotos.remove(requestId);
						}else if(cur.getString(1).equals(Processor.REQUESTSENT )){
							showToast("Photo " +cur.getString(1) + " still uploading");
						}else if(cur.getString(1).equals(Processor.STATUS_FAIL )){
							showToast("Photo " +cur.getString(1) + " failed to upload. trying again");
							photoUploadTask photoTask = new photoUploadTask();
							photoTask.execute(cur.getString(1));
						}
					}
				}else{
					Log.i("PhotosActivity uplPhotos", requestId + "IS Pending");
					showToast("Photo still uploading");
				}
			}
		}

		Log.i("PhotosActivity fuplPhotos", uploadingPhotos.size() + " ");
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photos, menu);
		return true;
	}

	/** Called when the user clicks the Pictures button */
	
	/** Fails if there is no internet connection. It sends an Intent to open the Image Gallery of the device
	 * @param view
	 */
	public void sendMessage(View view) {
		if(!ServiceHelper.isInternetAvailable(getBaseContext())){
			showToast("No Internet Connection Available");
			return;
		}
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
		startActivityForResult(i, RESULT_LOAD_IMAGE);

	}
	public void syncPhotos(View view){
		if(!ServiceHelper.isInternetAvailable(getBaseContext())){
			showToast("No Internet Connection Available");
			return;
		}
		syncPhotos();
	}
	
	/**Sync the images with the server
	 * 
	 */
	public void syncPhotos(){
		if(!ServiceHelper.isInternetAvailable(getBaseContext())){
			showToast("No Internet Connection Available");
			return;
		}
		for(String path : toBeUploadedPhotos){
			photoUploadTask photoTask = new photoUploadTask();
			photoTask.execute(path);
		}
		toBeUploadedPhotos.clear();
	}

	/** All BroadcastReceivers are unregistered
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (requestReceiverDelete != null) {
			try {
				this.unregisterReceiver(requestReceiverDelete);
				Log.i("PhotosActivity", "paused and unregistered receiver");
			} catch (IllegalArgumentException e) {
				Log.e("PhotosActivity", e.getLocalizedMessage());
			}
		}
		// Unregister for broadcast
		if (requestReceiver != null) {
			try {
				this.unregisterReceiver(requestReceiver);
				Log.i("PhotosActivity", "paused and unregistered receiver");
			} catch (IllegalArgumentException e) {
				Log.e("PhotosActivity", e.getLocalizedMessage());
			}
		}
		if (requestReceiverDatas != null) {
			try {
				this.unregisterReceiver(requestReceiverDatas);
			} catch (IllegalArgumentException e) {
				Log.e("PhotosActivity", e.getLocalizedMessage());
			}
		}
	}

	private void showToast(String message) {
		if (!isFinishing()) {
			Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	/** Gets the result of Image Choosing from the Gallery Activity and adds it the GridView and syncs
	 *  with the server
	 * partly copied from http://viralpatel.net/blogs/pick-image-from-galary-android-app/
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			cursor.moveToNext();
			String picturePath = cursor.getString(columnIndex);


			if(ImageAdapter.photosPath.contains(picturePath)){
				return;
			}
			ContentResolver cr= getContentResolver();

			String selection = "((" + DatabaseHelper.PHOTO_MD5_COL + " = ?))";
			String[] selectionArgs = new String[] {MD5.calculateMD5(new File(picturePath))}; 
			// Run query				
			// Submit the query and get a Cursor object back. 
			Cursor cur = cr.query(BContentProvider.PHOTO_URI, null, selection, selectionArgs, "_id DESC");
			
			if(cur.getCount()!=0){
				return;
			}
			ImageAdapter.photosPath.add(picturePath);

			toBeUploadedPhotos.add(picturePath);

			adapter.notifyDataSetChanged();
			showToast("new file added to adapter "+ picturePath);
			syncPhotos();

		}
	}
	
	/** Gets the User Account Information from the AccountManager and instantiates the ServiceHelper to upload an
	 * Image to the Server
	 */
	public class photoUploadTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
			Account account = availableAccounts[0];
			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("password", mAccountManager.getPassword(account));
			extras.putString("nickname",account.name);
			extras.putString("path", params[0]);
			Log.i("PhotosActivity", params[0]+ "");
			uploadingPhotos.add(sh.instantiate(LoadService.UPLOAD_PHOTO, extras));
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}

		@Override
		protected void onCancelled() {
		}
	}
	
	/** Gets the User Account Information from the AccountManager and instantiates the ServiceHelper to download
	 * Datas Information from the Server
	 */ 
	public class datasDownloadTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
			Account account = availableAccounts[0];
			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("password", mAccountManager.getPassword(account));
			extras.putString("nickname",account.name);
			datasReqID = sh.instantiate(LoadService.DOWNLOAD_DATAS, extras);
			Log.i("datasDownloadTask",  datasReqID+"");
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}

		@Override
		protected void onCancelled() {
		}
	}
	
	/** Gets the User Account Information from the AccountManager and instantiates the ServiceHelper to delete an
	 * Image from the Server
	 */
	public class photoDeleteTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);
			Account account = availableAccounts[0];
			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("password", mAccountManager.getPassword(account));
			extras.putString("nickname",account.name);
			extras.putString("path", params[0]);
			sh.instantiate(LoadService.DELETE_PHOTO, extras);
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}

		@Override
		protected void onCancelled() {
		}
	}
}
