//
// Decompiled by Jadx - 917ms
//
package cn.myflv.android.noactive.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import cn.myflv.android.noactive.server.Process;
import cn.myflv.android.noactive.server.ProcessRecord;
import de.robv.android.xposed.XposedHelpers;

public class FreezeUtils {


    private static final String V1_FREEZER_FROZEN_PORCS = "/sys/fs/cgroup/freezer/perf/frozen/cgroup.procs";
    private static final String V1_FREEZER_THAWED_PORCS = "/sys/fs/cgroup/freezer/perf/thawed/cgroup.procs";

    private final ClassLoader classLoader;
    private final int freezerVersion;


    public FreezeUtils(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.freezerVersion = getFreezerVersion(classLoader);
        if (freezerVersion == 2) {
            Process.enableFreezer(classLoader, true);
        }
        if (freezerVersion == 1 || freezerVersion == 2) {
            Log.i("Freezer V" + freezerVersion);
        } else {
            Log.i("Kill -" + freezerVersion);
        }
    }


    public static int getFreezerVersion(ClassLoader classLoader) {
        if (FreezerConfig.isKill19()) {
            return 19;
        }
        if (FreezerConfig.isKill20()) {
            return 20;
        }
        Class<?> CachedAppOptimizer = XposedHelpers.findClass(ClassEnum.CachedAppOptimizer, classLoader);
        boolean isSupportV2 = (boolean) XposedHelpers.callStaticMethod(CachedAppOptimizer, MethodEnum.isFreezerSupported);
        if (isSupportV2) {
            return 2;
        }
        return 1;
    }


    public static List<Integer> getFrozenPids() {
        List<Integer> pids = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(V1_FREEZER_FROZEN_PORCS));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                try {
                    pids.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                }
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return pids;
    }

    public void freezer(ProcessRecord processRecord) {
        if (freezerVersion == 2) {
            Process.freezer(classLoader, processRecord.getPid(), processRecord.getUid());
        } else if (freezerVersion == 1) {
            freezePid(processRecord.getPid());
        } else {
            Process.stop(classLoader, processRecord.getPid(), freezerVersion);
        }

    }

    public void unFreezer(ProcessRecord processRecord) {
        if (freezerVersion == 2) {
            Process.unFreezer(classLoader, processRecord.getPid(), processRecord.getUid());
        } else if (freezerVersion == 1) {
            thawPid(processRecord.getPid());
        } else {
            Process.cont(classLoader, processRecord.getPid());
        }
    }

    public static boolean isFrozonPid(int pid) {
        return getFrozenPids().contains(pid);
    }


    public static void freezePid(int pid) {
        writeNode(V1_FREEZER_FROZEN_PORCS, pid);
    }


    public static void thawPid(int pid) {
        writeNode(V1_FREEZER_THAWED_PORCS, pid);
    }


    private static void writeNode(String path, int val) {
        try {
            PrintWriter writer = new PrintWriter(path);
            writer.write(Integer.toString(val));
            writer.close();
        } catch (IOException ignored) {
        }
    }
}
