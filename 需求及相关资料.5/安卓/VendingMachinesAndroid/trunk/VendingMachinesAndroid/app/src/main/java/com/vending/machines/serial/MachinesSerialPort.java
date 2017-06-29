package com.vending.machines.serial;

import android.util.Log;
import com.dwin.navy.serialportapi.SerailPortOpt;
import com.vending.machines.util.ByteUtil;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class MachinesSerialPort {

    public static final String[] m_iSerialPort = { "S0", "S1", "S2", "S3","S4", "USB0", "USB1" };
    public static final int[] baudrate = { 115200, 57600, 38400, 19200, 9600,4800, 2400, 1200, 300, };
    public static final int[] databits = { 5, 6, 7, 8 };
    public static final int[] stopbits = { 1, 2, };
    public static final int[] paritys = { 'n', 'o', 'e', 'm', 's' };

    private SerailPortOpt serialportopt;
    private InputStream mInputStream;

    public boolean isOpen = false;
    String data;

    public MachinesSerialPort(String devNum, int speed, int dataBits, int stopBits,
                      int parity) {
        serialportopt = new SerailPortOpt();
        openSerial(devNum, speed, dataBits, stopBits, parity);
    }

    private boolean openSerial(String devNum, int speed, int dataBits,
                               int stopBits, int parity) {
        serialportopt.mDevNum = devNum;
        serialportopt.mDataBits = dataBits;
        serialportopt.mSpeed = speed;
        serialportopt.mStopBits = stopBits;
        serialportopt.mParity = parity;

        FileDescriptor fd = serialportopt.openDev(serialportopt.mDevNum);
        if (fd == null) {
            return false;
        } else {
            serialportopt.setSpeed(fd, speed);
            serialportopt.setParity(fd, dataBits, stopBits, parity);
            mInputStream = serialportopt.getInputStream();
            isOpen = true;
            return true;
        }
    }

    public void closeSerial() {
        if (serialportopt.mFd != null) {
            serialportopt.closeDev(serialportopt.mFd);
            isOpen = false;
        }
    }

    public void sendData(String data, String type) {
        try {
            serialportopt.writeBytes(type.equals("HEX") ? HexString2Bytes(data
                    .length() % 2 == 1 ? data += "0" : data.replace(" ", ""))
                    : HexString2Bytes(toHexString(data)));
        } catch (Exception e) {

        }
    }

    public String receiveData(String type,int len) {
        byte[] buf = new byte[len];
        int size;
        if (mInputStream == null) {
            return null;
        }
        size = serialportopt.readBytes(buf);
        if (size > 0) {
            try {
                data = type.equals("HEX") ? bytesToHexString(buf, size)
                        : new String(buf, 0, size, "gb2312").trim().toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return data;
        } else {
            return null;
        }
    }

    public String receiveGunData(String type,byte[] bs) {
        byte[] buf = new byte[1024];
        int size;
        if (mInputStream == null) {
            return null;
        }
        size = serialportopt.readBytes(buf);
        if (size > 0) {
            try {
                bs[0]=buf[size-2];
                bs[1]=buf[size-1];
                data = type.equals("HEX") ? bytesToHexString(buf, size)
                        : new String(buf, 0, size, "gb2312").trim().toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return data;
        } else {
            return null;
        }
    }

    private String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    private static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public static String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            hex += " ";
            ret += hex;
        }
        return ret.toUpperCase();
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean sendData(byte[] encode) {
        try {
            Log.i("MachinesSerialPort", "send data" + ByteUtil.outputHexofByte(encode));
            return serialportopt.writeBytes(encode);
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] receiveData() {
        byte[] buf = new byte[1024];
        if(this.mInputStream == null) {
            return null;
        } else {
            int size = this.serialportopt.readBytes(buf);
            if(size > 0) {
                byte[] result = new byte[size];
                System.arraycopy(buf,0,result,0,size);
                return result;
            } else {
                return null;
            }
        }
    }
}
