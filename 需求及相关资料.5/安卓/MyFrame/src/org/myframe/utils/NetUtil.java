package org.myframe.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import org.myframe.R;
import org.myframe.ui.ActivityStack;

/**
 * @author tianzc
 * @date 2015年11月13日
 * @description 网络工具集
 */
public class NetUtil {
	public static final int NETTYPE_WIFI = 0x01;// 无线wifi
	public static final int NETTYPE_CMWAP = 0x02;// wap网
	public static final int NETTYPE_CMNET = 0x03;// net网

	/**
	 * @return
	 * @date 2015年11月13日
	 * @author tianzc
	 * @description 网络是否连接
	 */
	public static boolean isConnected() {
		if (MContext.CONTEXT == null)
			return false;
		boolean flag=false;
		ConnectivityManager cm = (ConnectivityManager) MContext.CONTEXT
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null) {
				flag= ni != null && ni.isAvailable()
						&& ni.isConnectedOrConnecting();
			}
		}
		if(!flag){
			setNetwork();
		}
		return flag;
	}

	/**
	 * 网络未连接时，调用设置方法
	 */
	private static void setNetwork() {
		final Activity act = ActivityStack.create().topActivity();

		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (act == null)
					return;

				AlertDialog.Builder builder = new AlertDialog.Builder(act);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setTitle("网络提示信息");
				builder.setMessage("网络不可用，如果继续，请先设置网络！");
				builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = null;
						/**
						 * 判断手机系统的版本！如果API大于10 就是3.0+ 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
						 */
						if (android.os.Build.VERSION.SDK_INT > 10) {
							intent = new Intent(
									android.provider.Settings.ACTION_WIFI_SETTINGS);
						} else {
							intent = new Intent();
							ComponentName component = new ComponentName(
									"com.android.settings",
									"com.android.settings.WirelessSettings");
							intent.setComponent(component);
							intent.setAction("android.intent.action.VIEW");
						}

						if (act != null){
							act.startActivity(intent);
							act.sendBroadcast(new Intent("vending.machines.start.service"));
						}
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				builder.create();
				builder.show();
			}
		});
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */

	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) ActivityStack
				.create().topActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return netType;
		}
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!TextUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
}
