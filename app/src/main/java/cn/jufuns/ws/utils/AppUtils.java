package cn.jufuns.ws.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by lp on 2016/9/7.
 */
public class AppUtils {

    /**
     * Get AndroidManifest's meta element by metaKey
     *
     * @param context
     * @param metaKey
     * @return
     */
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return apiKey;
    }

    /**
     * Get AndroidManifest's meta element by metaKey
     *
     * @param context
     * @param metaKey
     * @return
     */
    public static int getIntMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        int apiKey = 0;
        if (context == null || metaKey == null) {
            return 80;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getInt(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 80;
        }
        Log.d("dd",""+apiKey);
        return apiKey;
    }

}
