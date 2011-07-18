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

import java.util.Arrays;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PreferActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private ListPreference lpalgo;
	private ListPreference lpthreads;
	private ListPreference lptimeout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		// algorithm
		lpalgo = (ListPreference) getPreferenceScreen().findPreference(
				getString(R.string.prefer_algo));

		ESThread est = new ESThread();
		String[] strLists = est.supportedAlgorithm();
		Arrays.sort(strLists);

		// lists
		lpalgo.setEntries(strLists);
		lpalgo.setEntryValues(strLists);

		// summary
		boolean bMatch = false;
		String selected_algo = lpalgo.getEntry().toString();
		for (String list : strLists) {
			if (list.equals(selected_algo)) {
				bMatch = true;
				break;
			}
		}
		if (!bMatch) {
			selected_algo = "MD5";
			lpalgo.setValue(selected_algo);
		}
		lpalgo.setSummary(selected_algo);

		// threads
		lpthreads = (ListPreference) getPreferenceScreen().findPreference(
				getString(R.string.prefer_threads));
		lpthreads.setSummary(lpthreads.getEntry());

		// timeout
		lptimeout = (ListPreference) getPreferenceScreen().findPreference(
				getString(R.string.prefer_timeout));
		lptimeout.setSummary(lptimeout.getEntry());
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		lpalgo.setSummary(lpalgo.getEntry());
		lpthreads.setSummary(lpthreads.getEntry());
		lptimeout.setSummary(lptimeout.getEntry());
	}

}
