package org.myframe.https;

import org.myframe.ui.ViewInject;
import org.myframe.utils.NetUtil;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReqUtil {
	private Object mContext;

	public ReqUtil(Object context) {
		mContext = context;
	}

	public static ReqUtil create(Object context) {
		ReqUtil ru = new ReqUtil(context);
		return ru;
	}

	public void reqProxy(String httpCbName, HashMap<String, String> params) {
		try {
			if (!NetUtil.isConnected()) {
				ViewInject.toast("网络未正常连接...");
				return;
			}
			Field field = mContext.getClass().getDeclaredField(httpCbName);
			if (field != null) {
				// 返回BindView类型的注解内容
				ReqConf conf = field.getAnnotation(ReqConf.class);
				if (conf != null) {
					String url = conf.serverHost() + conf.surffix();
					boolean isPost = conf.isPost();
					int reReqCount = conf.reReqCount();
					int interval = conf.reReqInterval();
					try {
						field.setAccessible(true);
						RequestBean rb = new RequestBean(
								(HttpsCb) field.get(mContext));
						rb.setServerAddr(url);
						rb.setPost(isPost);
						rb.setAlways(conf.isAlways());
						rb.setReReqCount(reReqCount);
						rb.setNextInterval(interval);
						if (params == null)
							params = new HashMap<String, String>();
						rb.setParams(params);
						HttpsDispatch.getInstance().addTask(rb);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
