package cn.myflv.android.noactive.utils;

public class ThreadUtil {
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Log.w("Thread sleep failed");
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Log.w("Thread sleep failed");
        }
    }
}
