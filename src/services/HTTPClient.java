package services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

/*
 * partly copied from https://github.com/necronet/Eli-G/blob/34061d7238d2d8c6f2317edbf9f530ce1bfed14c/src/net/clov3r/elig/http/HttpCaller.java
 */
public class HTTPClient {

	/**The server works with a self-signed TLS-Certificate for test purposes. To accept a self-signed  TLS
	 * certificate, we need to create an HttpClient Object with a modified SSL Context
	 * @return CloseableHttpClient that accepts self-signed SSL certificates
	 */
	public CloseableHttpClient getHttpClient(){
		SSLContextBuilder builder = new SSLContextBuilder();
		CloseableHttpClient httpClient = null;
		try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(),SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpClient = HttpClients.custom().setSSLSocketFactory(
					sslsf).build();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return null;
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
			return null;
		} catch (KeyManagementException e) {
			e.printStackTrace();
			return null;
		}
		return httpClient;

	}

	/**Registers a user on the server
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @return JSONObject of the Person entity from the server. If failed returns null
	 */
	public JSONObject register(String url,List<NameValuePair> params) {
		JSONObject json = null;
		CloseableHttpClient httpClient = getHttpClient();
		HttpPut httpPost = new HttpPut(url);

		try {
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpClient.execute(httpPost);
			Log.i("HTTPCLient", response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				Log.i("HTTPCLient", "Response is not null");
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					Log.i("HTTPClient register", line);
					total.append(line);
				}
				json = new JSONObject(total.toString());
			}
			httpClient.close();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return json;
	}
	
	// http://stackoverflow.com/questions/19517538/ignoring-ssl-certificate-in-apache-httpclient-4-3
	/**Logs in a user on the server
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @return JSONObject of the Person entity from the server. If failed returns null
	 */
	public JSONObject login(String url,List<NameValuePair> params) {
		CloseableHttpClient httpClient = getHttpClient();

		JSONObject json = null;
		HttpGet httpGet = new HttpGet(url);


		try {
			Log.i("HTTPClient Credentials", params.get(0).getValue() + " "+ params.get(1).getValue());
			String encodedUsernamdAndPass = Base64.encodeToString((params.get(0).getValue() + ":"+ params.get(1).getValue()).getBytes(), Base64.NO_WRAP);
			Log.i("HTTPClient Credentials", encodedUsernamdAndPass);
			httpGet.setHeader("Authorization",encodedUsernamdAndPass );
			Log.i("HTTPClient Credentials", httpGet.getFirstHeader("Authorization").getValue());

			HttpResponse response = httpClient.execute(httpGet);

			Log.i("HTTPCLient", response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				Log.i("HTTPCLient", "Response is not null");
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					Log.i("HTTPClient login", line);
					total.append(line);
				}
				json = new JSONObject(total.toString());
			}
			httpClient.close();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return json;
	}


	/**Uploads a photo to the server
	 * partly copied from http://prativas.wordpress.com/category/android/uploading-image-from-android-app-to-server-programmatically/
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @param photoPath Path of the Image to be uploaded in the Android File System
	 * @return a String that tells us, if the process was successful
	 */
	public String uploadPhoto(String url,List<NameValuePair> params,String photoPath ) {

		CloseableHttpClient httpClient = getHttpClient();
		HttpPut httpPut = new HttpPut(url);

		try {
			//httpPost.setHeader("Content-Type", "multipart/form-data");
			String encodedUsernamdAndPass = Base64.encodeToString((params.get(0).getValue() + ":"+ params.get(1).getValue()).getBytes(), Base64.NO_WRAP);
			Log.i("HTTPClient Credentials", encodedUsernamdAndPass);
			httpPut.setHeader("Authorization",encodedUsernamdAndPass );
			// creating a file body consisting of the file that we want to
			// send to the server
			Log.i("HTTPClient upload", "active");
			FileBody fileBody = new FileBody(new File(photoPath));

			MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
			multiPartEntityBuilder.addPart("file", fileBody);
			httpPut.setEntity(multiPartEntityBuilder.build());

			// Execute POST request to the given URL
			HttpResponse httpResponse = null;
			Log.i("HTTPClient upload", "active 2");
			httpResponse = httpClient.execute(httpPut);
			Log.i("HTTPClient upload", "active 3");
			// receive response as inputStream
			InputStream inputStream = httpResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				Log.i("HTTPClient login", line);
				total.append(line);
			}
			Log.i("HTTPClient upload", "active 4");			
			httpClient.close();

			return total.toString();

		} catch (Exception e) {
			return null;
		}
	}

	/**Downloads Datas from the Server. Datas represent all the files and datas, the user has uploaded to the server
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @return JSONObject that represent the files, that the user has uploaded to the server
	 */
	
	public JSONObject downloadDatas(String url,List<NameValuePair> params) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpGet httpGet = new HttpGet(url);

		try {
			Log.i("HTTPClient Credentials", params.get(0).getValue() + " "+ params.get(1).getValue());String encodedUsernamdAndPass = Base64.encodeToString((params.get(0).getValue() + ":"+ params.get(1).getValue()).getBytes(), Base64.NO_WRAP);
			Log.i("HTTPClient Credentials", encodedUsernamdAndPass);
			httpGet.setHeader("Authorization",encodedUsernamdAndPass );
			HttpResponse response = httpClient.execute(httpGet);

			Log.i("HTTPCLient", response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				Log.i("HTTPCLient", "Response is not null");
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					Log.i("HTTPClient login", line);
					total.append(line);
				}
				if(!total.toString().equals("null")){
					JSONObject json1 = new JSONObject(total.toString());
					httpClient.close();
					return json1;
				}
			}
			httpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	/**downloads a photo from the server
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @return boolean true if successful, false if not
	 */
	

	public boolean downloadPhoto(String url,List<NameValuePair> params,String nameOfFile) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			String encodedUsernamdAndPass = Base64.encodeToString((params.get(0).getValue() + ":"+ params.get(1).getValue()).getBytes(), Base64.NO_WRAP);
			Log.i("HTTPClient Credentials Download", encodedUsernamdAndPass);
			httpGet.setHeader("Authorization",encodedUsernamdAndPass );
			HttpResponse response = httpClient.execute(httpGet);

			Log.i("HTTPCLient", response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				BufferedInputStream bis = new BufferedInputStream(entity.getContent());
				//String filePath = "/DCIM/" ;
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/bakk/"+ nameOfFile);
				if(!file.exists()){
					Log.i("HTTPCLient", "File created 1");
					file.getParentFile().mkdirs();
				}else if( !file.isDirectory() && file.canWrite() ){
					Log.i("HTTPCLient", "File created 2");
					file.delete();
					file.getParentFile().mkdirs();
				}
				else{
					Log.i("HTTPCLient", "not accessible");
				}

				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				int inByte;
				while((inByte = bis.read()) != -1) bos.write(inByte);
				bis.close();
				bos.close();
			}
			httpClient.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	

	/**deletes a photo from the User's Account
	 * @param url Server url to connect
	 * @param params Parameters like nickname and password
	 * @return String "OK" if successful, false if not
	 */
	public String deletePhoto(String url, List<NameValuePair> params) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpDelete httpDelete = new HttpDelete(url);

		try {
			String encodedUsernamdAndPass = Base64.encodeToString((params.get(0).getValue() + ":"+ params.get(1).getValue()).getBytes(), Base64.NO_WRAP);
			Log.i("HTTPClient Credentials", encodedUsernamdAndPass);
			httpDelete.setHeader("Authorization",encodedUsernamdAndPass );
			HttpResponse response = httpClient.execute(httpDelete);

			Log.i("HTTPCLient", response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			httpClient.close();

			if (entity != null) {
				return "OK";
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}


