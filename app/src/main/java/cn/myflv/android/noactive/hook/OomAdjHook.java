package cn.myflv.android.noactive.hook;

import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ApplicationInfo;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.server.ProcessStateRecord;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodHook;

public class OomAdjHook extends XC_MethodHook {
    private final MemData memData;
    private final int type;
    public final static int Android_S = 1;
    public final static int Android_Q_R = 2;

    public OomAdjHook(MemData memData, int type) {
        this.memData = memData;
        this.type = type;
    }


    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        ProcessRecord processRecord;
        switch (type) {
            case Android_S:
                processRecord = new ProcessStateRecord(param.thisObject).getProcessRecord();
                break;
            case Android_Q_R:
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
        // 如果白名单应用或者进程就不处理
        if (memData.getWhiteApps().contains(packageName) || memData.getWhiteProcessList().contains(processName)) {
            return;
        }

        int curAdj;
        switch (type) {
            case Android_S:
                curAdj = (int) param.args[0];
                break;
            case Android_Q_R:
                curAdj = processRecord.getCurAdj();
                break;
            default:
                return;
        }
        int finalCurlAdj = processName.equals(packageName) ? Math.max(curAdj, 700) : 999;

        switch (type) {
            case Android_S:
                param.args[0] = finalCurlAdj;
                break;
            case Android_Q_R:
                processRecord.setCurAdj(finalCurlAdj);
                break;
            default:
        }
    }
}
