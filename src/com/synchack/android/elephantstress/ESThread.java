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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class ESThread extends Thread {

	public enum STAT {
		IDLE, RUNNING, CANCELED, ERROR_FATAL, DIGEST_ERROR
	};

	private byte[] buf = new byte[1024 * 4];
	private long counter = 0;
	private boolean cancel = false;
	private final String algo;
	private byte[] digest_org = null;
	private STAT stat = STAT.IDLE;

	ESThread() {
		algo = "MD5";
	}

	ESThread(String algo_) {
		algo = algo_;
	}

	@Override
	public void run() {
		stat = STAT.RUNNING;

		MessageDigest digester;
		try {
			digester = MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			counter = -1;
			stat = STAT.ERROR_FATAL;
			return;
		}

		for (;;) {
			if (cancel) {
				stat = STAT.CANCELED;
				return;
			}
			for (int i = 0; i < 250; i++) {
				if (cancel) {
					stat = STAT.CANCELED;
					return;
				}
				digester.update(buf);
			}
			byte[] digest = digester.digest();
			if (digest_org == null) {
				digest_org = digest;
			} else {
				// check digest...
				if (!Arrays.equals(digest, digest_org)) {
					stat = STAT.DIGEST_ERROR;
					counter = -1;
					return;
				}
			}

			counter++;
		}
	}

	public void cancel() {
		cancel = true;
	}

	public long getCount() {
		return counter;
	}

	public STAT getStatus() {
		return stat;
	}

	public String[] supportedAlgorithm() {
		ArrayList<String> lists = new ArrayList<String>();
		Set<?> names = Security.getAlgorithms("MessageDigest");
		for (Iterator<?> i = names.iterator(); i.hasNext();) {
			lists.add((String) i.next());
		}
		return lists.toArray(new String[0]);
	}
}
