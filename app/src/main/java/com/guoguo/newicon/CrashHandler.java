package com.guoguo.newicon;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context ctx;
    private final Thread.UncaughtExceptionHandler prev;

    public CrashHandler(Context c) {
        ctx = c.getApplicationContext();
        prev = Thread.getDefaultUncaughtExceptionHandler();
    }

    private void log(String s) {
        try {
            File f = new File(ctx.getFilesDir(), "log.txt");
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write((s + "\n").getBytes());
            fo.close();
        } catch (Throwable ig) {
        }
        try {
            File dir = ctx.getExternalFilesDir(null);
            if (dir != null) {
                File f2 = new File(dir, "log.txt");
                FileOutputStream fo2 = new FileOutputStream(f2, true);
                fo2.write((s + "\n").getBytes());
                fo2.close();
            }
        } catch (Throwable ig) {
        }
    }

    private void dump() {
        try {
            File f = new File(ctx.getFilesDir(), "log.txt");
            FileInputStream fis = new FileInputStream(f);
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) > 0) sb.append(new String(buf, 0, n));
            fis.close();
            ContentValues cv = new ContentValues();
            cv.put("_display_name", "壳崩溃-" + System.currentTimeMillis() + ".txt");
            cv.put("mime_type", "text/plain");
            Uri uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri != null) {
                OutputStream os = ctx.getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(sb.toString().getBytes());
                    os.close();
                }
            }
        } catch (Throwable ig) {
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            String msg = "CRASH: " + e.getClass().getName() + " | " + e.getMessage();
            log(msg);
            dump();
            Toast.makeText(ctx, "\u5d29\u6e83: " + e.getClass().getName(), Toast.LENGTH_LONG).show();
            Thread.sleep(2500);
        } catch (Throwable ig) {
        }
        if (prev != null) {
            prev.uncaughtException(t, e);
        }
    }
}
