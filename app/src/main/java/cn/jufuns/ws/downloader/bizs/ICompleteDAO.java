package cn.jufuns.ws.downloader.bizs;


public interface ICompleteDAO {

    void insertCompleteInfo(DLInfo info);

    void deleteCompleteInfo(String url);

    void updateCompleteInfo(DLInfo info);

    DLInfo queryCompleteInfo(String url);
}
