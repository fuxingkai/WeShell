package cn.jufuns.ws.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by zhangxg on 2016/8/11.
 */
public abstract class  AbstractDialog {

    private Context mContext;

    private WindowManager mWindowMgr;
    private WindowManager.LayoutParams mLpContent;

    private View mContentView;

    public AbstractDialog(Context context) {
        mContext = context;
        if (mContext == null) {
            throw new IllegalArgumentException("context is null");
        }

        mWindowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        mLpContent = new WindowManager.LayoutParams();
        mLpContent.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        mLpContent.format = PixelFormat.TRANSLUCENT;
        mLpContent.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mLpContent.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLpContent.height = WindowManager.LayoutParams.MATCH_PARENT;

        mContentView = createContentView(mContext);
        if (mContentView == null) {
            throw new RuntimeException("createContentView return null");
        }
    }

    protected final Context getContext() {
        return mContext;
    }

    protected final View getContentView() {
        return mContentView;
    }

    /**
     * make sure call on ui thread
     */
    public final void show() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("must call on ui thread");
        }

        mLpContent.token = null;

        onShow();

        try {
            mWindowMgr.addView(mContentView, mLpContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * make sure call on ui thread
     */
    public final void hide() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("must call on ui thread");
        }

        if (mContentView == null) {
            return;
        }

        onHide();

        try {
            mWindowMgr.removeView(mContentView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                mContext.getResources().getDisplayMetrics());
    }

    /**
     * called when the dialog is ready to show on window
     */
    protected void onShow() {}

    /**
     * called when the dialog is ready to hide
     */
    protected void onHide() {}

    /**
     * create the content view that will add to the window, the added view will
     * on the top on window
     * @param context
     * @return
     */
    protected abstract View createContentView(Context context);
}
