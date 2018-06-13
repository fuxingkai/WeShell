package cn.jufuns.ws.common;

import android.text.TextUtils;

import cn.jufuns.ws.utils.PrefUtils;

/**
 * 本应用常驻缓存
 *
 * @author zch 2016-11-29
 */
public class PermanentCache {

    private static PermanentCache instance;

    private String appForceUpdateInfo;//应用版本强制升级信息
    private String appUpdatesubTitle;//应用版本更新内容

    private PermanentCache() {
    }

    public static PermanentCache getInstance() {
        if (null == instance) {
            synchronized (PermanentCache.class) {
                if (null == instance) {
                    instance = new PermanentCache();
                }
            }
        }
        return instance;
    }

    public String getAppForceUpdateInfo() {
        if (TextUtils.isEmpty(appForceUpdateInfo)) {
            appForceUpdateInfo = (String) PrefUtils.getInstance().getParam("appForceUpdateInfo", "");
        }
        return appForceUpdateInfo;
    }

    public void setAppForceUpdateInfo(String appForceUpdateInfo) {
        this.appForceUpdateInfo = appForceUpdateInfo;
        PrefUtils.getInstance().setParam("appForceUpdateInfo", appForceUpdateInfo);
    }

    /**
     * 获得升级应用的提示内容
     * @return
     */
    public String getAppUpdatesubTitle() {
        if (TextUtils.isEmpty(appUpdatesubTitle)) {
            appUpdatesubTitle = (String) PrefUtils.getInstance().getParam("appUpdatesubTitle", "");
        }
        return appUpdatesubTitle;
    }

    /**
     *设置升级应用的提示内容
     * @param appUpdatesubTitle
     */
    public void setAppUpdatesubTitle(String appUpdatesubTitle) {
        this.appUpdatesubTitle = appUpdatesubTitle;
        PrefUtils.getInstance().setParam("appUpdatesubTitle", appUpdatesubTitle);
    }
}

