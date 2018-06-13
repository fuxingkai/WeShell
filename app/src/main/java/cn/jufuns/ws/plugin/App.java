package cn.jufuns.ws.plugin;

import android.content.Intent;

import org.json.JSONObject;

import cn.jufuns.ws.activity.ScanningActivity;
import cn.jufuns.ws.common.Constant;

public class App extends Plugin {

    @Override
    public PluginResult exec(String action, JSONObject args)
            throws ActionNotFoundException {
        if ("sao".equals(action)) {//扫二维码
            return sao();
        } else {
            throw new ActionNotFoundException("App", action);
        }
    }

    /**
     * 扫二维码
     */
    private PluginResult sao() {
        Intent intent = new Intent(context, ScanningActivity.class);
        context.startActivityForResult(intent, Constant.REQUEST_CODE_SAO);
        return PluginResult.newEmptyPluginResult();
    }
}
