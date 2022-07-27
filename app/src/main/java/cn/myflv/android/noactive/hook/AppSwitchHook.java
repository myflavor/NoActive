package cn.myflv.android.noactive.hook;

import android.os.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ActivityManagerService;
import cn.myflv.android.noactive.server.ApplicationInfo;
import cn.myflv.android.noactive.server.ComponentName;
import cn.myflv.android.noactive.server.Event;
import cn.myflv.android.noactive.server.ProcessList;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.utils.FreezeUtils;
import cn.myflv.android.noactive.utils.Log;
import cn.myflv.android.noactive.utils.ThreadUtil;
import de.robv.android.xposed.XC_MethodHook;

public class AppSwitchHook extends XC_MethodHook {
    private final int ACTIVITY_RESUMED;
    private final int ACTIVITY_PAUSED;

    public final static int SIMPLE = 1;
    public final static int DIFFICULT = 2;
    private final int type;

    private final MemData memData;
    private final FreezeUtils freezeUtils;
    private final Map<String, Long> freezerTokenMap = new HashMap<>();

    public AppSwitchHook(ClassLoader classLoader, MemData memData, int type) {
        this.ACTIVITY_RESUMED = Event.ACTIVITY_RESUMED(classLoader);
        this.ACTIVITY_PAUSED = Event.ACTIVITY_PAUSED(classLoader);
        this.memData = memData;
        this.freezeUtils = new FreezeUtils(classLoader);
        this.type = type;
    }

    /**
     * Hook APP 切换事件
     *
     * @param param 方法参数
     * @throws Throwable 异常
     */
    public void beforeHookedMethod(MethodHookParam param) throws Throwable {
        // 开启一个新线程防止避免阻塞主线程
        new Thread(() -> {
            // 获取方法参数
            Object[] args = param.args;
            // 获取切换事件
            int event = (int) args[2];
            // AMS有两个方法，但参数不同
            String packageName = type == SIMPLE ? (String) args[0] : new ComponentName(args[0]).getPackageName();

            // 如果是进入前台
            if (event == ACTIVITY_RESUMED) {
                // 后台APP移除
                memData.getAppBackgroundSet().remove(packageName);
            } else if (event != ACTIVITY_PAUSED) {
                // 不是进入前台或者后台就不处理
                return;
            }

            // 获取AMS
            ActivityManagerService activityManagerService = new ActivityManagerService(param.thisObject);
            // 重要系统APP
            boolean isImportantSystemApp = activityManagerService.isImportantSystemApp(packageName);
            if (isImportantSystemApp) {
                Log.d(packageName + " is important system app");
                return;
            }
            // 系统APP
            boolean isSystem = activityManagerService.isSystem(packageName);
            // 判断是否白名单系统APP
            if (isSystem && !memData.getBlackSystemApps().contains(packageName)) {
                Log.d(packageName + " is white system app");
                return;
            }
            if (event == ACTIVITY_PAUSED) {
                //暂停事件
                onPause(activityManagerService, packageName);
            } else {
                //继续事件
                onResume(activityManagerService, packageName);
            }
        }).start();
    }

    /**
     * 获取目标进程
     *
     * @param activityManagerService AMS
     * @param packageName            包名
     * @return 目标进程列表
     */
    public List<ProcessRecord> getTargetProcessRecords(ActivityManagerService activityManagerService, String packageName) {
        // 从AMS获取进程列表对象
        ProcessList processList = activityManagerService.getProcessList();
        // 从进程列表对象获取所有进程
        List<ProcessRecord> processRecords = processList.getProcessRecords();
        // 存放需要冻结/解冻的 processRecord
        List<ProcessRecord> targetProcessRecords = new ArrayList<>();
        // 对进程列表加锁
        synchronized (processList.getProcessList()) {
            // 遍历进程列表
            for (ProcessRecord processRecord : processRecords) {
                ApplicationInfo applicationInfo = processRecord.getApplicationInfo();
                // 如果包名和事件的包名不同就不处理
                if (!applicationInfo.getPackageName().equals(packageName)) {
                    continue;
                }
                // 获取进程名
                String processName = processRecord.getProcessName();
                // 如果进程名称不是包名开头就跳过
                if (!processName.startsWith(packageName)) {
                    continue;
                }
                // 如果白名单进程包含进程则跳过
                if (memData.getWhiteProcessList().contains(processName)) {
                    Log.d("white process " + processName);
                    continue;
                }
                // 如果白名单APP包含包名并且杀死进程不包含进程名就跳过
                if (memData.getWhiteApps().contains(packageName) && !memData.getKillProcessList().contains(processName)) {
                    Log.d("white app process " + processName);
                    continue;
                }
                // 添加目标进程
                targetProcessRecords.add(processRecord);
            }
        }
        return targetProcessRecords;
    }

    /**
     * APP切换至前台
     *
     * @param packageName            包名
     * @param activityManagerService AMS
     */
    public void onResume(ActivityManagerService activityManagerService, String packageName) {
        List<ProcessRecord> targetProcessRecords = getTargetProcessRecords(activityManagerService, packageName);
        // 如果目标进程为空就不处理
        if (targetProcessRecords.isEmpty()) {
            return;
        }
        Log.d(packageName + " resumed");
        // 遍历目标进程列表
        for (ProcessRecord targetProcessRecord : targetProcessRecords) {
            // 确保APP不在后台
            if (memData.getAppBackgroundSet().contains(packageName)) {
                return;
            }
            // 解冻进程
            freezeUtils.unFreezer(targetProcessRecord);
        }
    }

    /**
     * APP切换至后台
     *
     * @param activityManagerService AMS
     * @param packageName            包名
     */
    public void onPause(ActivityManagerService activityManagerService, String packageName) {
        Log.d(packageName + " paused");
        long token = System.currentTimeMillis();
        setToken(packageName, token);
        // 休眠3s
        ThreadUtil.sleep(3000);
        if (!isCorrectToken(packageName, token)) {
            return;
        }

        // 应用是否前台
        boolean isAppForeground = activityManagerService.isAppForeground(packageName);
        // 如果是前台应用就不处理
        if (isAppForeground) {
            Log.d(packageName + " is in foreground");
            return;
        }

        // 后台应用添加包名
        memData.getAppBackgroundSet().add(packageName);

        List<ProcessRecord> targetProcessRecords = getTargetProcessRecords(activityManagerService, packageName);
        // 如果目标进程为空就不处理
        if (targetProcessRecords.isEmpty()) {
            return;
        }

        // 遍历目标进程
        for (ProcessRecord targetProcessRecord : targetProcessRecords) {
            // 应用又进入前台了
            if (!memData.getAppBackgroundSet().contains(packageName)) {
                // 为保证解冻顺利
                return;
            }
            // 目标进程名
            String processName = targetProcessRecord.getProcessName();
            // 目标进程PID
            int pid = targetProcessRecord.getPid();
            // 如果杀死进程列表包含进程名
            if (memData.getKillProcessList().contains(processName)) {
                Log.d(processName + " kill");
                // 杀死进程
                FreezeUtils.kill(pid);
            } else {
                Log.d(processName + " freezer");
                freezeUtils.freezer(targetProcessRecord);
            }
        }
    }


    public void setToken(String packageName, long token) {
        freezerTokenMap.put(packageName, token);
    }

    public boolean isCorrectToken(String packageName, long value) {
        Long token = freezerTokenMap.get(packageName);
        return token != null && value == token;
    }

}
