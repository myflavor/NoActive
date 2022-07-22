package cn.myflv.android.noactive.hook;

import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.utils.FreezeUtils;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodHook;

public class ProcessKilledHook extends XC_MethodHook {
    public ProcessKilledHook() {
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        boolean killed = (boolean) param.args[0];
        if (!killed) {
            return;
        }
        ProcessRecord processRecord = new ProcessRecord(param.thisObject);
        Log.d(processRecord.getProcessName() + " was killed");
        boolean isFrozonPid = FreezeUtils.isFrozonPid(processRecord.getPid());
        if (!isFrozonPid) {
            return;
        }
        FreezeUtils.thawPid(processRecord.getPid());
        Log.d(processRecord.getPid() + "was thawed");
    }
}
