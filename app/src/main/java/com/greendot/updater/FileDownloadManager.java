package com.greendot.updater;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

class FileDownloadManager {

    private static FileDownloadManager instance;

    private DownloadManager mDownloadManager;


    private FileDownloadManager() {
    }

    static FileDownloadManager get() {
        if (instance == null) {
            instance = new FileDownloadManager();
        }
        return instance;
    }

    public DownloadManager getDM(Context context) {
        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) context
                    .getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        }
        return mDownloadManager;
    }


    long startDownload(UpdaterConfig updaterConfig) {
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(updaterConfig.getFileUrl()));
        req.setAllowedNetworkTypes(updaterConfig.getAllowedNetworkTypes());
        req.setAllowedOverRoaming(updaterConfig.isAllowedOverRoaming());

        if (updaterConfig.isCanMediaScanner()) {
            req.allowScanningByMediaScanner();
        }
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        req.setVisibleInDownloadsUi(updaterConfig.isShowDownloadUI());
        req.setTitle(updaterConfig.getTitle());
        req.setDescription(updaterConfig.getDescription());

        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release.apk");

        req.setTitle(updaterConfig.getTitle());
        req.setDescription(updaterConfig.getDescription());

        long id = getDM(updaterConfig.getContext()).enqueue(req);
        UpdaterUtils.saveDownloadId(updaterConfig.getContext(), id);
        return id;
    }


    /**
     * 获取文件保存的路径
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return file path
     * @see FileDownloadManager#getDownloadUri(Context, long)
     */
    private String getDownloadPath(Context context, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = getDM(context).query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                }
            } finally {
                c.close();
            }
        }
        return null;
    }


    /**
     * 获取保存文件的地址
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @see FileDownloadManager#getDownloadPath(Context, long)
     */
    public Uri getDownloadUri(Context context, long downloadId) {
        return getDM(context).getUriForDownloadedFile(downloadId);
    }

    /**
     * 获取下载状态
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING
     * @see DownloadManager#STATUS_PAUSED
     * @see DownloadManager#STATUS_RUNNING
     * @see DownloadManager#STATUS_SUCCESSFUL
     * @see DownloadManager#STATUS_FAILED
     */
    public int getDownloadStatus(Context context, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = getDM(context).query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }
}
