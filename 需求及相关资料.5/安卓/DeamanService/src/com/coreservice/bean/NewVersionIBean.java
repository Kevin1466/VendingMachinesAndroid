package com.coreservice.bean;

/**
 * @author Administrator 新版本数据
 */
public class NewVersionIBean {
	private String appVersion;
	private String appDownloadUrl;
	private String appVersionDesc;

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getAppDownloadUrl() {
		return appDownloadUrl;
	}

	public void setAppDownloadUrl(String appDownloadUrl) {
		this.appDownloadUrl = appDownloadUrl;
	}

	public String getAppVersionDesc() {
		return appVersionDesc;
	}

	public void setAppVersionDesc(String appVersionDesc) {
		this.appVersionDesc = appVersionDesc;
	}
}
