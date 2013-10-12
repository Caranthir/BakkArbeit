package ws13.bakkarbeit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * partly copied from https://github.com/necronet/Eli-G/blob/34061d7238d2d8c6f2317edbf9f530ce1bfed14c/src/net/clov3r/elig/http/HttpCaller.java
 */
public class HTTPClient {

	public JSONObject register(String url,List<NameValuePair> params) {
		System.out.println("httpclient");
		JSONObject json = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

	  /*  BasicNameValuePair[] params = {
	            new BasicNameValuePair("nickname", "single"),
	            new BasicNameValuePair("password", "journeyPlannerCommand"),
	            new BasicNameValuePair("email", "on")};*/

		try {
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpClient.execute(httpPost);

			//Log.d(LOG, response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					total.append(line);
				}
				json = new JSONObject(total.toString());
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}

	/*public String requestHtml(String url, List<NameValuePair> params) {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		try {
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			UrlEncodedFormEntity query = new UrlEncodedFormEntity(params);
			httpPost.setEntity(query);

			HttpResponse response = httpClient.execute(httpPost);


			HttpEntity entity = response.getEntity();

			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					total.append(line);
				}
				Log.d(LOG, total.toString());
				return total.toString();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/


