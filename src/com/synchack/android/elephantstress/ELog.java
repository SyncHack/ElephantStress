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

import android.util.Log;

public final class ELog {

	private static final String TAG = "ELog";
	private static final boolean enabled = true;
	
	private static String makeMsg(final String msg, final Throwable tr){
    	if(!enabled){ return ""; }
		StackTraceElement[] ste = tr.getStackTrace();
		return msg + " >>> " + ste[1].getMethodName() + "(" + ste[1].getFileName() + ":" + ste[1].getLineNumber() + ") ";
	}

    // verbose
    public static int v( final String msg ){
    	if(!enabled){ return 0; }
		return Log.v( TAG, makeMsg(msg, new Throwable()) );
    }
    public static int v( final String msg, final Throwable tr) {
    	if(!enabled){ return 0; }
    	return Log.v( TAG, makeMsg(msg, new Throwable()), tr );
    }
	
	// debug
    public static int d( final String msg ){
    	if(!enabled){ return 0; }
		return Log.d( TAG, makeMsg(msg, new Throwable()) );
    }
    public static int d( final String msg, final Throwable tr ){
    	if(!enabled){ return 0; }
    	return Log.d( TAG, makeMsg(msg, new Throwable()), tr );
    }
    
    // info
    public static int i( final String msg ){
    	if(!enabled){ return 0; }
    	return Log.i( TAG, makeMsg(msg, new Throwable()) );
    }
    public static int i( final String msg, final Throwable tr) {
    	if(!enabled){ return 0; }
    	return Log.i( TAG, makeMsg(msg, new Throwable()), tr );
    }
    
    // warning
    public static int w( final String msg ){
    	if(!enabled){ return 0; }
    	return Log.w( TAG, makeMsg(msg, new Throwable()) );
    }
    public static int w( final String msg, final Throwable tr) {
    	if(!enabled){ return 0; }
    	return Log.w( TAG, makeMsg(msg, new Throwable()), tr );
    }
    
    // error
    public static int e( final String msg ){
    	if(!enabled){ return 0; }
    	return Log.e( TAG, makeMsg(msg, new Throwable()) );
    }
    public static int e( final String msg, final Throwable tr) {
    	if(!enabled){ return 0; }
    	return Log.e( TAG, makeMsg(msg, new Throwable()), tr );
    }

}
