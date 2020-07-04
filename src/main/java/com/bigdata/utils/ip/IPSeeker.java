//package com.bigdata.utils.ip;
//
///**
// * @Author: ly
// * @Date: 2020/6/29 22:07
// * @Version 1.0
// */
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.io.UnsupportedEncodingException;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.channels.FileChannel.MapMode;
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.StringTokenizer;
//
//public class IPSeeker {
//    public static final String ERROR_RESULT = "错误的IP数据库文件";
//    private static final int IP_RECORD_LENGTH = 7;
//    private static final byte AREA_FOLLOWED = 1;
//    private static final byte NO_AREA = 2;
//    private Hashtable ipCache = new Hashtable();
//    private RandomAccessFile ipFile;
//    private MappedByteBuffer mbb;
//    private static IPSeeker instance = null;
//    private long ipBegin;
//    private long ipEnd;
//    private IPSeeker.IPLocation loc = new IPSeeker.IPLocation();
//    private byte[] buf = new byte[100];
//    private byte[] b4 = new byte[4];
//    private byte[] b3 = new byte[3];
//
//    protected IPSeeker(String ipFilePath) {
//        try {
//            this.ipFile = new RandomAccessFile(ipFilePath, "r");
//        } catch (FileNotFoundException var3) {
//            System.out.println("IP地址信息文件没有找到，IP显示功能将无法使用");
//            this.ipFile = null;
//        }
//
//        if (this.ipFile != null) {
//            try {
//                this.ipBegin = this.readLong4(0L);
//                this.ipEnd = this.readLong4(4L);
//                if (this.ipBegin == -1L || this.ipEnd == -1L) {
//                    this.ipFile.close();
//                    this.ipFile = null;
//                }
//            } catch (IOException var4) {
//                System.out.println("IP地址信息文件格式有错误，IP显示功能将无法使用");
//                this.ipFile = null;
//            }
//        }
//
//    }
//
//    public static IPSeeker getInstance(String ipFilePath) {
//        if (instance == null) {
//            instance = new IPSeeker(ipFilePath);
//        }
//
//        return instance;
//    }
//
//    public List getIPEntriesDebug(String s) {
//        List ret = new ArrayList();
//        long endOffset = this.ipEnd + 4L;
//
//        for(long offset = this.ipBegin + 4L; offset <= endOffset; offset += 7L) {
//            long temp = this.readLong3(offset);
//            if (temp != -1L) {
//                IPSeeker.IPLocation loc = this.getIPLocation(temp);
//                if (loc.country.indexOf(s) != -1 || loc.area.indexOf(s) != -1) {
//                    IPSeeker.IPEntry entry = new IPSeeker.IPEntry();
//                    entry.country = loc.country;
//                    entry.area = loc.area;
//                    this.readIP(offset - 4L, this.b4);
//                    entry.beginIp = IPSeeker.IPSeekerUtils.getIpStringFromBytes(this.b4);
//                    this.readIP(temp, this.b4);
//                    entry.endIp = IPSeeker.IPSeekerUtils.getIpStringFromBytes(this.b4);
//                    ret.add(entry);
//                }
//            }
//        }
//
//        return ret;
//    }
//
//    public List getIPEntries(String s) {
//        ArrayList ret = new ArrayList();
//
//        try {
//            if (this.mbb == null) {
//                FileChannel fc = this.ipFile.getChannel();
//                this.mbb = fc.map(MapMode.READ_ONLY, 0L, this.ipFile.length());
//                this.mbb.order(ByteOrder.LITTLE_ENDIAN);
//            }
//
//            int endOffset = (int)this.ipEnd;
//
//            for(int offset = (int)this.ipBegin + 4; offset <= endOffset; offset += 7) {
//                int temp = this.readInt3(offset);
//                if (temp != -1) {
//                    IPSeeker.IPLocation loc = this.getIPLocation(temp);
//                    if (loc.country.indexOf(s) != -1 || loc.area.indexOf(s) != -1) {
//                        IPSeeker.IPEntry entry = new IPSeeker.IPEntry();
//                        entry.country = loc.country;
//                        entry.area = loc.area;
//                        this.readIP(offset - 4, this.b4);
//                        entry.beginIp = IPSeeker.IPSeekerUtils.getIpStringFromBytes(this.b4);
//                        this.readIP(temp, this.b4);
//                        entry.endIp = IPSeeker.IPSeekerUtils.getIpStringFromBytes(this.b4);
//                        ret.add(entry);
//                    }
//                }
//            }
//        } catch (IOException var8) {
//            System.out.println(var8.getMessage());
//        }
//
//        return ret;
//    }
//
//    private int readInt3(int offset) {
//        this.mbb.position(offset);
//        return this.mbb.getInt() & 16777215;
//    }
//
//    private int readInt3() {
//        return this.mbb.getInt() & 16777215;
//    }
//
//    public String getCountry(byte[] ip) {
//        if (this.ipFile == null) {
//            return "错误的IP数据库文件";
//        } else {
//            String ipStr = IPSeeker.IPSeekerUtils.getIpStringFromBytes(ip);
//            IPSeeker.IPLocation loc;
//            if (this.ipCache.containsKey(ipStr)) {
//                loc = (IPSeeker.IPLocation)this.ipCache.get(ipStr);
//                return loc.country;
//            } else {
//                loc = this.getIPLocation(ip);
//                this.ipCache.put(ipStr, loc.getCopy());
//                return loc.country;
//            }
//        }
//    }
//
//    public String getCountry(String ip) {
//        return this.getCountry(IPSeeker.IPSeekerUtils.getIpByteArrayFromString(ip));
//    }
//
//    public String getArea(byte[] ip) {
//        if (this.ipFile == null) {
//            return "错误的IP数据库文件";
//        } else {
//            String ipStr = IPSeeker.IPSeekerUtils.getIpStringFromBytes(ip);
//            IPSeeker.IPLocation loc;
//            if (this.ipCache.containsKey(ipStr)) {
//                loc = (IPSeeker.IPLocation)this.ipCache.get(ipStr);
//                return loc.area;
//            } else {
//                loc = this.getIPLocation(ip);
//                this.ipCache.put(ipStr, loc.getCopy());
//                return loc.area;
//            }
//        }
//    }
//
//    public String getArea(String ip) {
//        return this.getArea(IPSeeker.IPSeekerUtils.getIpByteArrayFromString(ip));
//    }
//
//    public IPSeeker.IPLocation getIPLocation(byte[] ip) {
//        IPSeeker.IPLocation info = null;
//        long offset = this.locateIP(ip);
//        if (offset != -1L) {
//            info = this.getIPLocation(offset);
//        }
//
//        if (info == null) {
//            info = new IPSeeker.IPLocation();
//            info.country = "未知国家";
//            info.area = "未知地区";
//        }
//
//        return info;
//    }
//
//    private long readLong4(long offset) {
//        long ret = 0L;
//
//        try {
//            this.ipFile.seek(offset);
//            ret |= (long)(this.ipFile.readByte() & 255);
//            ret |= (long)(this.ipFile.readByte() << 8 & '\uff00');
//            ret |= (long)(this.ipFile.readByte() << 16 & 16711680);
//            ret |= (long)(this.ipFile.readByte() << 24 & -16777216);
//            return ret;
//        } catch (IOException var6) {
//            return -1L;
//        }
//    }
//
//    private long readLong3(long offset) {
//        long ret = 0L;
//
//        try {
//            this.ipFile.seek(offset);
//            this.ipFile.readFully(this.b3);
//            ret |= (long)(this.b3[0] & 255);
//            ret |= (long)(this.b3[1] << 8 & '\uff00');
//            ret |= (long)(this.b3[2] << 16 & 16711680);
//            return ret;
//        } catch (IOException var6) {
//            return -1L;
//        }
//    }
//
//    private long readLong3() {
//        long ret = 0L;
//
//        try {
//            this.ipFile.readFully(this.b3);
//            ret |= (long)(this.b3[0] & 255);
//            ret |= (long)(this.b3[1] << 8 & '\uff00');
//            ret |= (long)(this.b3[2] << 16 & 16711680);
//            return ret;
//        } catch (IOException var4) {
//            return -1L;
//        }
//    }
//
//    private void readIP(long offset, byte[] ip) {
//        try {
//            this.ipFile.seek(offset);
//            this.ipFile.readFully(ip);
//            byte temp = ip[0];
//            ip[0] = ip[3];
//            ip[3] = temp;
//            temp = ip[1];
//            ip[1] = ip[2];
//            ip[2] = temp;
//        } catch (IOException var5) {
//            System.out.println(var5.getMessage());
//        }
//
//    }
//
//    private void readIP(int offset, byte[] ip) {
//        this.mbb.position(offset);
//        this.mbb.get(ip);
//        byte temp = ip[0];
//        ip[0] = ip[3];
//        ip[3] = temp;
//        temp = ip[1];
//        ip[1] = ip[2];
//        ip[2] = temp;
//    }
//
//    private int compareIP(byte[] ip, byte[] beginIp) {
//        for(int i = 0; i < 4; ++i) {
//            int r = this.compareByte(ip[i], beginIp[i]);
//            if (r != 0) {
//                return r;
//            }
//        }
//
//        return 0;
//    }
//
//    private int compareByte(byte b1, byte b2) {
//        if ((b1 & 255) > (b2 & 255)) {
//            return 1;
//        } else {
//            return (b1 ^ b2) == 0 ? 0 : -1;
//        }
//    }
//
//    private long locateIP(byte[] ip) {
//        long m = 0L;
//        this.readIP(this.ipBegin, this.b4);
//        int r = this.compareIP(ip, this.b4);
//        if (r == 0) {
//            return this.ipBegin;
//        } else if (r < 0) {
//            return -1L;
//        } else {
//            long i = this.ipBegin;
//            long j = this.ipEnd;
//
//            while(i < j) {
//                m = this.getMiddleOffset(i, j);
//                this.readIP(m, this.b4);
//                r = this.compareIP(ip, this.b4);
//                if (r > 0) {
//                    i = m;
//                } else {
//                    if (r >= 0) {
//                        return this.readLong3(m + 4L);
//                    }
//
//                    if (m == j) {
//                        j -= 7L;
//                        m = j;
//                    } else {
//                        j = m;
//                    }
//                }
//            }
//
//            m = this.readLong3(m + 4L);
//            this.readIP(m, this.b4);
//            r = this.compareIP(ip, this.b4);
//            if (r <= 0) {
//                return m;
//            } else {
//                return -1L;
//            }
//        }
//    }
//
//    private long getMiddleOffset(long begin, long end) {
//        long records = (end - begin) / 7L;
//        records >>= 1;
//        if (records == 0L) {
//            records = 1L;
//        }
//
//        return begin + records * 7L;
//    }
//
//    private IPSeeker.IPLocation getIPLocation(long offset) {
//        try {
//            this.ipFile.seek(offset + 4L);
//            byte b = this.ipFile.readByte();
//            if (b == 1) {
//                long countryOffset = this.readLong3();
//                this.ipFile.seek(countryOffset);
//                b = this.ipFile.readByte();
//                if (b == 2) {
//                    this.loc.country = this.readString(this.readLong3());
//                    this.ipFile.seek(countryOffset + 4L);
//                } else {
//                    this.loc.country = this.readString(countryOffset);
//                }
//
//                this.loc.area = this.readArea(this.ipFile.getFilePointer());
//            } else if (b == 2) {
//                this.loc.country = this.readString(this.readLong3());
//                this.loc.area = this.readArea(offset + 8L);
//            } else {
//                this.loc.country = this.readString(this.ipFile.getFilePointer() - 1L);
//                this.loc.area = this.readArea(this.ipFile.getFilePointer());
//            }
//
//            return this.loc;
//        } catch (IOException var6) {
//            return null;
//        }
//    }
//
//    private IPSeeker.IPLocation getIPLocation(int offset) {
//        this.mbb.position(offset + 4);
//        byte b = this.mbb.get();
//        if (b == 1) {
//            int countryOffset = this.readInt3();
//            this.mbb.position(countryOffset);
//            b = this.mbb.get();
//            if (b == 2) {
//                this.loc.country = this.readString(this.readInt3());
//                this.mbb.position(countryOffset + 4);
//            } else {
//                this.loc.country = this.readString(countryOffset);
//            }
//
//            this.loc.area = this.readArea(this.mbb.position());
//        } else if (b == 2) {
//            this.loc.country = this.readString(this.readInt3());
//            this.loc.area = this.readArea(offset + 8);
//        } else {
//            this.loc.country = this.readString(this.mbb.position() - 1);
//            this.loc.area = this.readArea(this.mbb.position());
//        }
//
//        return this.loc;
//    }
//
//    private String readArea(long offset) throws IOException {
//        this.ipFile.seek(offset);
//        byte b = this.ipFile.readByte();
//        if (b != 1 && b != 2) {
//            return this.readString(offset);
//        } else {
//            long areaOffset = this.readLong3(offset + 1L);
//            return areaOffset == 0L ? "未知地区" : this.readString(areaOffset);
//        }
//    }
//
//    private String readArea(int offset) {
//        this.mbb.position(offset);
//        byte b = this.mbb.get();
//        if (b != 1 && b != 2) {
//            return this.readString(offset);
//        } else {
//            int areaOffset = this.readInt3();
//            return areaOffset == 0 ? "未知地区" : this.readString(areaOffset);
//        }
//    }
//
//    private String readString(long offset) {
//        try {
//            this.ipFile.seek(offset);
//            int i = 0;
//
//            for(this.buf[i] = this.ipFile.readByte(); this.buf[i] != 0; this.buf[i] = this.ipFile.readByte()) {
//                ++i;
//            }
//
//            if (i != 0) {
//                return IPSeeker.IPSeekerUtils.getString(this.buf, 0, i, "GBK");
//            }
//        } catch (IOException var4) {
//            System.out.println(var4.getMessage());
//        }
//
//        return "";
//    }
//
//    private String readString(int offset) {
//        try {
//            this.mbb.position(offset);
//            int i = 0;
//
//            for(this.buf[i] = this.mbb.get(); this.buf[i] != 0; this.buf[i] = this.mbb.get()) {
//                ++i;
//            }
//
//            if (i != 0) {
//                return IPSeeker.IPSeekerUtils.getString(this.buf, 0, i, "GBK");
//            }
//        } catch (IllegalArgumentException var3) {
//            System.out.println(var3.getMessage());
//        }
//
//        return "";
//    }
//
//    public String getAddress(String ip) {
//        String country = this.getCountry(ip).equals(" CZ88.NET") ? "" : this.getCountry(ip);
//        String area = this.getArea(ip).equals(" CZ88.NET") ? "" : this.getArea(ip);
//        String address = country + " " + area;
//        return address.trim();
//    }
//
//    public List<String> getAllIp() {
//        List<String> list = new ArrayList();
//        byte[] buf = new byte[4];
//
//        for(long i = this.ipBegin; i < this.ipEnd; i += 7L) {
//            try {
//                this.readIP(this.readLong3(i + 4L), buf);
//                String ip = IPSeeker.IPSeekerUtils.getIpStringFromBytes(buf);
//                list.add(ip);
//            } catch (Exception var6) {
//            }
//        }
//
//        return list;
//    }
//
//    public static class IPSeekerUtils {
//        public IPSeekerUtils() {
//        }
//
//        public static byte[] getIpByteArrayFromString(String ip) {
//            byte[] ret = new byte[4];
//            StringTokenizer st = new StringTokenizer(ip, ".");
//
//            try {
//                ret[0] = (byte)(Integer.parseInt(st.nextToken()) & 255);
//                ret[1] = (byte)(Integer.parseInt(st.nextToken()) & 255);
//                ret[2] = (byte)(Integer.parseInt(st.nextToken()) & 255);
//                ret[3] = (byte)(Integer.parseInt(st.nextToken()) & 255);
//            } catch (Exception var4) {
//                System.out.println(var4.getMessage());
//            }
//
//            return ret;
//        }
//
//        public static String getString(String s, String srcEncoding, String destEncoding) {
//            try {
//                return new String(s.getBytes(srcEncoding), destEncoding);
//            } catch (UnsupportedEncodingException var4) {
//                return s;
//            }
//        }
//
//        public static String getString(byte[] b, String encoding) {
//            try {
//                return new String(b, encoding);
//            } catch (UnsupportedEncodingException var3) {
//                return new String(b);
//            }
//        }
//
//        public static String getString(byte[] b, int offset, int len, String encoding) {
//            try {
//                return new String(b, offset, len, encoding);
//            } catch (UnsupportedEncodingException var5) {
//                return new String(b, offset, len);
//            }
//        }
//
//        public static String getIpStringFromBytes(byte[] ip) {
//            StringBuffer sb = new StringBuffer();
//            sb.append(ip[0] & 255);
//            sb.append('.');
//            sb.append(ip[1] & 255);
//            sb.append('.');
//            sb.append(ip[2] & 255);
//            sb.append('.');
//            sb.append(ip[3] & 255);
//            return sb.toString();
//        }
//    }
//
//    public class IPEntry {
//        public String beginIp;
//        public String endIp;
//        public String country;
//        public String area;
//
//        public IPEntry() {
//            this.beginIp = this.endIp = this.country = this.area = "";
//        }
//
//        public String toString() {
//            return this.area + " " + this.country + "IP  Χ:" + this.beginIp + "-" + this.endIp;
//        }
//    }
//
//    public class IPLocation {
//        public String country;
//        public String area;
//
//        public IPLocation() {
//            this.country = this.area = "";
//        }
//
//        public IPSeeker.IPLocation getCopy() {
//            IPSeeker.IPLocation ret = IPSeeker.this.new IPLocation();
//            ret.country = this.country;
//            ret.area = this.area;
//            return ret;
//        }
//    }
//}
