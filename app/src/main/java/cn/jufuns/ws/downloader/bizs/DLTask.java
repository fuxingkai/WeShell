package cn.jufuns.ws.downloader.bizs;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static cn.jufuns.ws.downloader.bizs.DLCons.Base.DEFAULT_TIMEOUT;
import static cn.jufuns.ws.downloader.bizs.DLCons.Base.LENGTH_PER_THREAD;
import static cn.jufuns.ws.downloader.bizs.DLCons.Base.MAX_REDIRECTS;
import static cn.jufuns.ws.downloader.bizs.DLCons.Code.*;
import static cn.jufuns.ws.downloader.bizs.DLCons.Code.HTTP_TEMP_REDIRECT;
import static cn.jufuns.ws.downloader.bizs.DLError.ERROR_CREATE_FILE;
import static cn.jufuns.ws.downloader.bizs.DLError.ERROR_OPEN_CONNECT;
import static cn.jufuns.ws.downloader.bizs.DLError.ERROR_UNHANDLED_REDIRECT;

class DLTask implements Runnable, IDLThreadListener {
    private static final String TAG = DLTask.class.getSimpleName();

    private DLInfo info;
    private Context context;

    private long totalProgress;
    private int count;
    private long lastTime = System.currentTimeMillis();
    private long mLastUpdateTime = System.currentTimeMillis();

    DLTask(Context context, DLInfo info) {
        this.info = info;
        this.context = context;
        this.totalProgress = info.currentBytes;
        if (!info.isResume) {
            DLDBManager.getInstance(context).insertTaskInfo(info);
        }
    }

