package cn.myflv.android.noactive.hook;

import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ApplicationInfo;
import cn.myflv.android.noactive.server.ProcessList;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.server.ProcessStateRecord;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodHook;

public class OomAdjHook extends XC_MethodHook {
    private final ClassLoader classLoader;
    private final MemData memData;
    private final int type;
    public final static int Android_S = 1;
    public final static int Android_Q_R = 2;
    public final static int Color = 3;

    public OomAdjHook(ClassLoader classLoader, MemData memData, int type) {
        this.classLoader = classLoader;
        this.memData = memData;
        this.type = type;
    }


    public void computeOomAdj(MethodHookParam param) {
        ProcessRecord processRecord;
        switch (type) {
            case Android_S:
                processRecord = new ProcessStateRecord(param.thisObject).getProcessRecord();
                break;
            case Android_Q_R:
            case Color:
                processRecord = new ProcessRecord(param.args[0]);
                break;
            default:
                return;
        }
        // 如果进程或者应用信息为空就不处理
        if (processRecord == null || processRecord.getApplicationInfo() == null) {
            return;
        }
        ApplicationInfo applicationInfo = processRecord.getApplicationInfo();
        String packageName = processRecord.getApplicationInfo().getPackageName();
        // 如果包名为空就不处理(猜测系统进程可能为空)
        if (packageName == null) {
            return;
        }
        String processName = processRecord.getProcessName();
        // 如果进程名称等于包名就跳过
        if (!processName.startsWith(packageName)) {
            return;
        }
        // 如果是系统应用并且不是系统黑名单就不处理
        if (applicationInfo.getUid() < 10000 || (applicationInfo.isSystem() && !memData.getBlackSystemApps().contains(packageName))) {
            return;
        }
        // 如果是前台应用就不处理
        if (!memData.getAppBackgroundSet().contains(packageName)) {
            return;
        }
        int finalCurlAdj;

        // 如果白名单应用或者进程就不处理
        if (memData.getWhiteApps().contains(packageName) || memData.getWhiteProcessList().contains(processName)) {
            finalCurlAdj = processName.equals(packageName) ? 500 : 700;
        } else {
            int curAdj = processName.equals(packageName) ? 700 : 900;
            finalCurlAdj = curAdj + memData.getBackgroundIndex(packageName);
        }

        Log.d(processName + " -> " + finalCurlAdj);


        switch (type) {
            case Android_S:
                param.args[0] = finalCurlAdj;
                break;
            case Android_Q_R:
                processRecord.setCurAdj(finalCurlAdj);
                break;
            case Color:
                ProcessList.setOomAdj(classLoader, processRecord.getPid(), processRecord.getUid(), finalCurlAdj);
                break;
            default:
        }

    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        if (type != Color) {
            computeOomAdj(param);
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        if (type == Color) {
            computeOomAdj(param);
        }
    }
}
