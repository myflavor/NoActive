package cn.myflv.android.noactive.server;

import java.util.Collection;

import cn.myflv.android.noactive.entity.MethodEnum;
import de.robv.android.xposed.XposedHelpers;

public class Process {
    public final static String Process = "android.os.Process";

    public final static int Freezer = 20;
    public final static int UnFreezer = 18;
    public final static int Kill = 9;

    public static Class<?> getProcess(ClassLoader classLoader) {
        return XposedHelpers.findClass(Process, classLoader);
    }

    public static void kill(ClassLoader classLoader, int pid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, Kill);
    }


    public static void freezer(ClassLoader classLoader, int pid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, Freezer);
    }


    public static void unFreezer(ClassLoader classLoader, int pid) {
        Class<?> Process = getProcess(classLoader);
        XposedHelpers.callStaticMethod(Process, MethodEnum.sendSignal, pid, UnFreezer);
    }

}
