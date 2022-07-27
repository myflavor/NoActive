package cn.myflv.android.noactive.hook;

import cn.myflv.android.noactive.entity.FieldEnum;
import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ApplicationInfo;
import cn.myflv.android.noactive.server.BroadcastFilter;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.server.ReceiverList;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class BroadcastDeliverHook extends XC_MethodHook {
    private final MemData memData;

    public BroadcastDeliverHook(MemData memData) {
        this.memData = memData;
    }

    @Override
    public void beforeHookedMethod(MethodHookParam param) throws Throwable {
        Object[] args = param.args;
        if (args[1] == null) {
            return;
        }
        BroadcastFilter broadcastFilter = new BroadcastFilter(args[1]);
        ReceiverList receiverList = broadcastFilter.getReceiverList();
        // 如果广播为空就不处理
        if (receiverList == null) {
            return;
        }
        ProcessRecord processRecord = receiverList.getProcessRecord();
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
        // 如果进程名称不是包名开头就跳过
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
        // 暂存
        Object app = processRecord.getProcessRecord();
        param.setObjectExtra(FieldEnum.app, app);
        Log.d(processRecord.getProcessName() + " clear broadcast");
        // 清楚广播
        receiverList.clear();
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);

        // 获取进程
        Object app = param.getObjectExtra(FieldEnum.app);
        if (app == null) {
            return;
        }

        Object[] args = param.args;
        if (args[1] == null) {
            return;
        }
        Object receiverList = XposedHelpers.getObjectField(args[1], FieldEnum.receiverList);
        if (receiverList == null) {
            return;
        }
        // 还原修改
        XposedHelpers.setObjectField(receiverList, FieldEnum.app, app);
    }
}
