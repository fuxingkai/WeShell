package cn.jufuns.ws.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.jufuns.ws.common.AppApi;
import cn.jufuns.ws.common.PermanentCache;
import cn.jufuns.ws.data.res.UpgradeRes;
import cn.jufuns.ws.upgrade.ForceUpgradeDialog;
import cn.jufuns.ws.upgrade.NormalUpgradeDialog;
import cn.jufuns.ws.utils.async.Async;

/**
 * 查询升级工具类
 *
 * @author zch 2016-12-5
 */
public class UpgradeUtils {

    private static String mResult;//检查更新返回的结果字符串

    /**
     * make sure call on UI thread
     *
     * @param context
     */
    public static void checkUpgrade(final Context context) {
        mResult = null;
        if (context == null) {
            LogUtils.e("context is null");
            return;
        }

        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("call this method in UI thread instead");
        }
        Async.run(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;

                try {
                    int versionCode = -1;
                    try {
                        versionCode = context.getPackageManager()
                                .getPackageInfo(context.getPackageName(), 0).versionCode;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (versionCode == -1) {
                        LogUtils.e("version code is invalid, check upgrade failed");
                        return;
                    }

                    final int finalVersionCode = versionCode;

                    URL url = new URL(AppApi.doQueryAppVersion());
                    //获得连接对象
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //设置属性
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    //设置输入流和输出流,都设置为true
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Connection", "keep-alive"); //http1.1

                    //封装要提交的数据
                    JSONObject json = new JSONObject();
                    decorateBizJSONObject(json);
                    JSONObject jsonRequestData = new JSONObject();
                    try {
                        jsonRequestData.put("appNo", context.getPackageName());
                        jsonRequestData.put("versionNo", String.valueOf(versionCode));

                        json.put("request_data", jsonRequestData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //把提交的数据以输出流的形式提交到服务器
                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes());
                    os.close();

                    //通过响应码来判断是否连接成功
                    if (conn.getResponseCode() == 200) {
                        //获得服务器返回的字节流
                        inputStream = conn.getInputStream();

                        //内存输出流,适合数据量比较小的字符串 和 图片
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int len = 0;
                        while ((len = inputStream.read(buf)) != -1) {
                            baos.write(buf, 0, len);
                        }
                        //可使用 toByteArray() 和 toString() 获取数据。
                        mResult = new String(baos.toByteArray());
                    } else {
                        checkLocalForceUpgrade(context, finalVersionCode);
                        return;
                    }
                    Async.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (TextUtils.isEmpty(mResult)) {
                                checkLocalForceUpgrade(context, finalVersionCode);
                                return;
                            }
                            String response_data = JsonUtils.getValue(mResult, "response_data");
                            if (TextUtils.isEmpty(response_data)) {
                                checkLocalForceUpgrade(context, finalVersionCode);
                                return;
                            }
                            UpgradeRes upgradeRes = JsonUtils.fromJson(response_data, UpgradeRes.class);
                            if(null == upgradeRes){
                                LogUtils.e("upgradeRes is null");
                                return;
                            }
                            if (TextUtils.isEmpty(upgradeRes.downUrl)) {
                                LogUtils.e("download url is null");
                                return;
                            }
                            int serverVersion = -1;
                            try {
                                serverVersion = Integer.parseInt(upgradeRes.versionNo);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            if (serverVersion == -1) {
                                LogUtils.e("parse version code from server failed");
                                checkLocalForceUpgrade(context, finalVersionCode);
                                return;
                            } else if (serverVersion <= finalVersionCode) {
                                LogUtils.i("ignore upgrade as current version code is greater or equals than the one server defined");
                                return;
                            }
                            String subTitle = upgradeRes.versionName+"\n"+upgradeRes.versionNote;//版本升级副标题拼接
                            if ("1".equals(upgradeRes.needUpdate)) {
                                PermanentCache.getInstance().setAppForceUpdateInfo(upgradeRes.versionNo + "@" + upgradeRes.downUrl);
                                PermanentCache.getInstance().setAppUpdatesubTitle(subTitle);//版本升级副标题保存
                                showUpgradeDialog(context, true, upgradeRes.downUrl,subTitle);
                            } else if ("1".equals(upgradeRes.isNew)) {
                                showUpgradeDialog(context, false, upgradeRes.downUrl,subTitle);
                            } else {
                                LogUtils.d("no need to upgrade");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != inputStream) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /* package */
    static void showUpgradeDialog(Context context, boolean forceUpgrade, String downloadUrl,String subTitle) {
//        if (forceUpgrade) {
//            ForceUpgradeDialog dialog = new ForceUpgradeDialog(context);
//            dialog.setDownloadUrl(downloadUrl);
//            dialog.show();
//        } else {
//            NormalUpgradeDialog dialog = new NormalUpgradeDialog(context);
//            dialog.setDownloadUrl(downloadUrl);
//            dialog.show();
//        }
        //版本升级提示更新
        if (forceUpgrade) {
            ForceUpgradeDialog dialog = new ForceUpgradeDialog(context);
            dialog.setDownloadUrl(downloadUrl);
            dialog.setSubTitleContent(subTitle);
            dialog.show();
        } else {
            NormalUpgradeDialog dialog = new NormalUpgradeDialog(context);
            dialog.setDownloadUrl(downloadUrl);
            dialog.setSubTitleContent(subTitle);
            dialog.show();
        }
    }

    /* package */
    static void checkLocalForceUpgrade(Context context, int curVersionCode) {
        //local save force upgrade info --> "10@http://xxx"
        String forceUpgradeInfo = PermanentCache.getInstance().getAppForceUpdateInfo();
        String subTitle = PermanentCache.getInstance().getAppUpdatesubTitle();//版本升级提示副标题

        if (TextUtils.isEmpty(forceUpgradeInfo)) {
            return;
        }

        String[] info = forceUpgradeInfo.split("@", 2);
        if (info == null || info.length != 2) {
            return;
        }

        String forceVersion = info[0];
        String downloadUrl = info[1];

        if (TextUtils.isEmpty(forceVersion) || TextUtils.isEmpty(downloadUrl)) {
            return;
        }

        //should we check the download url???

        int forceVersionCode = -1;
        try {
            forceVersionCode = Integer.parseInt(forceVersion);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (forceVersionCode == -1) {
            return;
        }

        if (forceVersionCode > curVersionCode) {
            showUpgradeDialog(context, true, downloadUrl,subTitle);
        }
    }

    public static void decorateBizJSONObject(JSONObject json) {
        if (json == null) {
            LogUtils.e("json object is null");
            return;
        }

        try {
            json.put("serial_number", "20160124201122499phone8888888");
            json.put("service_name", "equ.getnode");
            json.put("service_ver", "v1.0");
            json.put("sign", "lkjerwsfdsdfsf");
            json.put("equid", "equid");
            json.put("equtype", "2");
            json.put("timestamp", String.valueOf(System.currentTimeMillis()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
