package cn.jufuns.ws.data.res;

import java.io.Serializable;

/**
 * 查询升级响应信息类
 *
 * @author zch 2016-12-19
 */
public class UpgradeRes implements Serializable {

    public static final long serialVersionUID = 1L;

    public String appNo;//App标示号，即包名
    public String versionNo;//版本号
    public String downUrl;//下载地址
    public String isNew;//是否有新版本，0：否，1：是
    public String needUpdate;//是否强制更新，0：否，1：是
    public String versionNote;//版本描述
    public String versionName;//版本名称
}
