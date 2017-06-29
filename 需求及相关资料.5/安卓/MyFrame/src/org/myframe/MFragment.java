package org.myframe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MFragment extends Fragment {

	private IntentFilter mIntentFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		registerBroadcast();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterBroadcast();
	}

	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			if (mIntentFilter != null
					&& mIntentFilter.hasAction(intent.getAction()))
				dealBroadcase(intent);
		}
	};

	protected void dealBroadcase(Intent intent) {

	}

	protected void setFilters(String... actions) {
		if (actions != null && actions.length > 0) {
			mIntentFilter = new IntentFilter();
			for (String action : actions) {
				mIntentFilter.addAction(action);
			}
			registerBroadcast();
		} else {
			mIntentFilter = null;
		}
	}

	public void registerBroadcast() {
		if (mIntentFilter != null)
			getActivity().registerReceiver(myBroadcastReceiver, mIntentFilter);
	}

	public void unRegisterBroadcast() {
		if (mIntentFilter != null)
			getActivity().unregisterReceiver(myBroadcastReceiver);
	}
}
