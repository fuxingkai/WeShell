package cn.jufuns.ws.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cn.jufuns.ws.R;
import cn.jufuns.ws.common.Constant;
import cn.jufuns.ws.plugin.IPlugin;
import cn.jufuns.ws.plugin.PluginManager;
import cn.jufuns.ws.plugin.PluginNotFoundException;
import cn.jufuns.ws.plugin.PluginResult;
import cn.jufuns.ws.ui.LoadingDialog;
import cn.jufuns.ws.utils.AppUtils;
import cn.jufuns.ws.utils.LogUtils;
import cn.jufuns.ws.utils.NetUtils;
import cn.jufuns.ws.utils.UpgradeUtils;

import static cn.jufuns.ws.R.id.webView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private Context mContext;

    private WebView mWebView;
    private boolean mIsShowLoading = true;
    private boolean mIsRequestedUpgrade = false;//是否正在请求检测版本更新

    protected PluginManager mPluginManager;

    private Dialog loadingDialog;

    @Override
    protected void onStart() {
        mPluginManager.onStart();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        mPluginManager.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        mPluginManager.onResume();
        super.onResume();
//        loadUrl();
    }

    @Override
    public void onPause() {
        mPluginManager.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPluginManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mPluginManager.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_SAO && resultCode == RESULT_OK) {
            String resultString = data.getStringExtra("resultString");
            //LogUtils.e("resultString-->" + resultString);//获取到二维码的扫码信息。例如可能是网址http://www.xiaomi.com?wd=1
            mWebView.loadUrl("javascript:setInfo('" + resultString + "')");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate()");
        // 去掉Activity上面的标题栏、状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mContext = this;

        mPluginManager = new PluginManager(this);
        mPluginManager.loadPlugin();
        mPluginManager.onCreate(savedInstanceState);

        // 提醒修改IP和端口
        // Toast.makeText(MainActivity.this, "双击图片修改IP和端口", 2000).show();

        mWebView = (WebView) findViewById(webView);
        registerForContextMenu(mWebView);// 注册上下文菜单

        WebSettings webSettings = mWebView.getSettings();
        // 启用javascript
        webSettings.setJavaScriptEnabled(true);
        //启用地理定位
        webSettings.setGeolocationEnabled(true);
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        //设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(dir);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(PluginState.ON);

        webSettings.setSupportZoom(true);// 支持放大缩小
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // 去除登录时弹出保存密码的提示
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        // set.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        //add by zch
        //开启WebView的缓存功能可以减少对服务器资源的请求，一般使用默认缓存策略就可以了。
        //设置 缓存模式
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebServerChromeClient());

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);

        // 添加js交互接口类，并起别名 jsInterface
        mWebView.addJavascriptInterface(new JavascriptInterface(this), "jsInterface");

        mWebView.setWebViewClient(new MyWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("sms:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Referer", view.getUrl());

                view.loadUrl(url, map);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
            }


        });

        mWebView.setOnKeyListener(new View.OnKeyListener() { // webview can
            // go back
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack();
                        } else {
                            finish();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        loadDefaultUrl();//loadDefaultUrl()修改后的位置
        //loadUrl("zch.html");

        mWebView.requestFocus();

        checkUpgrade();
    }

    private void loadUrl(String entryUrl) {
        mWebView.loadUrl("file:///android_asset" + File.separator + entryUrl);
    }

    // js通信接口
    public class JavascriptInterface {

        private Context context;

        public JavascriptInterface(Context context) {
            this.context = context;
        }

        //拨打电话（在Android客户端用到）
        @android.webkit.JavascriptInterface
        public void exitSys(String number) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //发短信
        @android.webkit.JavascriptInterface
        public void sendSMS(String number) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //MyWebViewClient 监听
    private class MyWebViewClient extends WebViewClient {

        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
            if (url.endsWith("jquery.min.js") || url.endsWith("bootstrap.js") || url.endsWith("respond.min.js") || url.endsWith("tools.js") || url.endsWith("Validform_v5.3.2_min.js")) {
                return getWebResourceResponseFromAsset("js", url);
            } else if (url.endsWith("bootstrap.css") || url.endsWith("style.css") || url.endsWith("tools.css")) {
                return getWebResourceResponseFromAsset("css", url);
            } else if (url.endsWith("button-color_03.png") || url.endsWith("login_icon01.png") || url.endsWith("login_icon02.png") || url.endsWith("login_img.png")) {
                return getWebResourceResponseFromAsset("img", url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mIsShowLoading) {
                closeProgressDialog();
                mIsShowLoading = false;
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (mIsShowLoading) {
                showProgressDialog();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


    }

    /**
     * 从本地assets文件夹获取资源
     *
     * @param resType：资源类型。css、js、img等等
     * @param url
     */
    private WebResourceResponse getWebResourceResponseFromAsset(String resType, String url) {
        WebResourceResponse response = null;
        try {
            AssetManager am = getResources().getAssets();
            //资源路径，如bootstrap.css、jquery.min.js
            //http://saas.jufuns.cn/crm/html5/js/jquery.min.js
            String resPath = url.substring(url.lastIndexOf("/") + 1, url.length());
            if ("css".equals(resType)) {
                resPath = "css/" + resPath;
                response = new WebResourceResponse("text/css",
                        "utf-8", am.open(resPath));
            } else if ("js".equals(resType)) {
                resPath = "js/" + resPath;
                response = new WebResourceResponse("text/javascript",
                        "utf-8", am.open(resPath));
            } else if ("img".equals(resType)) {
                resPath = "img/" + resPath;
                response = new WebResourceResponse("image/png",
                        "utf-8", am.open(resPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    // 第一次使用时加载默认配置
    public void loadDefaultUrl() {
        loadUrl();
    }

    //加载网页
    public void loadUrl(){
        //如果内网可以访问到，优先访问内网
        /*if(NetUtils.getResponseCode(AppUtils.getMetaValue(this,
                "INTERNAL_URL"))==200){
            mWebView.loadUrl(AppUtils.getMetaValue(this, "INTERNAL_URL") + "&os=Android");
        }else {
            if(NetUtils.isNetworkConnected(mContext)){
                mWebView.loadUrl(AppUtils.getMetaValue(this, "URL") + "&os=Android");
            }else {
                Toast.makeText(mContext,"请连接网络",Toast.LENGTH_LONG).show();
            }
        }*/
        mWebView.loadUrl(AppUtils.getMetaValue(this, "URL") + "&os=Android");
    }

//    private ProgressDialog mDialog;
//    private Dialog loadingDialog;

    private void showProgressDialog() {
        /*if (mDialog == null) {
            mDialog = new ProgressDialog(mContext);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
            mDialog.setMessage("正在加载 ，请等待...");
            mDialog.setIndeterminate(false);//设置进度条是否为不明确
            mDialog.setCancelable(true);//设置进度条是否可以按退回键取消
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    mDialog = null;
                }
            });
            mDialog.show();
        }*/
        /*loadingDialog = LoadingDialog.createLoadingDialog(mContext);
        loadingDialog.show();*/
     }

    private void closeProgressDialog() {
        /*if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }*/
        if(loadingDialog != null){
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    /**
     * 检测版本更新
     */
    private void checkUpgrade() {
        if (!mIsRequestedUpgrade) {
            mIsRequestedUpgrade = true;
            mWebView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UpgradeUtils.checkUpgrade(mContext);
                }
            }, 1000L);
        }
    }

    private class WebServerChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // mProgress.setProgress(newProgress);
            Log.i(TAG,"onProgressChanged()");
            Log.i(TAG,"newProgress-->"+newProgress);
            if (newProgress == 100){
                if(loadingDialog!=null){
                    loadingDialog.dismiss();
                    loadingDialog=null;
                }
            }
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {

            LogUtils.d("sys defaultValue:" + defaultValue);
            LogUtils.e("onJsPrompt ----------" + "url:" + url + "message:"
                    + message + "defaultValue:" + defaultValue + "result:"
                    + result + "----------");
            JSONObject args = null;
            JSONObject head = null;
            try {
                head = new JSONObject(message);
                if (defaultValue != null && !defaultValue.equals("")
                        && !defaultValue.equals("null")) {
                    try {
                        args = new JSONObject(defaultValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String execResult = mPluginManager.exec(
                        head.getString(IPlugin.SERVICE),
                        head.getString(IPlugin.ACTION), args);
                LogUtils.e("onJsPrompt execResult:" + execResult);
                result.confirm(execResult);
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
                result.confirm(PluginResult.getErrorJSON(e));
                return true;
            } catch (PluginNotFoundException e) {
                e.printStackTrace();
                result.confirm(PluginResult.getErrorJSON(e));
                return true;
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }
}