package cn.jufuns.ws.downloader.bizs;


interface IDLThreadListener {
    void onProgress(long progress);

    void onStop(DLThreadInfo threadInfo);

    void onFinish(DLThreadInfo threadInfo);
}