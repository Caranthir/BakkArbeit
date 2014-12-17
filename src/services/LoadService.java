package services;

import java.util.ArrayList;

/* partly copied from https://github.com/aug-mn/restful-android/blob/1237c3235b26c9e9bf1a93b8cc6cec5ef16675b4/src/mn/aug/restfulandroid/service/TwitterService.java?source=cc
 * 
 */
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

public class LoadService extends IntentService {

	private ResultReceiver mCallback;

	private Intent mOriginalRequestIntent;

	public static final String SERVICE_CALLBACK = "SERVICE_CALLBACK";

	public static final String REGISTER = "REGISTER";

	public static final String LOGIN = "LOGIN";
	
	public static final String UPLOAD_PHOTO= "SENDPHOTO";
	
	public static final String DOWNLOAD_DATAS= "DDATAS";
	
	public static final String DELETE_PHOTO= "DPHOTO";
	
	public static final String ORIGINAL_INTENT_EXTRA = "ORIGINAL_INTENT_EXTRA";

	public LoadService() {
		super("configuration");
	}
	
    /* (non-Javadoc)
     * @see android.app.IntentService#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {

        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		String action = intent.getExtras().getString("action");
		if(action.equals(REGISTER)){

			mOriginalRequestIntent = intent;

			// Get request data from Intent
			mCallback = intent.getParcelableExtra(LoadService.SERVICE_CALLBACK);

			//String url = getString(R.string.cseserver)+ searchUrl;

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
			params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));
			params.add(new BasicNameValuePair("email", intent.getExtras().getString("email")));

			Processor p= new Processor(getApplicationContext());
			p.register(params, makeProcessorCallback());

			stopSelf();
		}else if(action.equals(LOGIN)){

			mOriginalRequestIntent = intent;

			// Get request data from Intent
			mCallback = intent.getParcelableExtra(LoadService.SERVICE_CALLBACK);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
			params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));

			Processor p= new Processor(getApplicationContext());
			p.login(params, makeProcessorCallback());
			stopSelf();
		}else if(action.equals(UPLOAD_PHOTO)){
			mOriginalRequestIntent = intent;

			// Get request data from Intent
			mCallback = intent.getParcelableExtra(LoadService.SERVICE_CALLBACK);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
			params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));
			String path = intent.getExtras().getString("path");
			Processor p= new Processor(getApplicationContext());
			p.uploadPhoto(params,path,  makeProcessorCallback(),intent.getLongExtra(ServiceHelper.REQUEST_ID, -1));
			stopSelf();
		}else if(action.equals(DOWNLOAD_DATAS)){
			mOriginalRequestIntent = intent;

			// Get request data from Intent
			mCallback = intent.getParcelableExtra(LoadService.SERVICE_CALLBACK);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
			params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));
			String path = intent.getExtras().getString("path");
			Processor p= new Processor(getApplicationContext());
			p.getDatas(params,path,  makeProcessorCallback());
			stopSelf();
		}else if(action.equals(DELETE_PHOTO)){
			mOriginalRequestIntent = intent;

			// Get request data from Intent
			mCallback = intent.getParcelableExtra(LoadService.SERVICE_CALLBACK);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nickname", intent.getExtras().getString("nickname")));
			params.add(new BasicNameValuePair("password", intent.getExtras().getString("password")));
			String path = intent.getExtras().getString("path");
			Processor p= new Processor(getApplicationContext());
			p.deletePhoto(params,path,  makeProcessorCallback());
			stopSelf();
		}
	}

	/**Creates a Callback Method, that is called by the Processor to send the result of the REST-Method
	 * to the Service Helper
	 * @return a Callback Class with a send method
	 */
	private Callback makeProcessorCallback() {
		Callback callback = new Callback() {

			/* (non-Javadoc)
			 * @see contentprovider.Callback#send(int)
			 */
			@Override
			public void send(int resultCode) {
				if (mCallback != null) {
					Log.i("Callback", "Result code = " + resultCode);
					mCallback.send(resultCode, getOriginalIntentBundle());
				}
			}
		};
		return callback;
	}


	/**Returns the original Intent, that was given by the ServiceHelper
	 * @return the original Intent
	 */
	protected Bundle getOriginalIntentBundle() {
		Bundle originalRequest = new Bundle();
		originalRequest.putParcelable(ORIGINAL_INTENT_EXTRA, mOriginalRequestIntent);
		return originalRequest;
	}
}
