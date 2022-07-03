package cn.myflv.android.noactive.hook;

import java.util.ArrayList;
import java.util.List;

import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ActivityManagerService;
import cn.myflv.android.noactive.server.ApplicationInfo;
import cn.myflv.android.noactive.server.ComponentName;
import cn.myflv.android.noactive.server.Event;
import cn.myflv.android.noactive.server.Process;
import cn.myflv.android.noactive.server.ProcessList;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.utils.Log;
import cn.myflv.android.noactive.utils.ThreadUtil;
import de.robv.android.xposed.XC_MethodHook;

public class AppSwitchHook extends XC_MethodHook {
    private final int ACTIVITY_RESUMED;
    private final int ACTIVITY_PAUSED;

    public final static int SIMPLE = 1;
    public final static int DIFFICULT = 2;
    private final int type;

    private final ClassLoader classLoader;
    private final MemData memData;


    public AppSwitchHook(ClassLoader classLoader, MemData memData, int type) {
        this.classLoader = classLoader;
        this.ACTIVITY_RESUMED = Event.ACTIVITY_RESUMED(classLoader);
        this.ACTIVITY_PAUSED = Event.ACTIVITY_PAUSED(classLoader);
        this.memData = memData;
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
            // 如果切换事件不是暂停和继续就不处理
            if (event != ACTIVITY_PAUSED && event != ACTIVITY_RESUMED) {
                return;
            }
            // 获取AMS
            ActivityManagerService activityManagerService = new ActivityManagerService(param.thisObject);
            // AMS有两个方法，但参数不同
            String packageName = type == SIMPLE ? (String) args[0] : new ComponentName(args[0]).getPackageName();
            // 获取用户ID
            int userId = (int) args[1];
            List<ProcessRecord> targetProcessRecords = getTargetProcessRecords(activityManagerService, packageName);
            // 如果目标进程为空就不处理
            if (targetProcessRecords.isEmpty()) {
                return;
            }
            if (event == ACTIVITY_PAUSED) {
                //暂停事件
                onPause(activityManagerService, packageName, userId, targetProcessRecords);
            } else {
                //继续事件
                onResume(packageName, targetProcessRecords);
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
                // 如果系统黑名单不包含包名并且是系统应用并且进程名是包名开头(APP调用的WebView是系统APP)
                if (!memData.getBlackSystemApps().contains(packageName) && applicationInfo.isSystem()
                        && processName.startsWith(packageName)) {
                    Log.d(packageName + " is white system app");
                    // 直接返回空列表
                    return new ArrayList<>();
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
     * @param packageName          包名
     * @param targetProcessRecords 目标进程列表
     */
    public void onResume(String packageName, List<ProcessRecord> targetProcessRecords) {
        Log.d(packageName + " resumed");
        // 后台APP移除包名
        memData.getBlackSystemApps().remove(packageName);
        // 遍历目标进程列表
        for (ProcessRecord targetProcessRecord : targetProcessRecords) {
            // 如果白名单包含目标进程就跳过
            if (memData.getWhiteProcessList().contains(targetProcessRecord.getProcessName())) {
                continue;
            }
            // 解冻进程
            Process.unFreezer(classLoader, targetProcessRecord.getPid());
        }
    }

    /**
     * APP切换至后台
     *
     * @param activityManagerService AMS
     * @param packageName            包名
     * @param userId                 用户ID
     * @param targetProcessRecords   目标进程列表
     */
    public void onPause(ActivityManagerService activityManagerService, String packageName, int userId, List<ProcessRecord> targetProcessRecords) {
        Log.d(packageName + " paused");
        // 休眠0.5s
        ThreadUtil.sleep(500);
        // 如果不是白名单应用就调用应用休眠
        if (!memData.getWhiteApps().contains(packageName)) {
            activityManagerService.makePackageIdle(packageName, userId);
        }
        // 休眠2.5s
        ThreadUtil.sleep(2500);
        // 应用是否前台
        boolean isAppForeground = isAppForeground(activityManagerService, targetProcessRecords);
        // 如果是前台应用就不处理
        if (isAppForeground) {
            Log.d(packageName + " is in foreground");
            return;
        }
        // 后台应用添加包名
        memData.getAppBackgroundSet().add(packageName);
        // 遍历目标进程
        for (ProcessRecord targetProcessRecord : targetProcessRecords) {
            // 如果目标进程是系统应用就跳过(WebView)
            if (targetProcessRecord.getApplicationInfo().isSystem()) {
                continue;
            }
            // 目标进程名
            String processName = targetProcessRecord.getProcessName();
            // 目标进程PID
            int pid = targetProcessRecord.getPid();
            // 如果杀死进程列表包含进程名
            if (memData.getKillProcessList().contains(processName)) {
                Log.d(processName + " kill");
                // 杀死进程
                Process.kill(classLoader, pid);
            } else {
                Log.d(processName + " freezer");
                // 冻结进程
                Process.freezer(classLoader, pid);
            }
        }
    }

    /**
     * APP是否前台
     *
     * @param activityManagerService AMS
     * @param targetProcessRecords   目标进程列表
     * @return 是否前台
     */
    public boolean isAppForeground(ActivityManagerService activityManagerService, List<ProcessRecord> targetProcessRecords) {
        for (ProcessRecord targetProcessRecord : targetProcessRecords) {
            if (targetProcessRecord.isNull()) {
                continue;
            }
            boolean appForeground = activityManagerService.isAppForeground(targetProcessRecord.getUid());
            if (appForeground) {
                return true;
            }
        }
        return false;
    }


}
