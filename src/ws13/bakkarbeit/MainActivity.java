package ws13.bakkarbeit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity {

	private static final int RESULT_LOAD_IMAGE = 0;
	private static final int RESULT_LOAD_CONTACTS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//	GridView gridview = (GridView) findViewById(R.id.gridview);
		//    gridview.setAdapter(new ImageAdapter(this));

		/*gridview.setOnItemClickListener(new OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		            Toast.makeText(HelloGridView.this, "" + position, Toast.LENGTH_SHORT).show();
		        }
		    });*/

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

	/** Called when the user clicks the Contacts button 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException */
	public void getContacts(View view) throws ParserConfigurationException, TransformerException {

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
		createXML(cursor);


		/*if(cursor==null){
			System.out.println("null");
		}else if (cursor.getCount() < 1) {
			System.out.println("empty");
		}else {
			while (cursor.moveToNext()){
				int index= cursor.getColumnIndex(Data.DATA1);
				System.out.println(cursor.getString(index));
				int index1= cursor.getColumnIndex(Data.MIMETYPE);
				System.out.println("DATA2 " + cursor.getString(index1));
			}
		}*/


	}    

	private void createXML(Cursor cursor) throws ParserConfigurationException, TransformerException{

		DocumentBuilderFactory documentBuilderFactory;
		DocumentBuilder documentBuilder;
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();


		Element root = document.createElement("contacts");
		document.appendChild(root);

		//System.out.println(players.size() + " " +p.getGender() );

		Element contact;
		cursor.moveToNext();

		int index1= cursor.getColumnIndex(Data.RAW_CONTACT_ID);
		String rawId = cursor.getString(index1);

		contact = document.createElement("contact");

		int indexVar= cursor.getColumnIndex(Data.DATA1);
		String var = cursor.getString(indexVar);

		int indexType= cursor.getColumnIndex(Data.MIMETYPE);
		String type = cursor.getString(indexType);

		System.out.println("var " + var+ "type "+type.substring(23));

		contact.setAttribute(type.substring(24), var+"");


		while(cursor.moveToNext()){

			index1= cursor.getColumnIndex(Data.RAW_CONTACT_ID);
			String rawIdNew = cursor.getString(index1);

			if(!rawIdNew.equals(rawId)){
				root.appendChild(contact);
				contact = document.createElement("contact");
				System.out.println("HOOP");
				rawId=rawIdNew;
			}
			indexVar= cursor.getColumnIndex(Data.DATA1);
			var = cursor.getString(indexVar);

			indexType= cursor.getColumnIndex(Data.MIMETYPE);
			type = cursor.getString(indexType);
			System.out.println("var " + var+ "type "+type);

			contact.setAttribute(type.replace("/", ""), var);
		}
		root.appendChild(contact);


		TransformerFactory tFactory =TransformerFactory.newInstance();
		Transformer transformer =tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(document);
		File file = new File(getFilesDir(), "contacts.xml");
		StreamResult result =new StreamResult(file);
		transformer.transform(source, result);
		System.out.println(file.toString());


		/*BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(file));

			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		 */

	}

	/**
	 * copied from http://viralpatel.net/blogs/pick-image-from-galary-android-app/
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media._ID};

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			System.out.println("id dfsdf");

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			cursor.moveToNext();
			String picturePath = cursor.getString(columnIndex);

			//String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME,  ContactsContract.CommonDataKinds.Phone.NUMBER};
			//int[] toViews = {R.id.imageview};
			ImageView imageView;
			imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			/*imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);*/
			System.out.println("id "+ imageView.getId());
			imageView.setId(5465465);
			System.out.println("id "+ imageView.getId());
			int[] toViews = {imageView.getId()};


			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
					R.layout.image, cursor, filePathColumn, toViews, 0);

			GridView gridview = (GridView) findViewById(R.id.gridview);
			gridview.setAdapter(adapter);
			adapter.notifyDataSetChanged();

			//cursor.close();

			// String picturePath contains the path of selected Image
		}
	}
}
