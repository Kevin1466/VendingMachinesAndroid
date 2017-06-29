package org.myframe.utils;

import org.myframe.ui.ActivityStack;

import android.content.Intent;
import android.os.Bundle;

public class BroadCastUtil {
	public static void sendBroadCast(String action) {
		ActivityStack.create().topActivity().sendBroadcast(new Intent(action));
	}

	public static void sendBroadCast(String action, Bundle params) {
		Intent it = new Intent(action);
		if (params != null)
			it.putExtras(params);
		ActivityStack.create().topActivity().sendBroadcast(it);
	}
}
