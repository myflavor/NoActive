package cn.myflv.android.noactive.server;

import cn.myflv.android.noactive.entity.MethodEnum;
import de.robv.android.xposed.XposedHelpers;

public class Process {
    public final static String Process = "android.os.Process";

    public final static int STOP_V1 = 19;
    public final static int STOP_V2 = 20;
    public final static int CONT = 18;
    public final static int Kill = 9;

    public static Class<?> getProcess(ClassLoader classLoader) {
        return XposedHelpers.findClass(Process, classLoader);
    }

    public static void kill(ClassLoader classLoader, int pid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, Kill);
    }


    public static void stop(ClassLoader classLoader, int pid, int signal) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, signal);
    }


    public static void cont(ClassLoader classLoader, int pid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, CONT);
    }

    public static void freezer(ClassLoader classLoader, int pid, int uid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.setProcessFrozen, pid, uid, true);
    }

    public static void unFreezer(ClassLoader classLoader, int pid, int uid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.setProcessFrozen, pid, uid, false);
    }
}
