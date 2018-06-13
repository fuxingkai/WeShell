package cn.jufuns.ws.upgrade;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.File;

import cn.jufuns.ws.downloader.bizs.DLError;
import cn.jufuns.ws.downloader.bizs.DLInfo;
import cn.jufuns.ws.downloader.bizs.DLManager;
import cn.jufuns.ws.downloader.interfaces.IDListener;
import cn.jufuns.ws.ui.OnDialogButtonClickListener;


/**
 * 强制升级弹出框
 * Created by zhangxg on 2016/8/11.
 */
public final class ForceUpgradeDialog extends AbstractUpgradeDialog implements IDListener {

    private static final String TAG = "ForceUpgradeDialog";
    private static final boolean DEBUG = false;

    /* package */ long mTotalBytes;

    /* package */ SpannableStringBuilder mSpanStrBuilder;
    /* package */ ForegroundColorSpan mErrorContentSpan;

    public ForceUpgradeDialog(Context context) {
        super(context);
//        DLManager.getInstance(context.getApplicationContext()).setDebugEnable(true);
        setTitleLLVisible(true);
        super.mTvUpgradeContent.setText(UpgradeText.TEXT_CONTENT_UPGRADE_FORCE);
        super.setLeftBtnVisible(false);
        super.setHideWhenButtonClick(false);
        super.setCanCancelByUser(false);
        super.mBtnRight.setText(UpgradeText.TEXT_UPGRADE);

        super.setOnButtonClickListener(new OnDialogButtonClickListener() {

            @Override
            public void onLeftButtonClick() {

            }

            @Override
            public void onRightButtonClick() {
                doWork();
            }
        });

        mSpanStrBuilder = new SpannableStringBuilder();
        mErrorContentSpan = new ForegroundColorSpan(Color.parseColor("#999999"));
    }

    /**
     * 开始下载
     */
    /* package */ void doWork() {

        String downloadUrl = getDownloadUrl();
        if (TextUtils.isEmpty(downloadUrl)) {
            Toast.makeText(getContext(), UpgradeText.TEXT_EMPTY_DOWNLOAD_URL, Toast.LENGTH_SHORT).show();
            return;
        }

        DLManager dlManager = DLManager.getInstance(getContext().getApplicationContext());
        DLInfo dlInfo = dlManager.getCompleteInfo(downloadUrl);
        if (dlInfo != null && dlInfo.currentBytes == dlInfo.totalBytes
                && !TextUtils.isEmpty(dlInfo.dirPath) && !TextUtils.isEmpty(dlInfo.fileName)) {

            File file = new File(dlInfo.dirPath + dlInfo.fileName);
            if (file.exists() && file.length() == dlInfo.currentBytes) {
                //download complete, and file exist
                installApp(dlInfo.dirPath + dlInfo.fileName);
                return;
            }
        }

        dlInfo = dlManager.getDLInfo(downloadUrl);

        if (dlInfo == null) {
            mPbDownload.setProgress(0);
            mTotalBytes = 0L;
        } else {
            if (dlInfo.totalBytes == 0L) {
                mPbDownload.setProgress(0);
            } else {
                mPbDownload.setProgress((int) (dlInfo.currentBytes * 100 / dlInfo.totalBytes));
            }

            mTotalBytes = dlInfo.totalBytes;
        }

        super.download(downloadUrl, null, this);
    }

