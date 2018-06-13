package cn.jufuns.ws.downloader.bizs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jufuns.ws.downloader.interfaces.IDListener;

public class DLInfo implements Cloneable {
    public long totalBytes;
    public long currentBytes;
    public String fileName;
    public String dirPath;
    public String baseUrl;
    public String realUrl;

    int redirect;
    boolean hasListener;
    boolean isResume;
    boolean isStop;
    String mimeType;
    String eTag;
    String disposition;
    String location;
    List<DLHeader> requestHeaders;
    final List<DLThreadInfo> threads;
    IDListener listener;
    File file;

    DLInfo() {
        threads = new ArrayList<DLThreadInfo>();
    }

    synchronized void addDLThread(DLThreadInfo info) {
        threads.add(info);
    }

    synchronized void removeDLThread(DLThreadInfo info) {
        threads.remove(info);
    }

    @Override
    public DLInfo clone() {
        DLInfo dlInfo = null;
        try {
            dlInfo = (DLInfo) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dlInfo;
    }
}