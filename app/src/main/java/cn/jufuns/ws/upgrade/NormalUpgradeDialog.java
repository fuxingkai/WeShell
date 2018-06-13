package cn.jufuns.ws.upgrade;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;


import java.io.File;
import java.util.Locale;

import cn.jufuns.ws.downloader.bizs.DLError;
import cn.jufuns.ws.downloader.bizs.DLInfo;
import cn.jufuns.ws.downloader.bizs.DLManager;
import cn.jufuns.ws.downloader.interfaces.IDListener;
import cn.jufuns.ws.ui.OnDialogButtonClickListener;


/**
 * 非强制升级弹出框
 * Created by zhangxg on 2016/8/10.
 */
public final class NormalUpgradeDialog extends AbstractUpgradeDialog implements IDListener {

    private static final String TAG = "NormalUpgradeDialog";
    private static final boolean DEBUG = false;

    /* package */ static final int NOTIFICATION_ID = 0xaabbcc;

    /* package */ long mTotalBytes;
    /* package */ int mNotifyDrawableResId;
    /* package */ NotificationManagerCompat mNotifyMgr;
    /* package */ Handler mHandler;

    /* package */ boolean mIsDownloading;
    /* package */ SpannableStringBuilder mSpanStrBuilder;
    /* package */ ForegroundColorSpan mErrorContentSpan;
    /* package */ ForegroundColorSpan mBgDownloadSpan;

    public NormalUpgradeDialog(Context context) {
        super(context);

        setNotificationIcon(context.getApplicationInfo().icon);

        mNotifyMgr = NotificationManagerCompat.from(getContext());
        mHandler = new Handler(Looper.getMainLooper());

        mSpanStrBuilder = new SpannableStringBuilder();
        mErrorContentSpan = new ForegroundColorSpan(Color.parseColor("#999999"));
        mBgDownloadSpan = new ForegroundColorSpan(Color.parseColor("#333333"));

//        super.setTitleText(UpgradeText.TEXT_TITLE);
        setTitleLLVisible(true);
        super.mTvUpgradeContent.setText(UpgradeText.TEXT_CONTENT_UPGRADE_NORMAL);
        super.setLeftBtnText(UpgradeText.TEXT_CANCEL);
        super.setRightBtnText(UpgradeText.TEXT_UPGRADE);
        super.setTouchOutsideCancel(false);
        super.setHideWhenButtonClick(false);

        super.setOnButtonClickListener(new OnDialogButtonClickListener() {

            @Override
            public void onLeftButtonClick() {
                cancelUpgrade();
            }

            @Override
            public void onRightButtonClick() {
                doUpgrade();
            }
        });
    }

    /**
     * 取消升级
     */
    /* package */ void cancelUpgrade() {

        if (mIsDownloading) {
            String downloadUrl = super.getDownloadUrl();
            if (!TextUtils.isEmpty(downloadUrl)) {
                DLManager.getInstance(getContext().getApplicationContext()).dlCancel(downloadUrl);
                mNotifyMgr.cancel(NOTIFICATION_ID);
            }
        }

        hide();
    }

    /**
     * 开始升级
     */
    /* package */ void doUpgrade() {
        if (mIsDownloading) {
            hide();
            return;
        }

        String downloadUrl = super.getDownloadUrl();
        if (TextUtils.isEmpty(downloadUrl)) {
            showToast(UpgradeText.TEXT_EMPTY_DOWNLOAD_URL);
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
                hide();
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

        mIsDownloading = true;

        mSpanStrBuilder.clear();
        mSpanStrBuilder.clearSpans();
        mSpanStrBuilder.append(UpgradeText.TEXT_DOWNLOAD_IN_BACKGROUND);
        mSpanStrBuilder.setSpan(mBgDownloadSpan, 0, mSpanStrBuilder.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mTvUpgradeContent.setText(mSpanStrBuilder);
        setRightBtnText(mSpanStrBuilder);

        super.download(downloadUrl, null, this);
    }

    /**
     * 设置通知的icon
     * @param drawResId
     */
    public void setNotificationIcon(int drawResId) {
        try {
            getContext().getResources().getDrawable(drawResId);
            mNotifyDrawableResId = drawResId;
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "the resource with id " + drawResId + " is not found, please check");
        }
    }

    @Override
    protected void onShow() {
        super.onShow();
        if (!mIsDownloading) {
            super.mTvUpgradeContent.setText(UpgradeText.TEXT_CONTENT_UPGRADE_NORMAL);
            setTitleLLVisible(true);
            super.setRightBtnText(UpgradeText.TEXT_UPGRADE);
            super.setLeftBtnVisible(true);
            super.mPbDownload.setVisibility(View.GONE);

            setDownloadUrl(getDownloadUrl());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mHandler = null;
        super.finalize();
    }

    @Override
    public void onPrepare() {
        if (DEBUG) {
            Log.d(TAG, "onPrepare");
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                setTitleLLVisible(false);
                mIsDownloading = true;
                mTvUpgradeContent.setText(UpgradeText.TEXT_UPGRADE_ONGOING);
                mPbDownload.setVisibility(View.VISIBLE);

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(mNotifyDrawableResId)
                        .setContentTitle(UpgradeText.TEXT_NOTIFICATION_TITLE)
                        .setContentText(UpgradeText.TEXT_NOTIFICATION_CONTENT_PREPARE)
                        .setTicker(UpgradeText.TEXT_NOTIFICATION_CONTENT_PREPARE)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();

                mNotifyMgr.notify(NOTIFICATION_ID, notification);
            }
        });
    }

