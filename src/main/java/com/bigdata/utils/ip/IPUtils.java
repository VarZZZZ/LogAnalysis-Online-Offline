package com.bigdata.utils.ip;

/**
 * @Author: ly
 * @Date: 2020/6/29 22:08
 * @Version 1.0
 */


import java.util.Properties;

public class IPUtils extends IPSeeker {
    private static String ipFilePath = "";
    private static Properties props = new Properties();
    private static IPUtils obj;

    protected IPUtils(String ipFilePath) {
        super(ipFilePath);
    }

    public static IPUtils getInstance() {
        return obj;
    }

    public IPUtils.RegionInfo analyseIp(String ip) {
        if (ip != null && !"".equals(ip.trim())) {
            IPUtils.RegionInfo info = new IPUtils.RegionInfo();

            try {
                String country = super.getCountry(ip);
                if (!"局域网".equals(country) && country != null && !country.isEmpty() && !country.trim().startsWith("CZ88")) {
                    int length = country.length();
                    int index = country.indexOf(30465);
                    if (index > 0) {
                        info.setCountry("中国");
                        info.setProvince(country.substring(0, Math.min(index + 1, length)));
                        int index2 = country.indexOf(24066, index);
                        if (index2 > 0) {
                            info.setCity(country.substring(index + 1, Math.min(index2 + 1, length)));
                        }
                    } else {
                        String flag = country.substring(0, 2);
                        byte var8 = -1;
                        switch(flag.hashCode()) {
                            case 647341:
                                if (flag.equals("上海")) {
                                    var8 = 5;
                                }
                                break;
                            case 679541:
                                if (flag.equals("北京")) {
                                    var8 = 6;
                                }
                                break;
                            case 680884:
                                if (flag.equals("内蒙")) {
                                    var8 = 0;
                                }
                                break;
                            case 735516:
                                if (flag.equals("天津")) {
                                    var8 = 8;
                                }
                                break;
                            case 748974:
                                if (flag.equals("宁夏")) {
                                    var8 = 3;
                                }
                                break;
                            case 785120:
                                if (flag.equals("广西")) {
                                    var8 = 1;
                                }
                                break;
                            case 837078:
                                if (flag.equals("新疆")) {
                                    var8 = 4;
                                }
                                break;
                            case 924821:
                                if (flag.equals("澳门")) {
                                    var8 = 10;
                                }
                                break;
                            case 1125424:
                                if (flag.equals("西藏")) {
                                    var8 = 2;
                                }
                                break;
                            case 1181273:
                                if (flag.equals("重庆")) {
                                    var8 = 7;
                                }
                                break;
                            case 1247158:
                                if (flag.equals("香港")) {
                                    var8 = 9;
                                }
                        }

                        switch(var8) {
                            case 0:
                                info.setCountry("中国");
                                info.setProvince("内蒙古自治区");
                                country = country.substring(3);
                                if (country != null && !country.isEmpty()) {
                                    index = country.indexOf(24066);
                                    if (index > 0) {
                                        info.setCity(country.substring(0, Math.min(index + 1, length)));
                                    }
                                }
                                break;
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                                info.setCountry("中国");
                                info.setProvince(flag);
                                country = country.substring(2);
                                if (country != null && !country.isEmpty()) {
                                    index = country.indexOf(24066);
                                    if (index > 0) {
                                        info.setCity(country.substring(0, Math.min(index + 1, length)));
                                    }
                                }
                                break;
                            case 5:
                            case 6:
                            case 7:
                            case 8:
                                info.setCountry("中国");
                                info.setProvince(flag + "市");
                                country = country.substring(3);
                                if (country != null && !country.isEmpty()) {
                                    index = country.indexOf(21306);
                                    if (index > 0) {
                                        char ch = country.charAt(index - 1);
                                        if (ch != 23567 || ch != 26657) {
                                            info.setCity(country.substring(0, Math.min(index + 1, length)));
                                        }
                                    }

                                    if ("unknown".equals(info.getCity())) {
                                        index = country.indexOf(21439);
                                        if (index > 0) {
                                            info.setCity(country.substring(0, Math.min(index + 1, length)));
                                        }
                                    }
                                }
                                break;
                            case 9:
                            case 10:
                                info.setCountry("中国");
                                info.setProvince(flag + "特别行政区");
                                break;
                            default:
                                info.setCountry(country);
                        }
                    }
                } else {
                    info.setCountry("中国");
                    info.setProvince("上海市");
                }
            } catch (Exception var10) {
            }

            return info;
        } else {
            return null;
        }
    }

    static {
        try {
            props.load(IPUtils.class.getClassLoader().getResourceAsStream("params.properties"));
            if (Boolean.parseBoolean(props.getProperty("run.local"))) {
                ipFilePath = props.getProperty("ip.file.local");
            } else {
                ipFilePath = props.getProperty("ip.file.server");
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }

        obj = new IPUtils(ipFilePath);
    }

    public static class RegionInfo {
        private String country = "unknown";
        private String province = "unknown";
        private String city = "unknown";

        public RegionInfo() {
        }

        public String getCountry() {
            return this.country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return this.province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return this.city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String toString() {
            return "RegionInfo [country=" + this.country + ", province=" + this.province + ", city=" + this.city + "]";
        }
    }
}
