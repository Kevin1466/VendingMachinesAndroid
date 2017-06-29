package com.vending.machines.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteUtil {

    /** The maximum number of bytes in a UDP packet. */
    public static final int MAX_UDP_PACKET_SIZE = 65537;

    /** Number of bytes in a Java short. */
    public static final int NUM_BYTES_IN_SHORT = 2;

    /** Number of bytes in a Java int. */
    public static final int NUM_BYTES_IN_INT = 4;

    /** Number of bytes in a Java long. */
    public static final int NUM_BYTES_IN_LONG = 8;

    private static long maxValueCache[] = new long[64];

    static {
        for (int i = 1; i < 64; i++) {
            maxValueCache[i] = (long) (((long) 1 << i) - 1);
        }
    }

    /***************************************************************************
     * Methods
     **************************************************************************/


    /**
     *低位在前的long转化为4个byte
     * @return
     */
    public static byte[] long2Bytes(long value,int length){
        byte[] result = new byte[length];
        for(int i=0; i<length; i++){
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
    /**
     *低位在前的int转化为4个byte
     * @return
     */
    public static byte[] int2Bytes(int value,int length){
        byte[] result = new byte[length];
        for(int i=0; i<length; i++){
            result[i] = (byte) (value & 0xff);
            value >>= 8;
        }
        return result;
    }
    /**
     *低位在前的4个byte转化为int
     * @return
     */
    public static int bytes2Int(byte[] data,int offset,int length){
        int value = 0;
        for(int i=offset+length-1;i>=offset;i--){
            value <<= 8;
            value |=(data[i]&0xff);
        }
        return value;
    }

    public static void main(String[] args) {
        byte[] data = {(byte)0xea,0x03};
        int y = bytes2Int(data,0,2);
        System.out.println(y);
    }

    /**
     * Convert a Java short to a 2-byte array.
     *
     * @param s
     *            A short value.
     * @return A 2-byte array representing the short value.
     */
    public static byte[] shortToBytes(short s) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_SHORT);
        byteBuffer.putShort(s);
        return byteBuffer.array();

    }

    /**
     * Convert a byte array to a Java short.
     *
     * @param bytes
     *            A byte array.
     * @return A Java short.
     */
    public static short bytesToShort(byte[] bytes) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getShort();

    }

    /**
     * Convert a short to a hex representation.
     *
     * @param s
     *            A Java short.
     * @return A hex representation of the short.
     */
    public static String shortToHex(short s) {

        // Java doesn't have a method for converting a short to hex, so
        // use an int instead.
        int i = (int) s;
        return Integer.toHexString(i);

    }

    /**
     * Convert a hex representation to a Java short.
     *
     * @param s
     *            A hex representation.
     * @return A Java short.
     */
    public static short hexToShort(String s) {

        return Short.parseShort(s, 16);

    }

    /**
     * Convert a Java int to a 4-byte array.
     *
     * @param i
     *            A Java int value.
     * @return A 4-byte array representing the int value.
     */
    public static byte[] intToBytes(int i) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_INT);
        byteBuffer.putInt(i);
        return byteBuffer.array();

    }

    /**
     * Convert a byte array to a Java int.
     *
     * @param bytes
     *            A byte array.
     * @return A Java int.
     */
    public static int bytesToInt(byte[] bytes) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getInt();

    }

    /**
     * Convert a byte array to a Java int.
     *
     * @param bytes
     *            A byte array.
     * @return A Java int.
     */
    public static int bytesToInt(byte[] bytes,int offset,int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes,offset,length);
        return byteBuffer.getInt();

    }

    /**
     * Convert an int to a hex representation.
     *
     * @param i A Java int.
     * @return A hex representation of the int.
     */
    public static String intToHex(int i) {

        return Integer.toHexString(i);

    }

    /**
     * Convert a hex representation to a Java int.
     *
     * @param s
     *            A hex representation.
     * @return A Java int.
     */
    public static int hexToInt(String s) {

        return Integer.parseInt(s, 16);

    }

    /**
     * Convert a Java long to a 4-byte array.
     *
     * @param l
     *            A Java long value.
     * @return A 4-byte array representing the int value.
     */
    public static byte[] longToBytes(long l) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(NUM_BYTES_IN_LONG);
        byteBuffer.putLong(l);
        return byteBuffer.array();

    }

    /**
     * Convert a byte array to a Java long.
     *
     * @param bytes
     *            A byte array.
     * @return A Java long.
     */
    public static long bytesToLong(byte[] bytes) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.getLong();

    }

    /**
     * Convert a long to a hex representation.
     *
     * @param l A Java long.
     * @return A hex representation of the long.
     */
    public static String longToHex(long l) {

        return Long.toHexString(l);

    }

    /**
     * Convert a hex representation to a Java long.
     *
     * @param s
     *            A hex representation.
     * @return A Java long.
     */
    public static long hexToLong(String s) {

        return Long.parseLong(s, 16);

    }

    /**
     * Get a byte array in a printable binary form.
     *
     * @param bytes
     *            The byte to be writen.
     * @return A String reprentation of the byte.
     */
    public static String writeBytes(byte[] bytes) {

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {

            // New line every 4 bytes
            if (i % 4 == 0) {

                stringBuffer.append("\n");
            }

            stringBuffer.append(writeBits(bytes[i]) + " ");
        }

        return stringBuffer.toString();

    }

    /**
     * Get a byte array in a printable binary form.
     *
     * @param bytes
     *            The byte to be writen.
     * @return A String reprentation of the byte.
     */
    public static String writeBytes(byte[] bytes, int packetLength) {

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < packetLength; i++) {

            // New line every 4 bytes
            if (i % 4 == 0) {

                stringBuffer.append("\n");
            }

            stringBuffer.append(writeBits(bytes[i]) + " ");

        }

        return stringBuffer.toString();

    }

    /**
     * Get a byte in a printable binary form.
     *
     * @param b
     *            The byte to be writen.
     * @return A String reprentation of the byte.
     */
    public static String writeBits(byte b) {

        StringBuffer stringBuffer = new StringBuffer();
        int bit = 0;

        for (int i = 7; i >= 0; i--) {

            bit = (b >>> i) & 0x01;
            stringBuffer.append(bit);

        }

        return stringBuffer.toString();

    }

    /**
     * Get the maximum value for the number of unsigned bits.
     *
     * @param i
     *            The number of unsigned bits.
     * @return The maximum value for the number of unsigned bits.
     */
    public static int getMaxIntValueForNumBits(int i) {

        if (i >= 32)
            throw new RuntimeException("Number of bits exceeds Java int.");
        else
            return (int) maxValueCache[i];

    }

    /**
     * Get the maximum value for the number of unsigned bits.
     *
     * @param i
     *            The number of unsigned bits.
     * @return The maximum value for the number of unsigned bits.
     */
    public static long getMaxLongValueForNumBits(int i) {

        if (i >= 64)
            throw new RuntimeException("Number of bits exceeds Java long.");
        else
            return maxValueCache[i];

    }

    public static byte[] byteArraysMerge(byte[]...bs){
        int length = 0;
        for(int i=0;i<bs.length;i++){
            length += bs[i].length;
        }
        byte[] result = new byte[length];
        int index = 0;
        for(int i=0;i<bs.length;i++){
            System.arraycopy(bs[i], 0, result, index, bs[i].length);
            index += bs[i].length;
        }
        return result;
    }

    public static byte[] convertNum2DecimalBytes(long number)
    {
        if(number <= 0)return new byte[0];
        byte[] bytes = Long.toString(number).getBytes();
        for(int i = 0; i < bytes.length; i ++)
        {
            bytes[i] -= 0x30;
        }
        return bytes;
    }

    public static byte[] convertNum2DecimalBytes(long number, int len)
    {
        if(-1 == len)return convertNum2DecimalBytes(number);
        byte[] bytes = new byte[len];
        byte[] _bytes = Long.toString(number).getBytes();
        for(int i = 0; i < _bytes.length && i < len; i ++)
        {
            bytes[len-i-1] = (byte)(_bytes[_bytes.length-i-1] - 0x30);
        }
        return bytes;
    }
    public static byte[] convertNum2HexBytes(long number, int len)
    {
        if(-1 == len)return convertNum2HexBytes(number);
        byte[] bytes = new byte[len];

        long left = number;
        for(int i = len - 1;i >= 0 && left > 0; i --, left>>=4)
        {
            bytes[i] = (byte) (left % 16);
        }
        return bytes;
    }

    private static byte[] convertNum2HexBytes(long number)
    {
        List<Byte> _bytes = new ArrayList<Byte>();
        long left = number;
        for(; left > 0; left>>=4)_bytes.add((byte)(left%16));
        byte[] bytes = new byte[_bytes.size()];
        for(int i = 0; i < bytes.length; i ++)bytes[i] = _bytes.get(_bytes.size()-i-1);
        return bytes;
    }

    public static int convertLowBytes2Num(byte[] bytes){
        int num = 0;
        for(int i = 0; i < bytes.length; i ++)
        {
            num = num * 10 + bytes[i];
        }
        return num;
    }

    public static byte[] convert2DTo1D(byte[][] bytes)
    {
        int length_1 = bytes.length;
        int totalLength = 0;
        for (int i = 0; i < length_1; i++)
        {
            if(null != bytes[i])totalLength = totalLength + bytes[i].length;
        }
        byte[] _bytes = new byte[totalLength];
        int count = 0;
        for (int i = 0; i < length_1; i++)
        {
            if(null == bytes[i])continue;
            for (int j = 0; j < bytes[i].length; j++)
            {
                _bytes[count++] = bytes[i][j];
            }
        }
        return _bytes;
    }
    public static byte[] convert2DTo1D(List<byte[]> bytes)
    {
        int length_1 = bytes.size();
        int totalLength = 0;
        for (int i = 0; i < length_1; i++)
        {
            if(null != bytes.get(i))totalLength = totalLength + bytes.get(i).length;
        }
        byte[] _bytes = new byte[totalLength];
        int count = 0;
        for (int i = 0; i < length_1; i++)
        {
            if(null == bytes.get(i))continue;
            for (int j = 0; j < bytes.get(i).length; j++)
            {
                _bytes[count++] = bytes.get(i)[j];
            }
        }
        return _bytes;
    }

    public static byte getCheckSum(byte[] buf, int start, int end)
    {
        byte cs = (byte) 0xff;
        for(int i = start; i < end; i ++)
        {
            cs ^= buf[i];
        }
        return cs;
    }

    public static boolean testCheckSum(byte[] buf, int start, int end, byte cs)
    {
        byte _cs = getCheckSum(buf, start, end);
        return _cs == cs;
    }

    public static String outputHexofByte(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if(null != bytes)
        {
            for(byte b : bytes)
            {
                sb.append(String.format("%02x", b)).append(' ');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("]");
        return sb.toString();
    }
    public static String outputHexofByte(byte[] bytes, int offset)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        if(null != bytes)
        {
            for(byte b : bytes)
            {
                if(offset -- > 0)sb.append(String.format("%2x", b)).append(' ');
                else break;
            }
        }
        sb.append(" ]");
        return sb.toString();
    }
    public static String outputHexofByte(byte[] bytes, int begin, int end)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        if(null != bytes)
        {
            for(int i = begin>=0?begin:0; i < bytes.length && i < end; i ++)
            {
                sb.append(String.format("%2x", bytes[i])).append(' ');
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    public static byte[] format2ByteArray(long target, String format)
    {
        if(null == format || format.isEmpty())return convertNum2Bytes(target);
        else
        {
            // 找到f
            int i = 0;
            for(; i < format.length(); i ++)if('d' == format.charAt(i) || 'x' == format.charAt(i))break;
            char f = (i < format.length() ? format.charAt(i) : ' ');
            // 找到l，即长度
            int l = -1;
            boolean biggerThan = false; // 是否包含“至少”含义的长度
            String sl;
            if('+' == format.charAt(i-1))
            {
                biggerThan = true;
                sl = format.substring(1, i-1);
            }
            else
            {
                sl = format.substring(1, i);
            }
            if(sl.length() > 0)l = Integer.parseInt(sl);
            // 找到b，即进制
            int b = -1;
            if(i < format.length()-1)
            {
                String sb = format.substring(i+1);
                b = Integer.parseInt(sb);
            }
            switch(f)
            {
                case 'd':
                    if(b < 0)b = 10;
                    break;
                case 'x':
                    b = 16;
                    break;
                default:
                    b = 256;
                    break;
            }
            return format2ByteArray(target, l, b, biggerThan);
        }
    }
    public static byte[] format2ByteArray(long target, int len, int base, boolean biggerThan)
    {
        return convertNum2Bytes(target, len, base, biggerThan);
    }

    public static byte[] convertNum2Bytes(long target, int l)
    {
        if(-1 == l)return convertNum2Bytes(target);
        long left = target;
        byte bytes[] = new byte[l];
        for(int i = 0; i < l && left > 0; i ++, left>>=8)bytes[l-i-1] = (byte) (left%256);
        return bytes;
    }
    private static byte[] convertNum2Bytes(long target, int len, int base, boolean biggerThan)
    {
        int _len = (-1 != len && !biggerThan ? len : 8);

        long left = target;
        byte bytes[] = new byte[_len];
        int i = 0;
        for(; i < _len && left > 0; i ++, left/=base)bytes[_len-i-1] = (byte) (left%base);

        if((-1 == len || biggerThan) && i < _len)
        {// 需要重组
            if(biggerThan && i < len)i = len;
            byte[] res = new byte[i];
            for(int j = 8-i; j < 8; j ++)
            {
                res[j+i-8] = bytes[j];
            }
            return res;
        }
        return bytes;
    }

    private static byte[] convertNum2Bytes(long target)
    {
        byte bytes[] = convertNum2Bytes(target, 8);
        int i;
        for(i = 0; i < 8; i ++)
        {
            if(bytes[i] != 0)
                break;
        }
        byte res[] = new byte[8-i];
        for(int j = i; j < 8; j ++)
        {
            res[j-i] = bytes[j];
        }
        return res;
    }

    public static boolean isHexChar(char c)
    {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }

    public static List<String> convertNum2DecStringList(long num)
    {
        List<String> strList = new ArrayList<String>();
        String numStr = String.valueOf(num);
        for(int i = 0; i < numStr.length(); i ++)
        {
            char c = numStr.charAt(i);
            strList.add(String.valueOf(c));
        }
        return strList;
    }
}
