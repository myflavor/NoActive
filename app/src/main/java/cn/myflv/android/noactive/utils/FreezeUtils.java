//
// Decompiled by Jadx - 917ms
//
package cn.myflv.android.noactive.utils;

import android.os.Build;

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


    private static final int FREEZE_ACTION = 1;
    private static final int UNFREEZE_ACTION = 0;


    private static final String V1_FREEZER_FROZEN_PORCS = "/sys/fs/cgroup/freezer/perf/frozen/cgroup.procs";
    private static final String V1_FREEZER_THAWED_PORCS = "/sys/fs/cgroup/freezer/perf/thawed/cgroup.procs";

    private final ClassLoader classLoader;
    private final int freezerVersion;
    private final int killSignal;
    private final boolean useKill;


    public FreezeUtils(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.freezerVersion = FreezerConfig.getFreezerVersion(classLoader);
        this.killSignal = FreezerConfig.getKillSignal();
        this.useKill = FreezerConfig.isUseKill();
        if (useKill) {
            Log.i("Kill -" + killSignal);
        } else {
            Log.i("Freezer V" + freezerVersion);
        }
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
        if (useKill) {
            Process.stop(classLoader, processRecord.getPid(), killSignal);
        } else {
            if (freezerVersion == 2) {
                freezePid(processRecord.getPid(), processRecord.getUid());
            } else {
                freezePid(processRecord.getPid());
            }
        }
    }

    public void unFreezer(ProcessRecord processRecord) {
        if (useKill) {
            Process.cont(classLoader, processRecord.getPid());
        } else {
            if (freezerVersion == 2) {
                thawPid(processRecord.getPid(), processRecord.getUid());
            } else {
                thawPid(processRecord.getPid());
            }
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


    private static void setFreezeAction(int pid, int uid, boolean action) {
        String path = "/sys/fs/cgroup/uid_" + uid + "/pid_" + pid + "/cgroup.freeze";
        try {
            PrintWriter writer = new PrintWriter(path);
            if (action) {
                writer.write(Integer.toString(FREEZE_ACTION));
            } else {
                writer.write(Integer.toString(UNFREEZE_ACTION));
            }
            writer.close();
        } catch (IOException ignored) {
        }
    }

    public static void thawPid(int pid, int uid) {
        setFreezeAction(pid, uid, false);
    }


    public static void freezePid(int pid, int uid) {
        setFreezeAction(pid, uid, true);
    }
}
