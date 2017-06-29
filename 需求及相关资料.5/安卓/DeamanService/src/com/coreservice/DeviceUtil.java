package com.coreservice;

public class DeviceUtil {
	public static String getDeviceIdentity(){
		return Dwin.getInstance().getChipID();
	}
}
