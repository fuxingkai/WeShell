package cn.jufuns.ws.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import cn.jufuns.ws.GlobalApp;

/**
 * 信息存储
 *
 * @author zhich 2016-11-29
 */
public class PrefUtils {

    private static final String APP_NAME = "WebShell_SharedPreferences";

    private static PrefUtils instance;

    private PrefUtils() {
    }

    public static PrefUtils getInstance() {
        if (null == instance) {
            synchronized (PrefUtils.class) {
                instance = new PrefUtils();
            }
        }
        return instance;
    }


    /**
     * 保存数据
     *
     * @param key：保存数据的键
     * @param objectValue：保存的数据
     */
    public void setParam(String key, Object objectValue) {
        String type = objectValue.getClass().getSimpleName();
        SharedPreferences preferences = GlobalApp.getInstance().getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        if ("String".equals(type)) {
            editor.putString(key, (String) objectValue);
        } else if ("Integer".equals(type)) {
            editor.putInt(key, (Integer) objectValue);
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, (Boolean) objectValue);
        } else if ("Long".equals(type)) {
            editor.putLong(key, (Long) objectValue);
        }
        editor.commit();
    }

    /**
     * 根据键获取值
     *
     * @param key：获取数据的键
     * @param defaultValue：默认返回值
     * @return
     */
    public Object getParam(String key, Object defaultValue) {
        String type = defaultValue.getClass().getSimpleName();
        SharedPreferences preferences = GlobalApp.getInstance().getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        if ("String".equals(type)) {
            return preferences.getString(key, (String) defaultValue);
        } else if ("Integer".equals(type)) {
            return preferences.getInt(key, (Integer) defaultValue);
        } else if ("Boolean".equals(type)) {
            return preferences.getBoolean(key, (Boolean) defaultValue);
        } else if ("Long".equals(type)) {
            return preferences.getLong(key, (Long) defaultValue);
        }
        return null;
    }

}