package ws13.bakkarbeit;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private static final int RESULT_LOAD_IMAGE = 0;
	private static final int RESULT_LOAD_CONTACTS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called when the user clicks the Pictures button */
	public void sendMessage(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
		startActivityForResult(i, RESULT_LOAD_IMAGE);

	}    

	/** Called when the user clicks the Contacts button */
	public void getContacts(View view) {
		Intent i = new Intent(Intent.ACTION_PICK,  Data.CONTENT_URI ); 

		final String[] projection =
			{
				Data._ID,
				Data.RAW_CONTACT_ID,
				Data.MIMETYPE,
				Data.IS_PRIMARY,
				Data.IS_SUPER_PRIMARY,
				Data.DATA1,
				Data.DATA2,
				Data.DATA3,
				Data.DATA4,
				Data.DATA5,
				Data.DATA6,
				Data.DATA7,
				Data.DATA8,
				Data.DATA9,
				Data.DATA10,
				Data.DATA11,
				Data.DATA12,
				Data.DATA13,
				Data.DATA14,
				Data.DATA15
			};
		Cursor cursor = getContentResolver().query(Data.CONTENT_URI, projection, null, null, null);



		if(cursor==null){
			System.out.println("null");
		}else if (cursor.getCount() < 1) {
			System.out.println("empty");
		}else {
			while (cursor.moveToNext()){
				int index= cursor.getColumnIndex(Data.RAW_CONTACT_ID);
				System.out.println(cursor.getString(index));
			}
		}

		//startActivityForResult(i, RESULT_LOAD_CONTACTS);

	}    

	/**
	 * copied from http://viralpatel.net/blogs/pick-image-from-galary-android-app/
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			// String picturePath contains the path of selected Image
		}
	}
}
