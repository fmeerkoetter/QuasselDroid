package com.iskrembilen.quasseldroid.gui;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.service.CoreConnService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;

public class SplashActivity extends Activity {
	// Set the display time, in milliseconds (or extract it out as a configurable parameter)
	private final int SPLASH_DISPLAY_LENGTH = 1000;
	private final String TAG = SplashActivity.class.getSimpleName();
	private boolean canBeFinished;
	private Class<?> activityToStart;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		canBeFinished = false;
	}

	@Override
	protected void onStart() {

		super.onResume();
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				if(canBeFinished) {
					startActivity(activityToStart);
				} else {
					canBeFinished = true;
				}
			}
		}, SPLASH_DISPLAY_LENGTH);
		doBindService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		doUnbindService();
	}

	private void startActivity(Class<?> activity) {
		if(canBeFinished) {
			Intent mainIntent = new Intent(SplashActivity.this, activityToStart);
			SplashActivity.this.startActivity(mainIntent);
			finish();
		} else {
			activityToStart = activity;
		}
	}

	private CoreConnService boundConnService = null;
	private boolean isBound = false;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			boundConnService = ((CoreConnService.LocalBinder)service).getService();
			if(boundConnService.isConnected()) {
				startActivity(BufferActivity.class);
			} else {
				startActivity(LoginActivity.class);
			}
			canBeFinished = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			boundConnService = null;
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(SplashActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i(TAG, "Binding Service");
	}

	void doUnbindService() {
		if (isBound) {
			unbindService(mConnection);
			isBound = false;
		}
	}
}
