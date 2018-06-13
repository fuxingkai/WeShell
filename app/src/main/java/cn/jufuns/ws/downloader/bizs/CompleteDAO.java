package cn.jufuns.ws.downloader.bizs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_COMPLETE;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_CURRENT_BYTES;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_DIR_PATH;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_DISPOSITION;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_ETAG;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_FILE_NAME;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_LOCATION;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_MIME_TYPE;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_TOTAL_BYTES;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_URL_BASE;
import static cn.jufuns.ws.downloader.bizs.DLCons.DBCons.TB_TASK_URL_REAL;

public class CompleteDAO implements ICompleteDAO {

    private final DLDBHelper mDbHelper;

    CompleteDAO(Context context) {
        mDbHelper = new DLDBHelper(context);
    }

    @Override
    public void insertCompleteInfo(DLInfo info) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO " + TB_COMPLETE + "(" +
                        TB_TASK_URL_BASE + ", " +
                        TB_TASK_URL_REAL + ", " +
                        TB_TASK_DIR_PATH + ", " +
                        TB_TASK_FILE_NAME + ", " +
                        TB_TASK_MIME_TYPE + ", " +
                        TB_TASK_ETAG + ", " +
                        TB_TASK_DISPOSITION + ", " +
                        TB_TASK_LOCATION + ", " +
                        TB_TASK_CURRENT_BYTES + ", " +
                        TB_TASK_TOTAL_BYTES + ") values (?,?,?,?,?,?,?,?,?,?)",
                new Object[]{info.baseUrl, info.realUrl, info.dirPath, info.fileName,
                        info.mimeType, info.eTag, info.disposition, info.location,
                        info.currentBytes, info.totalBytes});
        db.close();

    }

    @Override
    public void deleteCompleteInfo(String url) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TB_COMPLETE + " WHERE " + TB_TASK_URL_BASE + "=?",
                new String[]{url});
        db.close();

    }

    @Override
    public void updateCompleteInfo(DLInfo info) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + TB_COMPLETE + " SET " +
                TB_TASK_DISPOSITION + "=?," +
                TB_TASK_LOCATION + "=?," +
                TB_TASK_MIME_TYPE + "=?," +
                TB_TASK_TOTAL_BYTES + "=?," +
                TB_TASK_FILE_NAME + "=?," +
                TB_TASK_CURRENT_BYTES + "=? WHERE " +
                TB_TASK_URL_BASE + "=?", new Object[]{info.disposition, info.location,
                info.mimeType, info.totalBytes, info.fileName, info.currentBytes, info.baseUrl});
        db.close();

    }

    @Override
    public DLInfo queryCompleteInfo(String url) {
        DLInfo info = null;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +
                TB_TASK_URL_BASE + ", " +
                TB_TASK_URL_REAL + ", " +
                TB_TASK_DIR_PATH + ", " +
                TB_TASK_FILE_NAME + ", " +
                TB_TASK_MIME_TYPE + ", " +
                TB_TASK_ETAG + ", " +
                TB_TASK_DISPOSITION + ", " +
                TB_TASK_LOCATION + ", " +
                TB_TASK_CURRENT_BYTES + ", " +
                TB_TASK_TOTAL_BYTES + " FROM " +
                TB_COMPLETE + " WHERE " +
                TB_TASK_URL_BASE + "=?", new String[]{url});
        if (c.moveToFirst()) {
            info = new DLInfo();
            info.baseUrl = c.getString(0);
            info.realUrl = c.getString(1);
            info.dirPath = c.getString(2);
            info.fileName = c.getString(3);
            info.mimeType = c.getString(4);
            info.eTag = c.getString(5);
            info.disposition = c.getString(6);
            info.location = c.getString(7);
            info.currentBytes = c.getInt(8);
            info.totalBytes = c.getInt(9);
        }
        c.close();
        db.close();
        return info;
    }

}
