package cn.jufuns.ws.common;

/**
 * 应用程序接口
 *
 * @author zch 2016-12-16
 */
public class AppApi {

    private static final String API_PLACEHOLDER = "/phoneintf";

    /**
     * 查询升级
     */
    public static String doQueryAppVersion() {
        return ConfigData.getDomain() + API_PLACEHOLDER + "/appInfo/queryAppVersion";
    }

}
