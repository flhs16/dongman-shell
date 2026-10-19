package com.norm.launch03;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String GAME_PKG = "com.shizi.tool.p3";
    private static final String GAME_ACTIVITY = "app.video.guoguo.SplashActivity";

    private void log(String s) {
        byte[] b = (s == null ? "" : s).concat("\n").getBytes();
        try {
            File f = new File(getFilesDir(), "log.txt");
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write(b);
            fo.close();
        } catch (Throwable ig) {
        }
        try {
            File dir = getExternalFilesDir(null);
            if (dir != null) {
                File f2 = new File(dir, "log.txt");
                FileOutputStream fo2 = new FileOutputStream(f2, true);
                fo2.write(b);
                fo2.close();
            }
        } catch (Throwable ig) {
        }
    }

    private void dumpLog() {
        try {
            File f = new File(getFilesDir(), "log.txt");
            FileInputStream fis = new FileInputStream(f);
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) > 0) sb.append(new String(buf, 0, n));
            fis.close();
            ContentValues cv = new ContentValues();
            cv.put("_display_name", "壳日志-" + System.currentTimeMillis() + ".txt");
            cv.put("mime_type", "text/plain");
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri != null) {
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(sb.toString().getBytes());
                    os.close();
                }
            }
        } catch (Throwable ig) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        log("P0 enter onCreate");

        Intent gi = getPackageManager().getLaunchIntentForPackage(GAME_PKG);
        if (gi != null) {
            log("P-launch via getLaunchIntentForPackage");
            try {
                startActivity(gi);
                finish();
                dumpLog();
                return;
            } catch (Throwable t) {
                log("launch err: " + t);
            }
        } else {
            log("P-noLaunchIntent, explicit class attempt");
            try {
                Intent x = new Intent().setClassName(GAME_PKG, GAME_ACTIVITY);
                startActivity(x);
                finish();
                dumpLog();
                return;
            } catch (Throwable t) {
                log("explicit err: " + t);
            }
        }
        log("P-install branch start");
        installFromAssets();
    }

    private void installFromAssets() {
        PackageInstaller.Session session = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            PackageInstaller pi = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams p = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sid = pi.createSession(p);
            session = pi.openSession(sid);
            log("I1 session open");

            out = session.openWrite("base.apk", 0, -1);
            in = getAssets().open("base.apk");
            byte[] buf = new byte[65536];
            int n;
            long total = 0;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                total += n;
            }
            out.flush();
            session.fsync(out);
            log("I2 copied " + total + " bytes");

            Intent status = new Intent();
            PendingIntent sender = PendingIntent.getActivity(
                    this, 0, status,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            session.commit(sender.getIntentSender());
            log("I3 committed, total=" + total);
            session.close();
            session = null;
            out = null;

            Toast.makeText(this, "\u5b89\u88c5\u5df2\u63d0\u4ea4\uff0c\u8bf7\u5728\u63a5\u4e0b\u6765\u7684\u7cfb\u7edf\u5f39\u7a97\u4e2d\u786e\u8ba4", Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            log("install err: " + t);
        } finally {
            try { if (out != null) out.close(); } catch (Throwable ig) {}
            try { if (in != null) in.close(); } catch (Throwable ig) {}
            if (session != null) {
                try { session.abandon(); } catch (Throwable ig) {}
            }
        }
        try { finish(); } catch (Throwable ig) {}
    }
}
