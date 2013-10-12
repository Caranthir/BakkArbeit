package contentprovider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceHelper {

	private Context context;
	private static ServiceHelper serviceHelper;

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

	public boolean startService(String action) {
		return startService(action, null);
	}

	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}

	public boolean startService(String action, Bundle extras) {
		if (isInternetAvailable(context)) {
			Intent intent = null;
			if (action.equals("register")) {
				System.out.println("servicehelper");

				intent = new Intent(context, LoadService.class);
				intent.putExtra("action", action);
				if(extras!=null){
					intent.putExtras(extras);
				}
				ComponentName bla =context.startService(intent);
				System.out.println(bla.toString());
				return true;
			} /*else {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.toast_message, null);

			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(R.string.no_internet_available);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();

			toast.show();
			return false;
		}*/
		}
		return false;
	}

}
