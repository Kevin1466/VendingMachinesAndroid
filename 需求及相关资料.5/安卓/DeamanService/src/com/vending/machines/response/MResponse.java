package com.vending.machines.response;

import java.util.List;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

public class MResponse {
	private String code;
	private String msg;
	private String data;

	public static MResponse obtainResponse(String json) throws Exception {
		try {
			MResponse mr = JSON.parseObject(json, MResponse.class);
			if (mr != null)
				return mr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new Exception("json parse error");
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		if (TextUtils.isEmpty(msg))
			msg = "";
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public <T> T obtain(Class cls) {
		try {
			T t = (T) JSON.parseObject(data, cls);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public <T> List<T> obtainList(Class cls){
		try {
			List<T> t = JSON.parseArray(data, cls);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
