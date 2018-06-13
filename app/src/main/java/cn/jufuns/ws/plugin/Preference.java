package cn.jufuns.ws.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Date;

import cn.jufuns.ws.common.Constant;
import cn.jufuns.ws.utils.DateUtils;

public class Preference extends Plugin {

    private static final String LOGIN_USER_KEY = "m3user";

    @Override
    public PluginResult exec(String action, JSONObject args)
            throws ActionNotFoundException {
        if ("put".equals(action)) {
            return put(args);
        } else if ("get".equals(action)) {
            return get(args);
        } else {
            throw new ActionNotFoundException("Preference", action);
        }
    }

    private PluginResult put(JSONObject args) {
        try {
            String key = args.optString("key");
            String value = args.optString("value");
            String prefname = args.optString("prefname");
            String prefFileName = Constant.APP_NAME;
            if (!TextUtils.isEmpty(prefname)) {
                prefFileName = prefFileName + "_" + prefname;
            }
            SharedPreferences sp = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
            Editor edit = sp.edit();

            edit.putString(key, value);
            //如果是登录页面，重置最新一次保存密码的日期
            if (LOGIN_USER_KEY.equals(key)) {
                edit.putString("lastDate", DateUtils.date2Str(new Date()));
            }
            edit.commit();
            return new PluginResult("ok");
        } catch (Exception e) {
            e.printStackTrace();
            return new PluginResult(e.getMessage(), PluginResult.Status.ERROR);
        }
    }

    private PluginResult get(JSONObject args) {
        try {
            String key = args.optString("key");
            String defValue = args.optString("defValue");
            String outDay = args.optString("outDay");

            String prefname = args.optString("prefname");
            String prefFileName = Constant.APP_NAME;
            if (!TextUtils.isEmpty(prefname)) {
                prefFileName = prefFileName + "_" + prefname;
            }
            SharedPreferences sp = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
            //如果是登录页面，重置最新一次保存密码的日期
            if (LOGIN_USER_KEY.equals(key)) {
                int dayValid = Integer.valueOf(outDay);//过期时间
                String lastDate = sp.getString("lastDate", "");
                if (TextUtils.isEmpty(lastDate)) {
                    return new PluginResult("");
                } else if (DateUtils.getDaysBetween(lastDate, DateUtils.date2Str(new Date())) >= dayValid) {
                    return new PluginResult("");
                }
            }
            String value = sp.getString(key, defValue);
            return new PluginResult(value);
        } catch (Exception e) {
            e.printStackTrace();
            return new PluginResult(e.getMessage(), PluginResult.Status.ERROR);
        }
    }

}
