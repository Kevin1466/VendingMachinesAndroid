package com.coreservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.coreservice.bean.NewVersionIBean;
import com.vending.machines.response.MResponse;
import org.myframe.https.HttpsCb;
import org.myframe.https.HttpsDispatch;
import org.myframe.https.ReqConf;
import org.myframe.https.ReqUtil;
import org.myframe.https.RequestBean;
import org.myframe.ui.ViewInject;
import org.myframe.utils.MContext;
import org.myframe.utils.MLoger;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class CoreService extends Service {

	String sdPath = "/sdcard/VendingMachines.apk";
	String downApkUrl = "";

	@Override
	public void onCreate() {
		super.onCreate();

        MLoger.debug("-----> getCurrentVersion " + getCurrentVersion());
        MContext.CONTEXT = this;
        MLoger.debug("----------checkNewVersion-onCreate-");
        HttpsDispatch.getInstance().start();
		HashMap<String, String> params = new HashMap<String, String>();
		ReqUtil.create(CoreService.this).reqProxy("checkNewVersion", params);

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	private void runDownload() {
        MLoger.debug("runDownload 开始下载.........");
        InstallUtil.download(sdPath, downApkUrl, new OnDownloadRet() {

			@Override
			public void onResponse(boolean flag) {
				if (flag) {
					afterDownload();
				} else {
					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							runDownload();
						}
					}, 1000 * 60 * 5);
				}
			}
		});

	}

	private void afterDownload() {
		try {
            final boolean flag = InstallUtil.slientInstall(new File(sdPath));
            MLoger.debug(">>>>>>> install " + flag);
            if (!flag) {
                new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						afterDownload();
					}
				}, 1000 * 60 * 1);
				return;
			}
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					if (flag) {
						try {
							InstallUtil
									.exec("am start -n com.vending.machines/com.vending.machines.activity.MainActivity");
                            MLoger.debug(">>>>>>> start " + flag);
                        } catch (Exception e) {
                            e.printStackTrace();
						}
					}
				}
			}, 5000);
		} catch (Exception e) {
			System.out.println("-----CoreService------>" + e.toString());
			e.printStackTrace();
		}
	}

    private String getCurrentVersion() {
        return InstallUtil.getAppVersion(CoreService.this, "com.vending.machines");
    }

	@ReqConf(surffix = "/api/apk/latest?HardVerion=V1.1", reReqInterval = 10 * 60, isAlways = true)
	private HttpsCb checkNewVersion = new HttpsCb() {

		@Override
		public void onResponse(String data, RequestBean rb) {
			try {
				Calendar instance = Calendar.getInstance(Locale.CHINA);
				int hour = instance.get(Calendar.HOUR_OF_DAY);
				if(hour > 4 && hour < 23){
					return;
				}
                String currentVersion = getCurrentVersion();
                MResponse r = MResponse.obtainResponse(data);
                if ("0".equals(r.getCode())) {
					NewVersionIBean bean = r.obtain(NewVersionIBean.class);
					try {
						MLoger.debug("----------checkNewVersion-onResponse-"
                                + bean.getAppVersion());
                        if (currentVersion != null) {
                            MLoger.debug("---checkNewVersion --  " + currentVersion + currentVersion.compareToIgnoreCase(bean
                                    .getAppVersion()));
                        } else {
                            downApkUrl = bean.getAppDownloadUrl();
                            runDownload();
                            return;
                        }
                        if (currentVersion.compareToIgnoreCase(bean
                                .getAppVersion()) < 0) {
							downApkUrl = bean.getAppDownloadUrl();
							runDownload();
                            MLoger.debug("----------checkNewVersion-onResponse-22");
                        }
                    } catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					ViewInject.toast(r.getCode() + "--->" + r.getMsg());
				}
			} catch (Exception e) {
				ViewInject.toast("parse error");
			}
		}

		@Override
		public void onError(String error, RequestBean rb) {
			ViewInject.toast("goods list error");
		}
	};
}
