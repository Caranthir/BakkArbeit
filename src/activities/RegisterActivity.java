package activities;

import services.LoadService;
import services.ServiceHelper;
import ws13.bakkarbeit.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.AsyncTask;
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
 * Activity which displays a screen to the user, offering registration.
 * this class is partly copied from https://github.com/aug-mn/restful-android/blob/1237c3235b26c9e9bf1a93b8cc6cec5ef16675b4/src/mn/aug/restfulandroid/activity/TimelineActivity.java
 */
public class RegisterActivity extends Activity {
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mNickname;

	long regId;

	private BroadcastReceiver requestReceiver;

	// UI references.
	private EditText mEmailView;
	private EditText mNicknameView;
	private EditText mPasswordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mNicknameView = (EditText) findViewById(R.id.nickname);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptRegister();
					return true;
				}
				return false;
			}
		});

		IntentFilter filter = new IntentFilter(ServiceHelper.ACTION_REQUEST_RESULT);
		requestReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				long resultRequestId = intent
						.getLongExtra(ServiceHelper.EXTRA_REQUEST_ID, 0);
				Log.i("RegisterActivity", "Active 1 ");
				
				if (resultRequestId == regId) {
					int resultCode = intent.getIntExtra(ServiceHelper.EXTRA_RESULT_CODE, 0);

					Log.i("RegisterActivity", "Result code = " + resultCode);

					if (resultCode == 1) {
						showToast("Successfully Registered");
						mAuthTask = null;
						finish();
					} else {
						mAuthTask = null;
						showToast("Unsuccessfull");
					}
					
				} 
			}
		};

		this.registerReceiver(requestReceiver, filter);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptRegister() {
		if(!ServiceHelper.isInternetAvailable(getBaseContext())){
			showToast("No Internet Connection Available");
			return;
		}
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mNickname = mNicknameView.getText().toString();


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

		if (TextUtils.isEmpty(mNickname)) {
			mNicknameView.setError(getString(R.string.error_field_required));
			focusView = mNicknameView;
			cancel = true;
		}


		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
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
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			ServiceHelper sh = ServiceHelper.getInstance(getApplicationContext());
			Bundle extras = new Bundle();
			extras.putString("nickname", mNickname);
			extras.putString("password", mPassword);
			extras.putString("email", mEmail);

			regId = sh.instantiate(LoadService.REGISTER, extras);

			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}
