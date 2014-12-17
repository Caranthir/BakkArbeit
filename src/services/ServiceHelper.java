package services;
//TODO PENDINGREQUESTLERIN KEY ILE OBJECTLERINI DEGISTIR SONRA PHOTOSACTIVITY YI BBITIR
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
/*
 * This class is partly copied from https://github.com/aug-mn/restful-android/blob/1237c3235b26c9e9bf1a93b8cc6cec5ef16675b4/src/mn/aug/restfulandroid/service/TwitterServiceHelper.java
 */
public class ServiceHelper {

	private Context context;
	private static ServiceHelper serviceHelper;

	public static final String REQUEST_ID = "REQUEST_ID";


	public static String ACTION_REQUEST_RESULT = "REQUEST_RESULT";
	public static String ACTION_PHOTO_RESULT = "REQUESTP_RESULT";
	public static String ACTION_DATAS_RESULT = "REQUESTD_RESULT";
	public static String ACTION_DELETE_RESULT = "REQUESTDE_RESULT";


	public static String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";
	public static String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
	//In photo Uploads the File Path of the File is used as the KEY
	private HashMap<Long,String> pendingPhotoRequests = new HashMap<Long ,String>();
	private HashMap<Long,String> pendingDeletePhotoRequests = new HashMap<Long ,String>();
	private HashMap<Long,String> pendingOtherRequests = new HashMap<Long	,String>();
	//private ArrayList<Integer> pendingPhotoRequests = new ArrayList<Integer>();
	
	public static ServiceHelper getInstance(Context context) {
		if (serviceHelper == null)
			serviceHelper = new ServiceHelper(context);
		// to update the context each time a service helper it's called
		serviceHelper.setContext(context);
		return serviceHelper;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ServiceHelper(Context context) {
		this.context = context;
	}

	public long startService(String action) {
		return instantiate(action, null);
	}

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}

	public long instantiate(String action, Bundle extras) {
		if (isInternetAvailable(context)) {
			Intent intent = null;
			if (action.equals(LoadService.REGISTER)) {

				if(pendingOtherRequests.containsValue(LoadService.REGISTER)){
					return (Long) getKeyFromValue(pendingOtherRequests, LoadService.REGISTER);
				}
				long reqId = generateID();
				pendingOtherRequests.put(reqId, LoadService.REGISTER);
				ResultReceiver serviceCallback = new ResultReceiver(null){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						handleRegisterResponse(resultCode, resultData);
					}
				};

				Log.i("ServiceHelper","SH Register");
				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				intent.putExtra(LoadService.SERVICE_CALLBACK, serviceCallback);
				intent.putExtra(REQUEST_ID, reqId);
				ComponentName bla =context.startService(intent);
				Log.i("ServiceHelper",bla.toString());
				return reqId;

			} else if (action.equals(LoadService.LOGIN)){
				if(pendingOtherRequests.containsValue(LoadService.LOGIN)){
					return (Long) getKeyFromValue(pendingOtherRequests, LoadService.LOGIN);
				}

				long reqId = generateID();
				pendingOtherRequests.put(reqId, LoadService.LOGIN);
				ResultReceiver serviceCallback = new ResultReceiver(null){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						handleLoginResponse(resultCode, resultData);
					}

				};

				Log.i("ServiceHelper","SH Login");
				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				intent.putExtra(LoadService.SERVICE_CALLBACK, serviceCallback);
				intent.putExtra(REQUEST_ID, reqId);
				ComponentName bla =context.startService(intent);
				Log.i("ServiceHelper",bla.toString());
				return reqId;

			}
			else if (action.equals(LoadService.UPLOAD_PHOTO)){
				if(pendingPhotoRequests.containsValue(extras.get("path"))){
					return (Long) getKeyFromValue(pendingPhotoRequests, extras.get("path"));
				}

				long reqId = generateID();
				pendingPhotoRequests.put( reqId, (String) extras.get("path"));
				ResultReceiver serviceCallback = new ResultReceiver(null){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						handlePhotoResponse(resultCode, resultData);
					}
				};

