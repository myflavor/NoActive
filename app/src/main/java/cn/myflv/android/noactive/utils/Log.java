package cn.myflv.android.noactive.utils;

import java.io.File;

import de.robv.android.xposed.XposedBridge;

public class Log {
    private final static String TAG = "NoActive";
    private static final boolean isDebug;
    private final static String ERROR = "error";
    private final static String WARN = "warn";
    private final static String INFO = "info";
    private final static String DEBUG = "debug";

    static {
        File config = new File(FreezerConfig.ConfigDir, "debug");
        isDebug = config.exists();
        i("Debug " + (isDebug ? "on" : "off"));
    }

    public static void d(String msg) {
        if (isDebug) {
            unify(DEBUG, msg);
        }
    }

    public static void i(String msg) {
        unify(INFO, msg);
    }

    public static void w(String msg) {
        unify(WARN, msg);
    }

    public static void e(String msg) {
        unify(ERROR, msg);
    }


    public static void e(String msg, Throwable throwable) {
        unify(ERROR, msg + ": " + throwable.getMessage());
    }

    public static void unify(String level, String msg) {
        XposedBridge.log(TAG + "(" + level + ") -> " + msg);
    }
}
