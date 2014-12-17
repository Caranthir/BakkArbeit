package activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ws13.bakkarbeit.R;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;



/** This Activity show the menu with 3 Options: Photos, SMSs and Contacts. The Server connection for SMS 
 * and contacts is not implemented.s
 * @author caranthir
 *
 */
public class MainActivity extends Activity {


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
	
	
	public void getPhotos(View view){
		Intent intent1 = new Intent(getApplicationContext(), PhotosActivity.class);
		startActivity(intent1);		
	}



	/**
	 * copied from http://stackoverflow.com/questions/7204035/how-to-access-sms-and-contacts-data
	 * @param view
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 */

	public void getSMS(View view) throws ParserConfigurationException, TransformerException{
		Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
		Cursor cursor1 = getContentResolver().query(mSmsinboxQueryUri,
				new String[] { "_id", "thread_id", "address", "person", "date",
				"body", "type" }, null, null, null);
		createXML(cursor1,"sms");

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
		createXML(cursor,"contacts");



	}    

	private void createXML(Cursor cursor, String type) throws ParserConfigurationException, TransformerException{

		DocumentBuilderFactory documentBuilderFactory;
		DocumentBuilder documentBuilder;
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element root;
		if(type.equals("sms")){
			root = document.createElement("SMSs");
		}else{
			root = document.createElement("contacts");
		}
		document.appendChild(root);

		//System.out.println(players.size() + " " +p.getGender() );

		Element contact;
		String[] columns = new String[] { "address", "person", "date", "body","type" ,"_id"};

		if(type.equals("sms")){
			if (cursor.getCount() > 0) {
				//	String count = Integer.toString(cursor.getCount());
				while (cursor.moveToNext()){
					//	String id = cursor.getString(cursor.getColumnIndex(columns[5]));
					contact = document.createElement("sms");
					String address = cursor.getString(cursor.getColumnIndex(columns[0]));
					if(address !=null){
						contact.setAttribute(columns[0], address);
					}
					String name = cursor.getString(cursor.getColumnIndex(columns[1]));
					if(name !=null){
						contact.setAttribute(columns[1], name);
					}
					String date = cursor.getString(cursor.getColumnIndex(columns[2]));
					if(date !=null){
						contact.setAttribute(columns[2], date);
					}
					String msg = cursor.getString(cursor.getColumnIndex(columns[3]));
					if(msg !=null){
						contact.setAttribute(columns[3], msg);
					}
					String type1 = cursor.getString(cursor.getColumnIndex(columns[4]));
					if(type1 !=null){
						contact.setAttribute(columns[4], type1);
					}
					root.appendChild(contact);
				}
			}
		}else {
			cursor.moveToNext();

			int index1= cursor.getColumnIndex(Data.RAW_CONTACT_ID);
			String rawId = cursor.getString(index1);

			contact = document.createElement("contact");

			int indexVar= cursor.getColumnIndex(Data.DATA1);
			String var = cursor.getString(indexVar);

			int indexType= cursor.getColumnIndex(Data.MIMETYPE);
			String typeContact = cursor.getString(indexType);

			if(typeContact !=null && var !=null){
				contact.setAttribute(typeContact.replace("/", ""), var);
			}

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
				typeContact = cursor.getString(indexType);
				System.out.println("var " + var+ "type "+typeContact);
				if(typeContact !=null && var !=null){
					contact.setAttribute(typeContact.replace("/", ""), var);
				}

			}
			root.appendChild(contact);

		}


		TransformerFactory tFactory =TransformerFactory.newInstance();
		Transformer transformer =tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(document);
		File file;
		if(type.equals("sms")){
			file = new File(getFilesDir(), "sms.xml");
		}else{
			file = new File(getFilesDir(), "contacts.xml");
		}
		StreamResult result =new StreamResult(file);
		transformer.transform(source, result);
		System.out.println(file.toString());


		BufferedReader br = null;

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



	}
}
