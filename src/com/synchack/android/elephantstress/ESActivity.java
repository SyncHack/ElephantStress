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

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ESActivity extends Activity {

	private IESService mIESService = null;
	private ServiceConnection mServConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName cn, IBinder ibinder) {
			mIESService = IESService.Stub.asInterface(ibinder);
		}

		@Override
		public void onServiceDisconnected(ComponentName cn) {
			mIESService = null;
		}
	};

	final private BroadcastReceiver mWUreceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			ESStatus ess = intent.getParcelableExtra(ESStatus.WU_STATUS);
			boolean isworking = ess.isWorking();

			// WU
			DecimalFormat df = new DecimalFormat();

			double wu_raw_curr = ess.getCurrentWU();
			;
			if (wu_raw_curr < 10.0) {
				df.applyPattern("0.0000");
				df.setMaximumFractionDigits(4);
			} else {
				df.applyPattern("###0.0");
				df.setMaximumFractionDigits(1);
			}
			String strCurrWU = df.format(wu_raw_curr);

			double wu_raw_total = ess.getAverageWU();
			if (wu_raw_total < 10.0) {
				df.applyPattern("0.000");
				df.setMaximumFractionDigits(3);
			} else {
				df.applyPattern("###0.0");
				df.setMaximumFractionDigits(1);
			}
			String strTotalWU = df.format(wu_raw_total);

			// if( isworking ){
			// tvScore.setText(strCurrWU);
			// }else{
			tvScore.setText(strTotalWU);
			// }

			String strMes;
			strMes = "Information :\n";
			strMes += "  Algorithm : " + ess.getWorkingAlgo() + "\n";
			strMes += "  Threads   : " + ess.numWorkingThreads() + "\n";
			strMes += "\n";
			strMes += "Current : \n";
			strMes += "  WorkUnit : " + strCurrWU + " wu\n";
			strMes += "  CPUUsage : " + df.format(ess.getCurrentCPUUsage())
					+ " %\n";
			strMes += "\n";
			strMes += "Total : \n";
			strMes += "  WorkUnit : " + strTotalWU + " wu\n";
			strMes += "  CPUUsage : " + df.format(ess.getAverageCPUUsage())
					+ " %\n";

			tvInfo.setText(strMes);

			checkToggle(isworking);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ToggleButton btn_stress = (ToggleButton) findViewById(R.id.btn_stress);
		btn_stress.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (mIESService != null) {
					try {
						if (isChecked) {
							mIESService.start();
						} else {
							mIESService.stop();
						}
					} catch (RemoteException e) {
						// no need check...
					}
				}
			}

		});

		// init Text
		tvInfo = (TextView) findViewById(R.id.textInfo);
		tvScore = (TextView) findViewById(R.id.textScore);
		btnStress = (ToggleButton) findViewById(R.id.btn_stress);

		// init Service
		Intent intentService = new Intent(this, ESService.class);
		startService(intentService);
		bindService(intentService, mServConn, BIND_AUTO_CREATE);
	}

	private TextView tvInfo;
	private final static String KEY_INFO = "key_info";
	private TextView tvScore;
	private final static String KEY_SCORE = "key_score";
	private ToggleButton btnStress;
	private final static String KEY_STRESS = "key_stress";

	@Override
	protected void onResume() {
		super.onResume();
		checkToggle();

		registerReceiver(mWUreceive,
				new IntentFilter(ESStatus.ACTION_WU_STATUS));

	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mWUreceive);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(mServConn);
		if (isFinishing()) {
			stopService(new Intent(this, ESService.class));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(KEY_INFO, tvInfo.getText().toString());
		outState.putString(KEY_SCORE, tvScore.getText().toString());
		outState.putBoolean(KEY_STRESS, btnStress.isChecked());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// restore info.
		if (savedInstanceState != null) {
			tvInfo.setText(savedInstanceState.getString(KEY_INFO));
			tvScore.setText(savedInstanceState.getString(KEY_SCORE));
			btnStress.setChecked(savedInstanceState.getBoolean(KEY_STRESS));
		}
	}

	//
	// Menu
	//
	private static final int MENU_ID_PREFERENCE = 1;
	private static final int MENU_ID_ABOUT = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ID_PREFERENCE, 0, R.string.menu_setting).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ID_ABOUT, 0, R.string.menu_info).setIcon(
				android.R.drawable.ic_menu_info_details);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case MENU_ID_PREFERENCE:
			// force STOPed.
			if (btnStress.isChecked()) {
				btnStress.setChecked(false);
			}
			Intent intent = new Intent(ESActivity.this, PreferActivity.class);
			startActivity(intent);
			break;
		case MENU_ID_ABOUT:
			showDialog(DIALOG_INFORMATION);
			break;
		}
		return true;
	}

	//
	// Dialog
	//   
	private final static int DIALOG_INFORMATION = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_INFORMATION:
			String verName = getString(R.string.app_name) + " ("
					+ Util.getVersionName(getApplicationContext()) + ")";
			TextView tv = new TextView(this);
			tv.setAutoLinkMask(Linkify.WEB_URLS);
			String str = this.getString(R.string.dialog_message_infor);
			tv.setText(str);

			return new AlertDialog.Builder(ESActivity.this).setTitle(verName)
					.setIcon(R.drawable.ic_launcher).setView(tv).create();
		}
		return super.onCreateDialog(id);
	}

	//
	// functions
	//
	private void checkToggle() {

		if (!btnStress.isChecked()) {
			return;
		}
		if (mIESService == null) {
			// ???
			return;
		}

		boolean isworking = false;
		try {
			ESStatus ess = mIESService.getESStatus();
			isworking = ess.isWorking();
		} catch (RemoteException e) {
		}

		checkToggle(isworking);
	}

	private void checkToggle(boolean isworking_) {
		// checkが付いていてserviceが動いていないってことはservice側でtimeoutした
		// ということだろうからトルクボタンもオフにする必要がある

		if (btnStress.isChecked() && !isworking_) {
			btnStress.setChecked(false);
		}
	}

}