    @Override
    public synchronized void onProgress(long progress) {
        totalProgress += progress;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000L) {
            if (DLCons.DEBUG) {
                Log.d(TAG, totalProgress + "");
            }

            if (info.hasListener) {
                info.listener.onProgress(totalProgress);
            }

            lastTime = currentTime;
        }
    }

    @Override
    public synchronized void onStop(DLThreadInfo threadInfo) {
        if (null == threadInfo) {
            DLManager.getInstance(context).removeDLTask(info.baseUrl);
            DLDBManager.getInstance(context).deleteTaskInfo(info.baseUrl);
            if (info.hasListener) {
                info.listener.onProgress(info.totalBytes);
                info.listener.onStop(info.totalBytes);
            }
            return;
        }
        DLDBManager.getInstance(context).updateThreadInfo(threadInfo);
        count++;
        if (count >= info.threads.size()) {
            if (DLCons.DEBUG) {
                Log.d(TAG, "All the threads was stopped.");
            }
            info.currentBytes = totalProgress;
            DLManager.getInstance(context).addStopTask(info)
                    .removeDLTask(info.baseUrl);
            DLDBManager.getInstance(context).updateTaskInfo(info);
            count = 0;
            if (info.hasListener)
                info.listener.onStop(totalProgress);
        }
    }

    @Override
    public synchronized void onFinish(DLThreadInfo threadInfo) {
        if (null == threadInfo) {
            DLManager.getInstance(context).removeDLTask(info.baseUrl);
            DLDBManager.getInstance(context).deleteTaskInfo(info.baseUrl);

            DLInfo completeInfo = DLDBManager.getInstance(context).queryCompleteInfo(info.baseUrl);
            DLInfo cloneInfo = info.clone();
            cloneInfo.currentBytes = cloneInfo.totalBytes;
            if (completeInfo == null) {
                DLDBManager.getInstance(context).insertCompleteInfo(cloneInfo);
            } else {
                DLDBManager.getInstance(context).updateCompleteInfo(cloneInfo);
            }

            if (info.hasListener) {
                info.listener.onProgress(info.totalBytes);
                info.listener.onFinish(info.file);
            }
            return;
        }

        info.removeDLThread(threadInfo);
        DLDBManager.getInstance(context).deleteThreadInfo(threadInfo.id);
        if (DLCons.DEBUG) {
            Log.d(TAG, "Thread size " + info.threads.size());
        }
        if (info.threads.isEmpty()) {
            if (DLCons.DEBUG) {
                Log.d(TAG, "Task was finished.");
            }
            DLManager.getInstance(context).removeDLTask(info.baseUrl);
            DLDBManager.getInstance(context).deleteTaskInfo(info.baseUrl);

            DLInfo completeInfo = DLDBManager.getInstance(context)
                    .queryCompleteInfo(info.baseUrl);
            DLInfo cloneInfo = info.clone();
            cloneInfo.currentBytes = cloneInfo.totalBytes;
            if (completeInfo == null) {
                DLDBManager.getInstance(context).insertCompleteInfo(cloneInfo);
            } else {
                DLDBManager.getInstance(context).updateCompleteInfo(cloneInfo);
            }

            if (info.hasListener) {
                info.listener.onProgress(info.totalBytes);
                info.listener.onFinish(info.file);
            }
            DLManager.getInstance(context).addDLTask();
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (info.redirect < MAX_REDIRECTS) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(info.realUrl).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);
                conn.setReadTimeout(DEFAULT_TIMEOUT);

                addRequestHeaders(conn);

                final int code = conn.getResponseCode();
                if (DLCons.DEBUG) {
                    Log.d(TAG, code + "");
                }
                switch (code) {
                    case HTTP_OK:
                    case HTTP_PARTIAL:
                        dlInit(conn, code);
                        return;
                    case HTTP_MOVED_PERM:
                    case HTTP_MOVED_TEMP:
                    case HTTP_SEE_OTHER:
                    case HTTP_NOT_MODIFIED:
                    case HTTP_TEMP_REDIRECT:
                        final String location = conn.getHeaderField("location");
                        if (TextUtils.isEmpty(location)) {
                            if (DLCons.DEBUG) {
                                Log.d(TAG, "Can not obtain real url from location in header.");
                            }

                            if (info.hasListener) {
                                info.listener.onError(DLError.ERROR_CANNOT_GET_URL, "Can not obtain real url from location in header.");
                            }
                            return;
                        }

                        info.realUrl = location;
                        info.redirect++;
                        continue;
                    default:
                        if (info.hasListener) {
                            info.listener.onError(code, conn.getResponseMessage());
                        }

                        DLManager.getInstance(context).removeDLTask(info.baseUrl);
                        return;
                }
            } catch (Exception e) {
                if (info.hasListener) {
                    info.listener.onError(ERROR_OPEN_CONNECT, e.toString());
                }
                DLManager.getInstance(context).removeDLTask(info.baseUrl);
                return;
            } finally {
                if (null != conn) {
                    conn.disconnect();
                }
            }
        }

        if (info.hasListener) {
            info.listener.onError(ERROR_UNHANDLED_REDIRECT, "too many redirects");
        }
    }

    private void dlInit(HttpURLConnection conn, int code) throws Exception {
        readResponseHeaders(conn);
        DLDBManager.getInstance(context).updateTaskInfo(info);
        if (!DLUtil.createFile(info.dirPath, info.fileName)) {
            if (info.hasListener) {
                info.listener.onError(ERROR_CREATE_FILE, "can not create file");
            }
            return;
        }

        info.file = new File(info.dirPath, info.fileName);
        if (info.file.exists() && info.file.length() == info.totalBytes) {
            Log.d(TAG, "The file which we want to download was already here.");
            onFinish(null);
            return;
        }

        if (info.hasListener) {
            info.listener.onStart(info.fileName, info.realUrl, info.totalBytes);
        }

        switch (code) {
            case HTTP_OK:
                dlData(conn);
                break;
            case HTTP_PARTIAL:
                if (info.totalBytes <= 0) {
                    dlData(conn);
                    break;
                }
                if (info.isResume) {
                    for (DLThreadInfo threadInfo : info.threads) {
                        DLManager.getInstance(context).addDLThread(new DLThread(context, threadInfo, info, this));
                    }
                    break;
                }
                dlDispatch();
                break;
        }
    }

    private void dlDispatch() {
        //usually the max thread count is 3
        long threadSize;
        long threadLength = LENGTH_PER_THREAD;
        if (info.totalBytes <= LENGTH_PER_THREAD) {
            threadSize = 2;
            threadLength = info.totalBytes / threadSize;
        } else {
            threadSize = 3;
            threadLength = info.totalBytes / threadSize;
        }
        long remainder = info.totalBytes % threadLength;
        for (int i = 0; i < threadSize; i++) {
            long start = i * threadLength;
            long end = start + threadLength - 1;
            if (i == threadSize - 1) {
                end = start + threadLength + remainder;
            }
            DLThreadInfo threadInfo = new DLThreadInfo(UUID.randomUUID().toString(),
                    info.baseUrl, start, end);
            info.addDLThread(threadInfo);
            DLDBManager.getInstance(context).insertThreadInfo(threadInfo);
            DLManager.getInstance(context).addDLThread(new DLThread(context, threadInfo, info, this));
        }
    }

    private void dlData(HttpURLConnection conn) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = conn.getInputStream();
            fos = new FileOutputStream(info.file);
            byte[] b = new byte[4096];
            int len;

            while (!info.isStop && (len = is.read(b)) != -1) {
                fos.write(b, 0, len);
                onProgress(len);
            }

            if (!info.isStop) {
                onFinish(null);
            } else {
                onStop(null);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw(e);
        } finally {

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {

                }
            }
        }

    }

    private void addRequestHeaders(HttpURLConnection conn) {
        for (DLHeader header : info.requestHeaders) {
            conn.addRequestProperty(header.key, header.value);
        }
    }

    private void readResponseHeaders(HttpURLConnection conn) {
        info.disposition = conn.getHeaderField("Content-Disposition");
        info.location = conn.getHeaderField("Content-Location");
        info.mimeType = DLUtil.normalizeMimeType(conn.getContentType());
        final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
        if (TextUtils.isEmpty(transferEncoding)) {
            try {
                info.totalBytes = Integer.parseInt(conn.getHeaderField("Content-Length"));
            } catch (NumberFormatException e) {
                info.totalBytes = -1;
            }
        } else {
            info.totalBytes = -1;
        }

        if (info.totalBytes == -1
                && (TextUtils.isEmpty(transferEncoding)
                        || !transferEncoding.equalsIgnoreCase("chunked"))) {
            throw new RuntimeException("Can not obtain size of download file.");
        }

        if (TextUtils.isEmpty(info.fileName))
            info.fileName = DLUtil.obtainFileName(info.realUrl,
                    info.disposition, info.location);
    }
}