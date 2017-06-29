package com.coreservice;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.myframe.utils.MLoger;

public class InstallUtil {

	public static void obtainSu() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("su");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getAppVersion(Context context, String packageName) {
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(packageName)) {
				return packageInfo.versionName;
			}
		}

		return null;
	}

	/**
	 * ��Ĭ��װ
	 * 
	 * @param file
	 * @return
	 */
	public static boolean slientInstall(File file) {
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(out);
			dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
			dataOutputStream
					.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
							+ file.getPath());
			// �ύ����
			dataOutputStream.flush();
			// �ر�������
			dataOutputStream.close();
			out.close();
			int value = process.waitFor();

			// ����ɹ�
			if (value == 0) {
				result = true;
			} else if (value == 1) { // ʧ��
				result = false;
			} else { // δ֪���
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static boolean exec(String cmd) {

		boolean result = false;
		Process process = null;
		OutputStream out = null;
		try {
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(out);
			dataOutputStream.writeBytes(cmd + "\n");
			dataOutputStream.flush();
			dataOutputStream.close();
			out.close();
			int value = process.waitFor();

			if (value == 0) {
				result = true;
			} else if (value == 1) {
				result = false;
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;

	}

	public static void download(String sdFile, String url, OnDownloadRet lis) {
		try {
			File f = new File(sdFile);
			if (f.exists()) {
				f.delete();
			}
		} catch (Exception e) {
		}
		try {
			URL u = new URL(url);
			URLConnection con = u.openConnection();
			InputStream is = con.getInputStream();
			byte[] bs = new byte[1024];
			int len;
			OutputStream os = new FileOutputStream(sdFile);
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
				MLoger.debug(">>>>>>>>>");
			}
			os.close();
			is.close();
			if (lis != null)
				lis.onResponse(true);
		} catch (Exception e) {
			e.printStackTrace();
			if (lis != null)
				lis.onResponse(false);
		}
	}
}
