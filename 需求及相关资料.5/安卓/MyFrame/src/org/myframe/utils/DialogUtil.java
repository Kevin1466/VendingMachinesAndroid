package org.myframe.utils;

import org.myframe.R;
import org.myframe.ui.ActivityStack;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * @ClassName: DialogAssistant
 * @Description: 自定义界面dialog助手类
 * @author duxiyao
 * @date 2015年11月20日 上午11:29:16
 */
public class DialogUtil {

	/**
	 * 
	 * @Title: getPwdDialog
	 * @Description: 手势密码dialog
	 * @param
	 * @return
	 * @throws
	 * @author duxiyao
	 * @date 2015年11月20日 上午11:28:05
	 */
	public static Dialog getPwdDialog(View v) {
		int h = (int) DensityUtils.getScreenH(v.getContext());
		int w = (int) (DensityUtils.getScreenW(v.getContext()));
		Dialog d = getDialog(R.style.gesture_pwd_dialog, v, w, h,
				Gravity.CENTER, -1, -1, 1f);
		return d;
	}

	public static Dialog getCustomDialog(View v) {

		// int h = (int) (DensityUtils.getScreenH());
		// int w = (int) (DensityUtils.getScreenW());

		// Activity act = KJActivityStack.create().topActivity();
		//
		// Dialog d = new Dialog(act, R.style.ccpalertdialog);
		// d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// d.setContentView(v);
		// Window win = d.getWindow();
		// WindowManager.LayoutParams lp = win.getAttributes();
		// win.setGravity(Gravity.CENTER);
		// lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		// lp.alpha = 0.7f;
		// win.setAttributes(lp);

		int w = (int) ((DensityUtils.getScreenW(v.getContext())) * 0.8);
		int h = WindowManager.LayoutParams.WRAP_CONTENT;
		Dialog d = getDialog(R.style.ccpalertdialog, v, w, h, Gravity.CENTER,
				-1, -1, 0.7f);
		d.setCanceledOnTouchOutside(false);
		d.setCancelable(true);
		return d;
	}

	/**
	 * 
	 * @Title: getDialog
	 * @Description: 可扩展dialog函数
	 * @param
	 * @return
	 * @throws
	 * @author duxiyao
	 * @date 2015年11月20日 上午11:29:33
	 */
	public static Dialog getDialog(int style, View v, int w, int h,
			int gravity, int x, int y, float alpha) {

		Activity act = ActivityStack.create().topActivity();

		Dialog d = new Dialog(act, style);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(v);
		Window win = d.getWindow();
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.width = w;
		lp.height = h;
		if (x != -1)
			lp.x = x;
		if (y != -1)
			lp.y = y;
		lp.alpha = alpha;
		win.setAttributes(lp);
		win.setGravity(gravity);
		d.setCanceledOnTouchOutside(false);
		d.setCancelable(false);
		return d;
	}
}
