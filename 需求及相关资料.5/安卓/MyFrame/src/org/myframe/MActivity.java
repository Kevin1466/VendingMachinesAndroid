/*
 * Copyright (c) 2014, 张涛.
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
package org.myframe;

import org.myframe.ui.FrameActivity;
import org.myframe.ui.ActivityStack;
import org.myframe.utils.MLoger;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * @author kymjs (https://github.com/kymjs)
 */
public abstract class MActivity extends FrameActivity {

	/**
	 * 当前Activity状态
	 */
	public static enum ActivityState {
		RESUME, PAUSE, STOP, DESTROY
	}

	public Activity aty;
	/** Activity状态 */
	public ActivityState activityState = ActivityState.DESTROY;

	private IntentFilter mIntentFilter;

	/***************************************************************************
	 * 
	 * print Activity callback methods
	 * 
	 ***************************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		aty = this;
		ActivityStack.create().addActivity(this);
		MLoger.state(this.getClass().getName(), "---------onCreat ");
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		MLoger.state(this.getClass().getName(), "---------onStart ");
	}

	@Override
	protected void onResume() {
		super.onResume();
		activityState = ActivityState.RESUME;
		MLoger.state(this.getClass().getName(), "---------onResume ");
	}

	@Override
	protected void onPause() {
		super.onPause();
		activityState = ActivityState.PAUSE;
		MLoger.state(this.getClass().getName(), "---------onPause ");
		// onPageEnd 在onPause
		// 之前调用,因为 onPause 中会保存信息
	}

	@Override
	protected void onStop() {
		super.onStop();
		activityState = ActivityState.STOP;
		MLoger.state(this.getClass().getName(), "---------onStop ");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		MLoger.state(this.getClass().getName(), "---------onRestart ");
	}

	@Override
	protected void onDestroy() {
		activityState = ActivityState.DESTROY;
		MLoger.state(this.getClass().getName(), "---------onDestroy ");
		super.onDestroy();
		ActivityStack.create().finishActivity(this);
	}

	/**
	 * skip to @param(cls)，and call @param(aty's) finish() method
	 */
	@Override
	public void skipActivity(Activity aty, Class<?> cls) {
		showActivity(aty, cls);
		aty.finish();
	}

	/**
	 * skip to @param(cls)，and call @param(aty's) finish() method
	 */
	@Override
	public void skipActivity(Activity aty, Intent it) {
		showActivity(aty, it);
		aty.finish();
	}

	/**
	 * skip to @param(cls)，and call @param(aty's) finish() method
	 */
	@Override
	public void skipActivity(Activity aty, Class<?> cls, Bundle extras) {
		showActivity(aty, cls, extras);
		aty.finish();
	}

	/**
	 * show to @param(cls)，but can't finish activity
	 */
	@Override
	public void showActivity(Activity aty, Class<?> cls) {
		Intent intent = new Intent();
		intent.setClass(aty, cls);
		aty.startActivity(intent);
	}

	/**
	 * show to @param(cls)，but can't finish activity
	 */
	@Override
	public void showActivity(Activity aty, Intent it) {
		aty.startActivity(it);
	}

	/**
	 * show to @param(cls)，but can't finish activity
	 */
	@Override
	public void showActivity(Activity aty, Class<?> cls, Bundle extras) {
		Intent intent = new Intent();
		intent.putExtras(extras);
		intent.setClass(aty, cls);
		aty.startActivity(intent);
	}

	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			MLoger.debug("--->receiver broadcast" + intent.getAction());
			if (mIntentFilter != null
					&& mIntentFilter.hasAction(intent.getAction())) {
				try {
					dealBroadcase(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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

	@Override
	public void registerBroadcast() {
		if (mIntentFilter != null)
			registerReceiver(myBroadcastReceiver, mIntentFilter);
	}

	@Override
	public void unRegisterBroadcast() {
		if (mIntentFilter != null)
			unregisterReceiver(myBroadcastReceiver);
	}
}
