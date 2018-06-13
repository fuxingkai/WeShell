package cn.jufuns.ws.upgrade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

import cn.jufuns.ws.downloader.bizs.DLInfo;
import cn.jufuns.ws.downloader.bizs.DLManager;
import cn.jufuns.ws.downloader.interfaces.IDListener;


/**
 * 更新弹出框抽象类
 * Created by zhangxg on 2016/8/11.
 */
/* package */ abstract class AbstractUpgradeDialog extends QuestionAlertDialog {

    public static final int EXTERNAL_STORAGE_NOT_AVAILABLE = -999;
    public static final int EXTERNAL_STORAGE_SPACE_NOT_ENOUGH = -998;

    private static final long DOWNLOAD_MIN_FREE_SPACE = 100 * 1024 * 1024;

    private String mDownloadUrl;

    protected TextView mTvUpgradeContent;
    protected TextView mTvUpgradeTitle;//版本更新标题
    protected TextView mTvUpgradeSubTitle;//版本更新副标题
    protected View mViewUpgradeTitleDivider;//版本更新标题分割线
    protected ProgressBar mPbDownload;
    protected LinearLayout mlayoutTitle;//标题布局

    public AbstractUpgradeDialog(Context context) {
        super(context);
        initDownloadDialogUI(context);
    }

    /**
     * 初始化下载UI
     * @param context
     */
    private void initDownloadDialogUI(Context context) {
        LinearLayout layoutContent = new LinearLayout(context);
        layoutContent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout layoutTitle = new LinearLayout(context);
        mlayoutTitle = layoutTitle;
        layoutTitle.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lplayoutTitle = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutContent.addView(mlayoutTitle,lplayoutTitle);

        //版本更新标题
        TextView tvUpgradeTitle = new TextView(context);
        mTvUpgradeTitle = tvUpgradeTitle;
        tvUpgradeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvUpgradeTitle.setTextColor(Color.parseColor("#FF5400"));
        tvUpgradeTitle.setSingleLine(true);
        tvUpgradeTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvUpgradeTitle.setText(UpgradeText.TEXT_TITLE_UPGRADE);
        tvUpgradeTitle.setGravity(Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams lpTvTitle = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int titleMargin = dp2px(20);
        lpTvTitle.setMargins(titleMargin, titleMargin, titleMargin, titleMargin);
        layoutTitle.addView(tvUpgradeTitle, lpTvTitle);

        //版本更新标题分割线
        View viewUpgradeTitleDivider = new View(context);
        viewUpgradeTitleDivider.setBackgroundColor(Color.parseColor("#e6e6e6"));
        layoutTitle.addView(viewUpgradeTitleDivider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        mViewUpgradeTitleDivider = viewUpgradeTitleDivider;

        //版本更新副标题
        TextView tvUpgradeSubTitle = new TextView(context);
        mTvUpgradeSubTitle = tvUpgradeSubTitle;
        tvUpgradeSubTitle.setLineSpacing(5f, 1.2f);
        tvUpgradeSubTitle.setMovementMethod(ScrollingMovementMethod.getInstance());//设置可滑动
        tvUpgradeSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17.45f);
        tvUpgradeSubTitle.setTextColor(Color.parseColor("#333333"));
        tvUpgradeSubTitle.setGravity(Gravity.LEFT);
        LinearLayout.LayoutParams lpTvUpgradeSubTitle= new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.WRAP_CONTENT);
        tvUpgradeSubTitle.setMaxHeight(dp2px(250));//设置最大高度
        int subTitleMargin = dp2px(15);
        lpTvUpgradeSubTitle.setMargins(subTitleMargin, subTitleMargin, subTitleMargin, subTitleMargin);
        layoutTitle.addView(tvUpgradeSubTitle, lpTvUpgradeSubTitle);

        //content text view
        TextView tvUpgradeContent = new TextView(context);
        mTvUpgradeContent = tvUpgradeContent;
        tvUpgradeContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17.45f);
        tvUpgradeContent.setTextColor(Color.parseColor("#333333"));
        tvUpgradeContent.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams lpTvUpgradeContent = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpTvUpgradeContent.topMargin = dp2px(45.8f);
        lpTvUpgradeContent.bottomMargin = dp2px(26.2f);
        layoutContent.addView(tvUpgradeContent, lpTvUpgradeContent);

        //progress bar
        ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        mPbDownload = pb;
        mPbDownload.setVisibility(View.GONE);

        GradientDrawable pbBgDrawable = new GradientDrawable();
        pbBgDrawable.setColor(Color.parseColor("#d9d9d9"));
        pbBgDrawable.setCornerRadius(8);

        GradientDrawable progressDrawable = new GradientDrawable();
        progressDrawable.setColor(Color.parseColor("#ff5400"));
        progressDrawable.setCornerRadius(8);
        ClipDrawable pbProgressClipDrawable = new ClipDrawable(progressDrawable,
                Gravity.LEFT, ClipDrawable.HORIZONTAL);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]
                {pbBgDrawable, pbProgressClipDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        pb.setProgressDrawable(layerDrawable);
        LinearLayout.LayoutParams lpPb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp2px(4.36f));
        int horizonMargin = dp2px(30);
        lpPb.leftMargin = horizonMargin;
        lpPb.rightMargin = horizonMargin;
        lpPb.bottomMargin = dp2px(20);
        layoutContent.addView(pb, lpPb);

        pb.setMax(100);
        pb.setProgress(0);

        super.setContent(layoutContent);
        super.setCanCancelByUser(false);
        super.mBtnRight.setText(UpgradeText.TEXT_UPGRADE);
        super.mBtnRight.setTextColor(Color.parseColor("#ff5400"));
    }


    /**
     * 设置副标题内容
     * @param charSequence
     */
    public void setSubTitleContent(CharSequence charSequence){
        if(null!=mTvUpgradeSubTitle){
            mTvUpgradeSubTitle.setText(charSequence);
        }
    }

    /**
     * 标题是否可见
     * @param isVisible
     */
    protected void setTitleLLVisible(boolean isVisible){
//        mlayoutTitle.setVisibility(View.GONE);
//        mTvUpgradeContent.setVisibility(View.VISIBLE);

        mlayoutTitle.setVisibility(isVisible? View.VISIBLE: View.GONE);
        mTvUpgradeContent.setVisibility(isVisible? View.GONE: View.VISIBLE);
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;

        if (!TextUtils.isEmpty(downloadUrl)) {
            DLInfo dlInfo = DLManager.getInstance(getContext().getApplicationContext()).getCompleteInfo(downloadUrl);

            if (dlInfo != null && dlInfo.currentBytes == dlInfo.totalBytes
                    && !TextUtils.isEmpty(dlInfo.dirPath) && !TextUtils.isEmpty(dlInfo.fileName)) {

                File file = new File(dlInfo.dirPath + dlInfo.fileName);
                if (file.exists() && file.length() == dlInfo.currentBytes) {
                    mBtnRight.setText(UpgradeText.TEXT_INSTALL);
                }
            }
        }
    }

    /**
     * 获得下载地址
     * @return
     */
    public final String getDownloadUrl() {
        return mDownloadUrl;
    }

    /**
     * download the file
     * @param downloadUrl the request download url
     * @param fileName download file name, if null, downloader will try to get file name from server,
     *                 if failed from server, use {@code UUID.randonUUID().toString() } as file name
     * @param listener the download listener
     */
    protected final void download(String downloadUrl, String fileName, IDListener listener) {
        String fileSaveDir;
        File externalFilesDir = getContext().getExternalFilesDir(null);
        if (externalFilesDir != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (externalFilesDir.getFreeSpace() < DOWNLOAD_MIN_FREE_SPACE) {
                showToast(UpgradeText.TEXT_DOWNLOAD_SPACE_NOT_ENOUGH);

                if (listener != null) {
                    listener.onError(EXTERNAL_STORAGE_SPACE_NOT_ENOUGH, "");
                }
                return;
            }

            fileSaveDir = externalFilesDir.getAbsolutePath() + "/upgrade/";

        } else {
            if (listener != null) {
                listener.onError(EXTERNAL_STORAGE_NOT_AVAILABLE, "");
            }
            return;
        }

        DLManager.getInstance(getContext().getApplicationContext()).dlStart(downloadUrl, fileSaveDir, fileName, listener);
    }

    /**
     * 安装apk
     * @param apkPath
     */
    protected final void installApp(String apkPath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
        getContext().startActivity(intent);
    }

    /***
     * 弹出提示
     * @param msg
     */
    //make sure call on ui thread
    protected final void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
