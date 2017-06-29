package com.vending.machines.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by lishichao on 16/7/9.
 */
public class AutoUpdateHandler {

    public static void checkServerAppVersion(final Context context) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.checkVersionUrl, new TextHttpResponseHandler("utf-8") {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(Constant.LOGTAG, "失败: " + responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(Constant.LOGTAG, "从服务器获取到的版本信息为: " + responseString);
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    String code = jsonObject.getString("code");
                    if (code.equals("0")) {
                        String newVersion = jsonObject.getJSONObject("data").getString("appVersion");
                        String appDownloadUrl = jsonObject.getJSONObject("data").getString("appDownloadUrl");
                        String currVersion = Utils.getVersionNumber(context);
                        newVersion = newVersion.replaceAll("[^0-9.]", "");
                        currVersion = currVersion.replaceAll("[^0-9.]", "");
                        if (Double.parseDouble(newVersion) > Double.parseDouble(currVersion)) {
                            Log.i(Constant.LOGTAG, String.format("发现新版本, 新版本为:%s ,当前版本为:%s", newVersion, currVersion));
                            Utils.longToast(context, String.format("发现新版本, 新版本为:%s ,当前版本为:%s", newVersion, currVersion));
                            downloadNewVersionApp(context, appDownloadUrl);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private static void downloadNewVersionApp(final Context context, String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.i(Constant.LOGTAG, "下载文件失败: " + file);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Log.i(Constant.LOGTAG, "下载文件为: " + file);

                Utils.longToast(context, "新版本下载完成, 自动更新开始");

                //安装
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                context.startActivity(intent);
            }
        });

    }

}
