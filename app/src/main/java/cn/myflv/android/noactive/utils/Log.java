package cn.myflv.android.noactive.utils;

import java.io.File;

import de.robv.android.xposed.XposedBridge;

public class Log {
    private final static String TAG = "NoActive";
    private static final boolean DEBUG;

    static {
        File config = new File(FreezerConfig.ConfigDir, "debug");
        DEBUG = config.exists();
        i("Debug " + (DEBUG ? "on" : "off"));
    }

    public static void d(String msg) {
        if (DEBUG) {
            XposedBridge.log(TAG + " -> " + msg);
        }
    }

    public static void i(String msg) {
        XposedBridge.log(TAG + " -> " + msg);
    }
}
