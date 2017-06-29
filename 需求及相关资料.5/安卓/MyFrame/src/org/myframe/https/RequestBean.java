package org.myframe.https;

import java.util.HashMap;

public class RequestBean implements Comparable<RequestBean> {
	private String serverAddr;
	private HashMap<String, String> params;
	private HttpsCb cb;
	private boolean isPost = true;
	private int mReReqCount = 3;
	private int mCurReqCount = 0;
	private long mNextReqTime;
	private int mNextReqInterval;
	private boolean isCancle = false;
	private boolean isAlways = false;

	public RequestBean(HttpsCb cb) {
		this.cb = cb;
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public HttpsCb getCb() {
		return cb;
	}

	public boolean isPost() {
		return isPost;
	}

	public void setPost(boolean isPost) {
		this.isPost = isPost;
	}

	public void setReReqCount(int count) {
		mReReqCount = count;
	}

	public void setNextInterval(int millis) {
		mNextReqInterval = millis;
	}

	public boolean isCancle() {
		return isCancle;
	}

	public void setCancle(boolean isCancle) {
		this.isCancle = isCancle;
	}

	public boolean isAlways() {
		return isAlways;
	}

	public void setAlways(boolean isAlways) {
		this.isAlways = isAlways;
	}

	/**
	 * @return -1 还没到时间; 0 这是最后一次; 1执行完这次请求还有下次
	 */
	public int isReq() {
		if (isAlways) {
			if (System.currentTimeMillis() >= mNextReqTime) {
				mNextReqTime = System.currentTimeMillis() + mNextReqInterval
						* 1000;
				return 1;
			} else
				return -1;
		}

		if (mCurReqCount <= mReReqCount
				&& System.currentTimeMillis() >= mNextReqTime) {
			mCurReqCount++;
			if (mCurReqCount > mReReqCount) {
				return 0;
			}
			mNextReqTime = System.currentTimeMillis() + mNextReqInterval * 1000;
			return 1;
		} else
			return -1;
	}

	@Override
	public int compareTo(RequestBean another) {
		return 1;
	}
}
