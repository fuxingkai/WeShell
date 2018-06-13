package cn.jufuns.ws.downloader.interfaces;

import java.io.File;

public interface IDListener {
    void onPrepare();

    void onStart(String fileName, String realUrl, long fileLength);

    void onProgress(long progress);

    void onStop(long progress);

    void onFinish(File file);

    void onError(int status, String error);
}