package cn.jufuns.ws.downloader.bizs;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

class DLThread implements Runnable {

    private static final String TAG = DLThread.class.getSimpleName();

    private Context ctx;
    private DLThreadInfo dlThreadInfo;
    private DLInfo dlInfo;
    private IDLThreadListener listener;

    private long mLastUpdateTime;

    public DLThread(Context ctx, DLThreadInfo dlThreadInfo, DLInfo dlInfo, IDLThreadListener listener) {
        this.ctx = ctx;
        this.dlThreadInfo = dlThreadInfo;
        this.listener = listener;
        this.dlInfo = dlInfo;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection) new URL(dlInfo.realUrl).openConnection();
            conn.setConnectTimeout(DLCons.Base.DEFAULT_TIMEOUT);
            conn.setReadTimeout(DLCons.Base.DEFAULT_TIMEOUT);

            addRequestHeaders(conn);

            raf = new RandomAccessFile(dlInfo.file, "rwd");
            raf.seek(dlThreadInfo.start);

            is = conn.getInputStream();

            mLastUpdateTime = System.currentTimeMillis();

            byte[] b = new byte[4096];
            int len;
            while (!dlThreadInfo.isStop && (len = is.read(b)) != -1) {
                dlThreadInfo.start += len;
                raf.write(b, 0, len);
                updateDLThreadInfoIfNeeded();
                listener.onProgress(len);
            }
            if (dlThreadInfo.isStop) {
                Log.d(TAG, "Thread " + dlThreadInfo.id + " will be stopped.");
                listener.onStop(dlThreadInfo);
            } else {
                Log.d(TAG, "Thread " + dlThreadInfo.id + " will be finished.");
                listener.onFinish(dlThreadInfo);
            }
        } catch (IOException e) {
            listener.onStop(dlThreadInfo);
            e.printStackTrace();
        } finally {

            ctx = null;

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != raf) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    private void addRequestHeaders(HttpURLConnection conn) {
        for (DLHeader header : dlInfo.requestHeaders) {
            conn.addRequestProperty(header.key, header.value);
        }
        conn.setRequestProperty("Range", "bytes=" + dlThreadInfo.start + "-" + dlThreadInfo.end);
    }

    private void updateDLThreadInfoIfNeeded() {
        long now = System.currentTimeMillis();
        if (Math.abs(now - mLastUpdateTime) > 2000L) {
            DLDBManager.getInstance(ctx).updateThreadInfo(dlThreadInfo);
            mLastUpdateTime = now;
        }
    }
}