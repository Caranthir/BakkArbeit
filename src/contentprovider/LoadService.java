package contentprovider;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import ws13.bakkarbeit.HTTPClient;

import android.app.IntentService;
import android.content.Intent;

public class LoadService extends IntentService {

	public LoadService() {
		super("configuration");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("service");


		//String url = getString(R.string.cseserver)+ searchUrl;
		HTTPClient caller = new HTTPClient();
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		/*BasicNameValuePair[] params = {
	            new BasicNameValuePair("nickname", "single"),
	            new BasicNameValuePair("password", "journeyPlannerCommand"),
	            new BasicNameValuePair("email", "on")};*/
		params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
		params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));
		params.add(new BasicNameValuePair("email", intent.getExtras().getString("email")));
		
		JSONObject object = caller.register("http://10.0.2.2:8080/BakkArbeit.Server2/rest/person",params);
		
		/*Document doc=Jsoup.parse(object);
		Elements datas = doc.select("b");*/
		
		String action = intent.getExtras().getString("action");
		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction(action);
		
		/*if(datas.size()>0){
			new JrvProcessor(this).parse(object);
			broadCastIntent.putExtra("success", true);
			broadCastIntent.putExtra("id",intent.getExtras().getString("id") );
		}*/
		this.sendBroadcast(broadCastIntent);

	}
}
