package cn.jufuns.ws.common;

import cn.jufuns.ws.GlobalApp;
import cn.jufuns.ws.utils.AppUtils;

/**
 * 配置信息
 *
 * @author zch 2016-12-16
 */

public class ConfigData {

    private static String sDomain;

    public static String getDomain() {
        if (null == sDomain) {
            sDomain = "http://" + AppUtils.getMetaValue(GlobalApp.getInstance(), "DOMAIN");
        }
        return sDomain;
    }
}
