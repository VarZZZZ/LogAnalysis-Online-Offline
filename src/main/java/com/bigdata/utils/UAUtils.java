package com.bigdata.utils;

import com.bigdata.domain.UserAgentInfo;
import cz.mallat.uasparser.OnlineUpdater;
import cz.mallat.uasparser.UASparser;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.security.User;

import java.io.IOException;

public class UAUtils {

    private static UASparser parser = null;
    static {
        try {
            parser = new UASparser(OnlineUpdater.getVendoredInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserAgentInfo getUserAgentInfo(String ua){
        UserAgentInfo info = null;
        if(StringUtils.isNotEmpty(ua)){
            try {
                cz.mallat.uasparser.UserAgentInfo tp = parser.parse(ua);
                if(null != tp){
                    info = new UserAgentInfo();
                    info.setBrowserName(tp.getUaFamily());
                    info.setBrowserVersion(tp.getBrowserVersionInfo());
                    info.setOsName(tp.getOsFamily());
                    info.setOsVersion(tp.getOsName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info;
    }



    public static void main(String[] args) throws IOException {
//        UASparser parser = new UASparser(OnlineUpdater.getVendoredInputStream());
//        cz.mallat.uasparser.UserAgentInfo info = parser.parse("Mozilla/4.0 (compatible; MSIE 7.0;Windows NT 5.1; )");
//        System.out.println(info);
        UserAgentInfo userAgentInfo = UAUtils.getUserAgentInfo("Mozilla/4.0 (compatible; MSIE 7.0;Windows NT 5.1; )");
        System.out.println(userAgentInfo);

    }
}
