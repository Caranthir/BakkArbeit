package activities;

import services.LoadService;
import services.ServiceHelper;
import ws13.bakkarbeit.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
/*
 * partly copied from https://github.com/Udinic/AccountAuthenticator/blob/606f3b15f104405cb9445d4c05d051e4d5614117/ExampleApp/src/com/udinic/accounts_example/Main1.java
 */
public class LoginRegisterActivity extends Activity {
	private AccountManager mAccountManager;
	private long logId;
	private UserLoginTask mAuthTask = null;
	private BroadcastReceiver requestReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mAccountManager = AccountManager.get(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_register);
	}

	@Override
	protected void onResume() {
		mAccountManager = AccountManager.get(this);
		super.onResume();
		setContentView(R.layout.activity_login_register);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_register, menu);
		return true;
	}
	
	public void register(View view){
		Intent intent1 = new Intent(getApplicationContext(), RegisterActivity.class);
		startActivity(intent1);
	}


	public void login(View view){
		final Account availableAccounts[] = mAccountManager.getAccountsByType(AuthenticatorActivity.ACCOUNT_TYPE);


		IntentFilter filter = new IntentFilter(ServiceHelper.ACTION_REQUEST_RESULT);
		requestReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				long resultRequestId = intent
						.getLongExtra(ServiceHelper.EXTRA_REQUEST_ID, 0);
				Log.i("LoginRegisterActivity", "Active 1 ");

				if (resultRequestId == logId) {

					int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);

					Log.i("LoginRegisterActivity", "Result code = " + resultCode);

					if (resultCode == 1) {
						showToast("Successfully Logged in");
						Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(intent1);
					} else {
						showToast("Unsuccessful");
					}

				} else {
					Log.d("AuthenticatorActivity", "Result is NOT for our request ID");
				}

			}
		};

		this.registerReceiver(requestReceiver, filter);


		if (availableAccounts.length == 0) {
			Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
			final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(AuthenticatorActivity.ACCOUNT_TYPE, "0", null, null, this, new AccountManagerCallback<Bundle>() {
				@Override
				public void run(AccountManagerFuture<Bundle> future) {
					try {
						//Bundle bnd = future.getResult();
						Log.i("LoginRegisterActivity", "AddNewAccount Bundle is ");
						Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(intent1);
					} catch (Exception e) {
						e.printStackTrace();
						Log.i("LoginRegisterActivity", "AddNewAccount FAIL ");
					}
				}
			}, null);

		}else if (availableAccounts.length == 1) {
			Log.d("LoginRegisterActivity", "1 Account found and trying to log in");
			Account account = availableAccounts[0];
			mAuthTask = new UserLoginTask();
			Log.i("LoginRegister", account.name +" "+ mAccountManager.getPassword(account));
			mAuthTask.execute(account.name, mAccountManager.getPassword(account));
		}
	}


	private void showToast(String message) {
		if (!isFinishing()) {
			Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}


	@Override
	protected void onPause() {
		super.onPause();

		// Unregister for broadcast
		if (requestReceiver != null) {
			try {
				this.unregisterReceiver(requestReceiver);
			} catch (IllegalArgumentException e) {
				Log.e("LoginRegisterActivity", e.getLocalizedMessage());
			}
		}
		
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {

			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("password", params[1]);
			extras.putString("nickname", params[0]);

			logId = sh.instantiate(LoadService.LOGIN, extras);

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
