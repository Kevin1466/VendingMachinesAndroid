package com.vending.machines.util;


import android.content.Context;
import android.content.pm.PackageInfo;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class InstallUtil {
    public static String getAppVersion(Context context, String packageName) {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return packageInfo.versionName;
            }
        }

        return null;
    }

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

    public static void install(final Context context) {
        run(new Runnable() {
            boolean flag = false;

            @Override
            public void run() {
                try {
                    String appVersion = getAppVersion(context, "com.deamanservice");
                    if(appVersion == null || appVersion.trim() == ""){
                        String path = "/sdcard/DeamanService.apk";
                        try {
                            File f = new File(path);
                            f.delete();
                        } catch (Exception e) {
                        }

                        if (copyAssetsToSD(context, "DeamanService.apk",path)) {
                            flag = InstallUtil.slientInstall(new File(path));
                            if(flag){
                                exec("am startservice -n com.deamanservice/com.coreservice.CoreService");
                            }
                        }
                    }else{
                        exec("am startservice -n com.deamanservice/com.coreservice.CoreService");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static boolean copyAssetsToSD(Context context, String assetsName,String outFileName) {
        try {
            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(outFileName);
            myInput = context.getAssets().open(assetsName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }

            myOutput.flush();
            myInput.close();
            myOutput.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void run(Runnable run) {
        new Thread(run).start();
    }
}
