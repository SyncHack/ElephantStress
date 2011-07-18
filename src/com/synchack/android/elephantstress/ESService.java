/*
 * Copyright (C) 2011 Makoto NARA (Mc.N)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.synchack.android.elephantstress;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ESService extends Service {

	private final IESService.Stub mIESServiceBinder = new IESService.Stub() {

		@Override
		public void start() throws RemoteException {
			startStress();
		}

		@Override
		public void stop() throws RemoteException {
			stopStress();
		}

		@Override
		public ESStatus getESStatus() throws RemoteException {
			return makeESStatus(false);
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		return mIESServiceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false; // noneed rebind()
	}

	private Handler mHandler;
	private static final int LOOP_TIME = 1000; // 1sec
	private static final int MSG_START_TIMER = 1;
	private static final int MSG_STOP_TIMER = 2;
	private static final int MSG_UPDATE = 3;
	private static final int MSG_STOP_TIMEOUT = 4;

	@Override
	public void onCreate() {
		super.onCreate();

		// prefer
		sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		// handler
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START_TIMER:
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE, LOOP_TIME);
					if (mTimeout > 0) {
						mHandler.sendEmptyMessageDelayed(MSG_STOP_TIMEOUT,
								mTimeout * 60 * 1000);
					}
					break;
				case MSG_STOP_TIMER:
					sendWU();
					if (mHandler.hasMessages(MSG_UPDATE)) {
						mHandler.removeMessages(MSG_UPDATE);
					}
					if (mHandler.hasMessages(MSG_STOP_TIMEOUT)) {
						mHandler.removeMessages(MSG_STOP_TIMEOUT);
					}
					// after shutdown service
					stopSelf();
					break;
				case MSG_UPDATE:
					sendWU();
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE, LOOP_TIME);
					break;
				case MSG_STOP_TIMEOUT:
					stopStress();
					break;
				}
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// forced closing threading
		if (mHandler != null) {
			if (mHandler.hasMessages(MSG_UPDATE)) {
				mHandler.removeMessages(MSG_UPDATE);
			}
		}
		stopESThread();
	}

	private void startStress() {
		startESThread();
		mHandler.sendEmptyMessage(MSG_START_TIMER);
	}

	private void stopStress() {
		mHandler.sendEmptyMessage(MSG_STOP_TIMER);
		stopESThread();
	}

	private ESThread[] mStresses = null;
	private boolean mWorking = false;
	private SharedPreferences sharedPref;

	private synchronized void startESThread() {

		if (mWorking) {
			return;
		}
		mWorking = true;

		mAlgo = sharedPref.getString(getString(R.string.prefer_algo),
				getString(R.string.entryvalues_list_algo_default));
		mThreads = Integer.parseInt(sharedPref.getString(
				getString(R.string.prefer_threads),
				getString(R.string.entryvalues_list_threads_default)));

		mTimeout = Integer.parseInt(sharedPref.getString(
				getString(R.string.prefer_timeout),
				getString(R.string.entryvalues_list_timeout_default)));

		mStresses = new ESThread[mThreads];
		for (int i = 0; i < mStresses.length; i++) {
			mStresses[i] = new ESThread(mAlgo);
		}

		// start!
		for (ESThread es : mStresses) {
			es.start();
		}

		mTotal_timer_start = SystemClock.uptimeMillis();
		mTotal_cputime_start = Process.getElapsedCpuTime();
		mCurr_timer_start = mTotal_timer_start;
		mCurr_cputime_start = mTotal_cputime_start;
		mTotal_count_start = 0;
		for (ESThread es : mStresses) {
			mTotal_count_start += es.getCount();
		}
		mCurr_count_start = mTotal_count_start;
	}

	private synchronized void stopESThread() {

		if (mStresses == null) {
			return;
		}

		// cancel
		for (ESThread es : mStresses) {
			es.cancel();
		}

		mWorking = false;
	}

	private synchronized long countESThread() {

		if (mStresses == null) {
			return 0;
		}

		long cnt = 0;
		for (ESThread es : mStresses) {
			cnt += es.getCount();
		}
		return cnt;
	}

	private long mTotal_timer_start;
	private long mTotal_timer_end;
	private long mTotal_count_start;
	private long mTotal_count_end;
	private long mCurr_timer_start;
	private long mCurr_count_start;

	private long mTotal_cputime_start;
	private long mTotal_cputime_end;
	private long mCurr_cputime_start;

	private String mAlgo;
	private int mThreads;
	private int mTimeout;

	private void sendWU() {
		Intent intent = new Intent(ESStatus.ACTION_WU_STATUS);
		intent.putExtra(ESStatus.WU_STATUS, makeESStatus(mWorking));
		sendBroadcast(intent);
	}

	private ESStatus makeESStatus(boolean working) {

		mTotal_timer_end = SystemClock.uptimeMillis();
		mTotal_cputime_end = Process.getElapsedCpuTime();
		mTotal_count_end = countESThread();

		ESStatus esstat = new ESStatus();
		esstat.setWU( //
				mTotal_timer_end - mTotal_timer_start, // Total time
				mTotal_count_end - mTotal_count_start, // Total count
				mTotal_timer_end - mCurr_timer_start, // Current time
				mTotal_count_end - mCurr_count_start, // Current count
				mTotal_cputime_end - mTotal_cputime_start, // Total cputime
				mTotal_cputime_end - mCurr_cputime_start // Current cputime
		);
		esstat.setWorking(mWorking);
		esstat.setSetting(mAlgo, mThreads);

		if (working) {
			mCurr_timer_start = mTotal_timer_end;
			mCurr_cputime_start = mTotal_cputime_end;
			mCurr_count_start = mTotal_count_end;
		}

		return esstat;
	}

}
