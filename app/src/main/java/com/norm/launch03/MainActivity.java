package com.norm.launch03;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final String GAME_PKG = "com.shizi.tool.p3";

    private void log(String s) {
        try {
            File f = new File(getFilesDir(), "log.txt");
            FileOutputStream fo = new FileOutputStream(f, true);
            fo.write((s + "\n").getBytes());
            fo.close();
        } catch (Throwable ig) {
        }
        try {
            File dir = getExternalFilesDir(null);
            if (dir != null) {
                File f2 = new File(dir, "log.txt");
                FileOutputStream fo2 = new FileOutputStream(f2, true);
                fo2.write((s + "\n").getBytes());
                fo2.close();
            }
        } catch (Throwable ig) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        log("P0 enter");

        TextView tv = new TextView(this);
        tv.setTextSize(17);
        tv.setPadding(72, 96, 72, 72);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);

        Intent gi = getPackageManager().getLaunchIntentForPackage(GAME_PKG);
        if (gi != null) {
            log("P-launch");
            tv.setText("找到本体，正在启动…");
            try {
                startActivity(gi);
                finish();
                return;
            } catch (Throwable t) {
                log("launch err: " + t);
                tv.setText("启动失败: " + t);
            }
        } else {
            log("P-nogame");
            tv.setText("外壳已存活（H2 测试）\n本机没有装游戏本体。\n看到这行字，说明外壳能正常跑。");
        }
        Toast.makeText(this, "\u5916\u58f3\u5df2\u5b58\u6d3b\uff08H2\uff09", Toast.LENGTH_LONG).show();
    }
}
