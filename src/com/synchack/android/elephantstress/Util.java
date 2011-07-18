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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class Util {

	public static String getVersionName(Context context) {
		String versionStr = "(none)";
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
			versionStr = info.versionName;
		} catch (Exception e) {
		}
		return versionStr;
	}

	public static boolean isDebugging(Context context) {
		PackageManager pm = context.getPackageManager();
		ApplicationInfo appInfo = null;
		boolean bret = false;
		try {
			appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
			bret = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
		} catch (Exception e) {
		}
		return bret;
	}

	// http://javafaq.jp/S007.html
	public static int b2u(byte b) {
		return b & 0xFF;
	}

	public static void sleep(long min) {
		try {
			Thread.sleep(min);
		} catch (InterruptedException e) {
			// ELog.e("unknown thread error : " + e);
			e.printStackTrace();
		}
	}

	public static void showToast(Context ctx, String str) {
		Toast.makeText(ctx, str, Toast.LENGTH_LONG).show();
	}

	public static void showToast(Context ctx, int id) {
		Toast.makeText(ctx, id, Toast.LENGTH_LONG).show();
	}

}
