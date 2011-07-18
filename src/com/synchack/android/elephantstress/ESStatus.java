package com.synchack.android.elephantstress;

import android.os.Parcel;
import android.os.Parcelable;

public class ESStatus implements Parcelable {

	public static final String ACTION_WU_STATUS = "com.synchack.android.elephantstress.esstatus";
	public static final String WU_STATUS = "wu_status";

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ESStatus> CREATOR = new Parcelable.Creator<ESStatus>() {
		public ESStatus createFromParcel(Parcel in) {
			return new ESStatus(in);
		}

		public ESStatus[] newArray(int size) {
			return new ESStatus[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(total_time);
		dest.writeLong(total_count);
		dest.writeLong(curr_time);
		dest.writeLong(curr_count);
		dest.writeLong(total_cputime);
		dest.writeLong(curr_cputime);
		dest.writeBooleanArray(bflags);
		dest.writeString(working_algo);
		dest.writeInt(working_threads);
	}

	public ESStatus(Parcel in) {
		total_time = in.readLong();
		total_count = in.readLong();
		curr_time = in.readLong();
		curr_count = in.readLong();
		total_cputime = in.readLong();
		curr_cputime = in.readLong();
		in.readBooleanArray(bflags);
		working_algo = in.readString();
		working_threads = in.readInt();
	}

	public ESStatus() {
	}

	// for App
	public double getCurrentWU() {
		if (curr_time == 0) {
			return 0;
		}

		return ((double) curr_count * 1000.0) / (double) curr_time;
	}

	public double getAverageWU() {
		if (total_time == 0) {
			return 0;
		}

		return ((double) total_count * 1000.0) / (double) total_time;
	}

	public double getCurrentCPUUsage() {
		if (curr_time == 0) {
			return 0;
		}

		return ((double) curr_cputime * 100) / (double) curr_time;
	}

	public double getAverageCPUUsage() {
		if (total_time == 0) {
			return 0;
		}

		return ((double) total_cputime * 100) / (double) total_time;
	}

	public boolean isWorking() {
		return bflags[0];
	}

	public String getWorkingAlgo() {
		return working_algo;
	}

	public int numWorkingThreads() {
		return working_threads;
	}

	// for Service
	private long total_time = 0;
	private long total_count = 0;
	private long curr_time = 0;
	private long curr_count = 0;
	private long total_cputime = 0;
	private long curr_cputime = 0;
	boolean[] bflags = new boolean[1];
	private String working_algo;
	private int working_threads;

	public void setWU(long total_time_, long total_count_, long curr_time_,
			long curr_count_, long total_cputime_, long curr_cputime_) {
		total_time = total_time_;
		total_count = total_count_;
		curr_time = curr_time_;
		curr_count = curr_count_;
		total_cputime = total_cputime_;
		curr_cputime = curr_cputime_;
	}

	public void setWorking(boolean isworking_) {
		bflags[0] = isworking_;
	}

	public void setSetting(String algo_, int threads_) {
		working_algo = algo_;
		working_threads = threads_;
	}

}
