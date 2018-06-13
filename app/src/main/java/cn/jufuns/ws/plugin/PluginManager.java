package cn.jufuns.ws.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;

public class PluginManager {
    static final String TAG = PluginManager.class.getSimpleName();

    private WeakReference<PluginManager> instance;

    private Activity context;
    private HashMap<String, String> configs = new HashMap<String, String>();
    private HashMap<String, IPlugin> plugins = new HashMap<String, IPlugin>();

    public PluginManager(Activity context) {
        this.context = context;
        instance = new WeakReference<PluginManager>(this);
    }

    public PluginManager getInstance() {
        return instance.get();
    }

    /**
     * 执行html5请求
     *
     * @param service
     * @param action
     * @param args
     * @return JSON 字符串
     * @throws PluginNotFoundException
     */
    public String exec(String service, String action, JSONObject args)
            throws PluginNotFoundException {
        IPlugin plugin = getPlugin(service);
        try {
            PluginResult result = plugin.exec(action, args);
            if(result==null) return null;
            return result.getJSONString();
        } catch (Exception e) {
            e.printStackTrace();
            return PluginResult.getErrorJSON(e);

        } catch (ActionNotFoundException e) {
            e.printStackTrace();
            return PluginResult.getErrorJSON(e);
        }
    }

    /**
     * 执行html5请求
     *
     * @param service
     * @param action
     * @param args
     * @return JSON 字符串
     * @throws PluginNotFoundException
     */
    public String execAsyn(String service, String action, JSONObject args, String requestID)
            throws PluginNotFoundException {
        IPlugin plugin = getPlugin(service);
        try {
            PluginResult result = plugin.execAsyn(action, args ,requestID);
            if(result==null) return null;
            return result.getJSONString();
        } catch (Exception e) {
            e.printStackTrace();
            return PluginResult.getErrorJSON(e);

        } catch (ActionNotFoundException e) {
            e.printStackTrace();
            return PluginResult.getErrorJSON(e);
        }
    }


    /**
     * 加载plugins.xml
     */
    public void loadPlugin() {
        int identifier = context.getResources().getIdentifier("extends_plugins", "xml",
                context.getPackageName());
        if (identifier == 0) {


            identifier = context.getResources().getIdentifier("plugins", "xml",
                    context.getPackageName());
        }

        if (identifier == 0) {
            pluginConfigurationMissing();
        }

        XmlResourceParser xml = context.getResources().getXml(identifier);
        try {
            int eventType = -1;
            while ((eventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String name = xml.getName();
                    if ("plugin".equals(name)) {
                        String pluginName = xml.getAttributeValue(null, "name");
                        String className = xml.getAttributeValue(null, "class");
                        configs.put(pluginName, className);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取插件
     *
     * @param pluginName
     * @return
     * @throws PluginNotFoundException
     */
    public IPlugin getPlugin(String pluginName) throws PluginNotFoundException {
        String className = configs.get(pluginName);
        if (className == null) {
            throw new PluginNotFoundException(pluginName);
        }
        if (plugins.containsKey(className)) {
            return plugins.get(className);
        } else {
            return addPlugin(className);
        }
    }


    /**
     * 添加插件到插件容器中
     *
     * @param className
     * @return
     */
    @SuppressWarnings("rawtypes")
    public IPlugin addPlugin(String className) {
        Log.d(TAG, "className:" + className);
        IPlugin plugin = null;
        try {
            Class c = getClassByName(className);

            //if (isWebServerPluginClass(c)) {
            plugin = (IPlugin) c.newInstance();
            plugin.setContext(context);
            plugins.put(className, plugin);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plugin;
    }


    @SuppressWarnings("rawtypes")
    private Class getClassByName(String className)
            throws ClassNotFoundException {
        if (className == null)
            return null;
        return Class.forName(className);
    }

    private void pluginConfigurationMissing() {
        System.err
                .println("=====================================================================================");
        System.err
                .println("ERROR: plugins.xml is missing.  Add res/xml/plugins.xml to your project.");
        System.err
                .println("=====================================================================================");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onCreate(savedInstanceState);
        }
    }

    public void onDestroy() {
        context = null;
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onDestroy();
        }
    }

    public void onPause() {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onPause();
        }
    }

    public void onRestart() {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onRestart();
        }
    }

    public void onResume() {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onResume();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onSaveInstanceState(outState);
        }
    }

    public void onStart() {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onStart();
        }
    }

    public void onStop() {
        Collection<IPlugin> iPlugins = plugins.values();
        for (IPlugin iPlugin : iPlugins) {
            iPlugin.onStop();
        }
    }
}
