package activities;

import services.LoadService;
import services.Processor;
import services.ServiceHelper;
import ws13.bakkarbeit.R;
import contentprovider.BContentProvider;
import contentprovider.DatabaseHelper;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 * This class is partly copied from https://github.com/Udinic/AccountAuthenticator/blob/master/src/com/udinic/accounts_authenticator_example/authentication/AuthenticatorActivity.java
 */
public class AuthenticatorActivity extends Activity {

	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
	public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
	public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

	public final static String ACCOUNT_TYPE= "bakkArbeit.ws13.bakkarbeit.AccountAuthenticator";

	public final static String PARAM_USER_PASS = "USER_PASS";



	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mNickname;
	private String mPassword;

	private BroadcastReceiver requestReceiver;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;

	private long logId =-1;

	private AccountManager mAccountManager;


	/** checks if there is already a request sent. if not registers the broadcast.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mAccountManager = AccountManager.get(getBaseContext());		
		// Set up the login form.

		IntentFilter filter = new IntentFilter(ServiceHelper.ACTION_REQUEST_RESULT);
		requestReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				long resultRequestId = intent
						.getLongExtra(ServiceHelper.EXTRA_REQUEST_ID, 0);

				if (resultRequestId == logId) {

					int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);

					Log.i("AuthenciatorActivity", "Result code = " + resultCode);

					String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					String accountPassword = intent.getStringExtra(AccountManager.KEY_PASSWORD);
					final Account account = new Account(accountName, ACCOUNT_TYPE);

					if (resultCode == 1) {
						showToast("Successfully Logged in");
						Log.i("AuthenticatorActivity Credentials", account.name+ " "+ accountPassword+ " " + mPassword);
						mAccountManager.addAccountExplicitly(account, accountPassword, null);
						Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(intent1);

					} else {
						showToast("Unsuccessful");
						finish();
					}

				} else {
					Log.d("AuthenticatorActivity", "Result is NOT for our request ID");
				}

			}
		};
		
		Log.i("AuthenticatorActivity", "logId " + logId);
		if(logId>0){
			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			if(sh.isRequestPending(logId)){
				Log.i("AuthenticatorActivity", "Existing Request, Registering Receiver");
				this.registerReceiver(requestReceiver, filter);
			}
			else{
				Log.i("AuthenticatorActivity", "Done Request, Getting Results");
				ContentResolver cr=getContentResolver();
				String[] projection = { DatabaseHelper.LOGIN_STATUS_COL};
				String[] mSelectionArgs = { mNickname } ;
				Cursor cursor = cr.query(BContentProvider.LOGIN_URI, projection, DatabaseHelper.LOGIN_TABLE + "= ?" , mSelectionArgs, null);		
				if(cursor== null){
					Log.e("AuthenticatorActivity", "Cursor is null");
				}else if(cursor.getCount()<1){
					Log.e("AuthenticatorActivity", "Cursor is empty");
				}else {
					cursor.moveToFirst();
					String status= cursor.getString(0);
					if(status.equals(Processor.STATUS_SUCCESS)){
						final Account account = new Account(mNickname, ACCOUNT_TYPE);
						showToast("Successfully Logged in");
						Log.i("AuthenticatorActivity Credentials", mNickname+ " "+ mPassword+ " " + mPassword);
						mAccountManager.addAccountExplicitly(account, mPassword, null);


					} else {
						showToast("Unsuccessful");
						finish();
					}
				}
			}
		}
		else{
			this.registerReceiver(requestReceiver, filter);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_authenticator);

		mEmailView = (EditText) findViewById(R.id.nickname);


		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
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



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//getMenuInflater().inflate(R.menu.authenticator, menu);
		return true;
	}

	/**
	 * Attempts to sign in the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mNickname = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			//mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			//showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {

			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("password", mPassword);
			extras.putString("nickname", mNickname);

			logId = sh.instantiate(LoadService.LOGIN, extras);

			return true;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