    @Override
    public void onStart(String fileName, String realUrl, final long fileLength) {
        if (DEBUG) {
            Log.d(TAG, "onStart --> " + fileLength);
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mIsDownloading = true;
                mTotalBytes = fileLength;
            }
        });
    }

    @Override
    public void onProgress(final long progress) {
        if (DEBUG) {
            Log.d(TAG, "onProgress --> " + progress);
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int pbProgress = ((mTotalBytes == 0L) ? 0 : (int) (progress * 100 / mTotalBytes));
                String pbProgressStr = pbProgress + " %";
                mPbDownload.setProgress(pbProgress);

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(mNotifyDrawableResId)
                        .setContentTitle(UpgradeText.TEXT_NOTIFICATION_TITLE)
                        .setContentText(String.format(Locale.CHINA, UpgradeText.TEXT_NOTIFICATION_CONTENT_DOWNLOADING, pbProgressStr))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();

                mNotifyMgr.notify(NOTIFICATION_ID, notification);
            }
        });
    }

    @Override
    public void onStop(long progress) {
        if (DEBUG) {
            Log.d(TAG, "onStop");
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mIsDownloading = false;
                setLeftBtnVisible(true);
                setRightBtnText(UpgradeText.TEXT_RETRY);
                setTitleLLVisible(false);
                mSpanStrBuilder.clear();
                mSpanStrBuilder.clearSpans();
                mSpanStrBuilder.append(UpgradeText.TEXT_DOWNLOAD_ERROR_UNKNOWN);
                mSpanStrBuilder.setSpan(mErrorContentSpan, 0, mSpanStrBuilder.length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mTvUpgradeContent.setText(mSpanStrBuilder);

                mPbDownload.setVisibility(View.GONE);

                mNotifyMgr.cancel(NOTIFICATION_ID);
            }
        });
    }

    @Override
    public void onFinish(final File file) {
        if (DEBUG) {
            Log.d(TAG, "onFinish --> " + file.getName());
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                mIsDownloading = false;
                mPbDownload.setVisibility(View.GONE);
                mTvUpgradeContent.setText(UpgradeText.TEXT_DOWNLOAD_COMPLETE);
                mBtnRight.setText(UpgradeText.TEXT_INSTALL);
                setLeftBtnVisible(true);
                setTitleLLVisible(true);
                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(mNotifyDrawableResId)
                        .setContentTitle(UpgradeText.TEXT_NOTIFICATION_TITLE)
                        .setContentText(UpgradeText.TEXT_NOTIFICATION_CONTENT_DOWNLOAD_COMPLETE)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();

                String filePath = file.getAbsolutePath();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
                notification.contentIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                mNotifyMgr.notify(NOTIFICATION_ID, notification);

                //should hide the dialog at the moment?
                hide();

                installApp(filePath);
            }
        });
    }

    @Override
    public void onError(final int status, final String error) {
        if (DEBUG) {
            Log.d(TAG, "onError --> " + status);
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                mIsDownloading = false;

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(mNotifyDrawableResId)
                        .setContentTitle(UpgradeText.TEXT_NOTIFICATION_TITLE)
                        .setContentText(UpgradeText.TEXT_NOTIFICATION_CONTENT_DOWNLOAD_ERROR)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();

                mNotifyMgr.notify(NOTIFICATION_ID, notification);

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

                mBtnRight.setText(UpgradeText.TEXT_RETRY);
                mPbDownload.setVisibility(View.GONE);
                setLeftBtnVisible(true);

                showToast(errText);
            }
        });
    }
}