    @Override
    public void onPrepare() {
        if (DEBUG) {
            Log.d(TAG, "onPrepare");
        }

        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                //修改正在强制升级的样式
                mBtnRight.setEnabled(false);
                setTitleLLVisible(false);
                mBtnRight.setText(UpgradeText.TEXT_EMPTY);
                mviewContentDivider.setVisibility(View.GONE);
                mLinearLayoutBtn.setVisibility(View.GONE);
                setPbBottomMargin(45.8f);
                mTvUpgradeContent.setText(UpgradeText.TEXT_CONTENT_UPGRADE_FORCEING);

                if (mPbDownload.getVisibility() != View.VISIBLE) {
                    mPbDownload.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 设置下载进度调的bottom边距
     * @param margin
     */
    public void setPbBottomMargin(float margin){
        LinearLayout.LayoutParams lpPb = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp2px(4.36f));
        lpPb.bottomMargin = dp2px(margin);
        mPbDownload.setLayoutParams(lpPb);
    }

    @Override
    public void onStart(String fileName, String realUrl, final long fileLength) {
        if (DEBUG) {
            Log.d(TAG, "onStart --> " + fileLength);
        }
        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                mTotalBytes = fileLength;
            }
        });
    }

    @Override
    public void onProgress(final long progress) {
        if (DEBUG) {
            Log.d(TAG, "onProgress --> " + progress);
        }
        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                int pbProgress = ((mTotalBytes == 0L) ? 0 : (int) (progress * 100 / mTotalBytes));
                mPbDownload.setProgress(pbProgress);
            }
        });
    }

    @Override
    public void onStop(long progress) {
        if (DEBUG) {
            Log.d(TAG, "onStop");
        }

        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                setTitleLLVisible(false);
                mSpanStrBuilder.clear();
                mSpanStrBuilder.clearSpans();
                mSpanStrBuilder.append(UpgradeText.TEXT_DOWNLOAD_ERROR_UNKNOWN);
                mSpanStrBuilder.setSpan(mErrorContentSpan, 0, mSpanStrBuilder.length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mTvUpgradeContent.setText(mSpanStrBuilder);
                mPbDownload.setVisibility(View.GONE);
                mBtnRight.setText(UpgradeText.TEXT_RETRY);
                mBtnRight.setEnabled(true);
                //修改正在强制升级的样式
                mviewContentDivider.setVisibility(View.VISIBLE);
                mLinearLayoutBtn.setVisibility(View.VISIBLE);
                setPbBottomMargin(20f);

            }
        });
    }

    @Override
    public void onFinish(final File file) {
        if (DEBUG) {
            Log.d(TAG, "onFinish --> " + file.getName());
        }
        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                mBtnRight.setText(UpgradeText.TEXT_INSTALL);
                mBtnRight.setEnabled(true);
                setTitleLLVisible(true);
                //修改正在强制升级的样式
                mviewContentDivider.setVisibility(View.VISIBLE);
                mLinearLayoutBtn.setVisibility(View.VISIBLE);
                setPbBottomMargin(20f);

                mPbDownload.setVisibility(View.GONE);

                installApp(file.getAbsolutePath());
            }
        });
    }

    @Override
    public void onError(final int status, String error) {
        if (DEBUG) {
            Log.d(TAG, "onError --> " + status);
        }
        mBtnRight.post(new Runnable() {

            @Override
            public void run() {
                mBtnRight.setText(UpgradeText.TEXT_RETRY);
                mBtnRight.setEnabled(true);
                //修改正在强制升级的样式
                mviewContentDivider.setVisibility(View.VISIBLE);
                mLinearLayoutBtn.setVisibility(View.VISIBLE);
                setPbBottomMargin(20f);

                mPbDownload.setVisibility(View.GONE);

                String errText;
                switch (status) {
                    case DLError.ERROR_NOT_NETWORK: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_NETWORK_NOT_AVAILABLE;
                        break;
                    }

                    case DLError.ERROR_CREATE_FILE: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_CREATE_FILE_FAILED;
                        break;
                    }

                    case DLError.ERROR_INVALID_URL: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_INVALID_URL;
                        break;
                    }

                    case DLError.ERROR_REPEAT_URL: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_TASK_ALREADY_EXISTS;
                        break;
                    }

                    case DLError.ERROR_CANNOT_GET_URL: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_CAN_NOT_GET_DOWNLOAD_URL;
                        break;
                    }

                    case DLError.ERROR_OPEN_CONNECT: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_CAN_NOT_OPEN_CONNECT;
                        break;
                    }

                    case DLError.ERROR_UNHANDLED_REDIRECT: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_UNHANDLED_REDIRECT;
                        break;
                    }

                    case EXTERNAL_STORAGE_NOT_AVAILABLE: {
                        errText = UpgradeText.TEXT_EXTERNAL_STORAGE_NOT_AVAILABLE;
                        break;
                    }

                    case EXTERNAL_STORAGE_SPACE_NOT_ENOUGH: {
                        errText = UpgradeText.TEXT_DOWNLOAD_SPACE_NOT_ENOUGH;
                        break;
                    }

                    default: {
                        errText = UpgradeText.TEXT_DOWNLOAD_ERROR_UNKNOWN;
                        break;
                    }
                }

                setTitleLLVisible(false);
                mSpanStrBuilder.clear();
                mSpanStrBuilder.clearSpans();
                mSpanStrBuilder.append(errText);
                mSpanStrBuilder.setSpan(mErrorContentSpan, 0, mSpanStrBuilder.length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mTvUpgradeContent.setText(mSpanStrBuilder);
            }
        });
    }
}
