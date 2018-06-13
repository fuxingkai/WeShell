package cn.jufuns.ws.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetUtils {

    /**
     * 判断网络是否连接
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取返回状态码
     * @param urlStr
     * @return
     */
    public static int getResponseCode(String urlStr){
        int code=404;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(2000);
            con.setConnectTimeout(2000);
            int responseCode=con.getResponseCode();
            code=con.getResponseCode();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
//			e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
//			e.printStackTrace();
        }
        return code;
    }
}
