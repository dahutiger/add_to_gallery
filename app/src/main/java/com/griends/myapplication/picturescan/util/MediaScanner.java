//package com.griends.myapplication.picturescan.util;
//
//import android.content.Context;
//import android.media.MediaScannerConnection;
//import android.net.Uri;
//import android.util.Log;
//
//import java.io.File;
//
//public class MediaScanner {
//
//    private MediaScannerConnection mConn = null;
//    private ScannerClient mClient = null;
//    private File mFile = null;
//    private String mMimeType = null;
//
//    public MediaScanner(Context context) {
//        if (mClient == null) {
//            mClient = new ScannerClient();
//        }
//        if (mConn == null) {
//            mConn = new MediaScannerConnection(context, mClient);
//        }
//    }
//
//    class ScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {
//
//        public void onMediaScannerConnected() {
//
//            if (mFile == null) {
//                return;
//            }
//            scan(mFile, mMimeType);
//        }
//
//        public void onScanCompleted(String path, Uri uri) {
//            mConn.disconnect();
//        }
//
//        private void scan(File file, String type) {
//            try {
//                Log.d("aaaaa", "file = " + file.getCanonicalPath());
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//            if (file.isFile()) {
//                mConn.scanFile(file.getAbsolutePath(), type);
//                return;
//            }
//            File[] files = file.listFiles();
//            if (files == null) {
//                return;
//            }
//            for (File f : file.listFiles()) {
//                scan(f, type);
//            }
//        }
//    }
//
//    public void scanFile(File file, String mimeType) {
//        mFile = file;
//        mMimeType = mimeType;
//        mConn.connect();
//    }
//}