				Log.i("ServiceHelper","SH Photo Upload");
				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				intent.putExtra(LoadService.SERVICE_CALLBACK, serviceCallback);
				intent.putExtra(REQUEST_ID, reqId);
				ComponentName bla =context.startService(intent);
				Log.i("ServiceHelper",bla.toString());
				return reqId;

			}else if (action.equals(LoadService.DOWNLOAD_DATAS)){
				if(pendingOtherRequests.containsValue(LoadService.DOWNLOAD_DATAS)){
					return (Long) getKeyFromValue(pendingOtherRequests, LoadService.DOWNLOAD_DATAS);
				}

				long reqId = generateID();
				pendingOtherRequests.put( reqId, LoadService.DOWNLOAD_DATAS);
				ResultReceiver serviceCallback = new ResultReceiver(null){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						handleDatasResponse(resultCode, resultData);
					}
				};

				Log.i("ServiceHelper","SH Datas Download");
				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				intent.putExtra(LoadService.SERVICE_CALLBACK, serviceCallback);
				intent.putExtra(REQUEST_ID, reqId);
				ComponentName bla =context.startService(intent);
				Log.i("ServiceHelper",bla.toString());
				return reqId;

			}else if (action.equals(LoadService.DELETE_PHOTO)){
				if(pendingDeletePhotoRequests.containsValue(extras.get("path"))){
					return (Long) getKeyFromValue(pendingDeletePhotoRequests, extras.get("path"));
				}

				long reqId = generateID();
				pendingDeletePhotoRequests.put( reqId , (String) extras.get("path"));
				ResultReceiver serviceCallback = new ResultReceiver(null){

					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						handleDeleteResponse(resultCode, resultData);
					}
				};

				Log.i("ServiceHelper","SH Datas Download");
				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				intent.putExtra(LoadService.SERVICE_CALLBACK, serviceCallback);
				intent.putExtra(REQUEST_ID, reqId);
				ComponentName bla =context.startService(intent);
				Log.i("ServiceHelper",bla.toString());
				return reqId;

			}
		}
		return 0; 
	}


	private long generateID() {
		long requestId = Math.abs(UUID.randomUUID().getLeastSignificantBits());
		while(pendingOtherRequests.containsKey(requestId) || pendingPhotoRequests.containsKey(requestId) || pendingDeletePhotoRequests.containsKey(requestId)){
			 requestId = UUID.randomUUID().getLeastSignificantBits();
		}
		return requestId;
	}
	
	

	public HashMap<Long, String> getPendingPhotoRequests() {
		return pendingPhotoRequests;
	}

	public HashMap<Long, String> getPendingDeletePhotoRequests() {
		return pendingDeletePhotoRequests;
	}

	public HashMap<Long, String> getPendingOtherRequests() {
		return pendingOtherRequests;
	}

	public boolean isRequestPending(long requestId){
		return (this.pendingOtherRequests.containsKey(requestId)|| this.pendingDeletePhotoRequests.containsKey(requestId)|| this.pendingPhotoRequests.containsKey(requestId) );
	}
	
	private void handleDatasResponse(int resultCode, Bundle resultData){
		Intent origIntent = (Intent)resultData.getParcelable(LoadService.ORIGINAL_INTENT_EXTRA);
		Log.i("ServiceHelper -datas", "ResultReceiver active ");

		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

			pendingOtherRequests.remove(requestId);
			Log.i("ServiceHelper-datas", "ResultReceiver active 2");

			Intent resultBroadcast = new Intent(ACTION_DATAS_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

			context.sendBroadcast(resultBroadcast);
		}
	}
	
	private void handlePhotoResponse(int resultCode, Bundle resultData){
		Intent origIntent = (Intent)resultData.getParcelable(LoadService.ORIGINAL_INTENT_EXTRA);
		String path = (String) origIntent.getExtras().get("path");
		Log.i("ServiceHelper", "ResultReceiver active ");

		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);
			
			pendingPhotoRequests.remove(requestId);
			Log.i("ServiceHelper", "ResultReceiver active 2" + " "+ requestId);

			Intent resultBroadcast = new Intent(ACTION_PHOTO_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
			resultBroadcast.putExtra("path", path);

			context.sendBroadcast(resultBroadcast);
		}
	}

	private void handleRegisterResponse(int resultCode, Bundle resultData){

		Intent origIntent = (Intent)resultData.getParcelable(LoadService.ORIGINAL_INTENT_EXTRA);
		Log.i("ServiceHelper", "ResultReceiver active ");


		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

			pendingOtherRequests.remove(requestId);
			Log.i("ServiceHelper", "ResultReceiver active 2");

			Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);

			context.sendBroadcast(resultBroadcast);
		}
	}

	private void handleLoginResponse(int resultCode, Bundle resultData){

		Intent origIntent = (Intent)resultData.getParcelable(LoadService.ORIGINAL_INTENT_EXTRA);
		Log.i("ServiceHelper", "ResultReceiver active ");


		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

			pendingOtherRequests.remove(requestId);
			Log.i("ServiceHelper", "ResultReceiver active 2");

			Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
			resultBroadcast.putExtra(AccountManager.KEY_ACCOUNT_NAME, origIntent.getExtras().getString("nickname"));
			resultBroadcast.putExtra(AccountManager.KEY_PASSWORD, origIntent.getExtras().getString("password"));
			context.sendBroadcast(resultBroadcast);

		}
	}
	private void handleDeleteResponse(int resultCode, Bundle resultData){
		Intent origIntent = (Intent)resultData.getParcelable(LoadService.ORIGINAL_INTENT_EXTRA);
		Log.i("ServiceHelper", "ResultReceiver active ");


		if(origIntent != null){
			long requestId = origIntent.getLongExtra(REQUEST_ID, 0);

			pendingDeletePhotoRequests.remove(requestId);
			Log.i("ServiceHelper", "ResultReceiver active 2");

			Intent resultBroadcast = new Intent(ACTION_DELETE_RESULT);
			resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
			resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
			context.sendBroadcast(resultBroadcast);
		}
	}
	
//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	  public static Object getKeyFromValue(Map hm, Object value) {
	    for (Object o : hm.keySet()) {
	      if (hm.get(o).equals(value)) {
	        return o;
	      }
	    }
	    return null;
	  }
}
