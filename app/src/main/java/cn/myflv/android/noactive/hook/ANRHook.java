package cn.myflv.android.noactive.hook;

import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.server.ActivityManagerService;
import cn.myflv.android.noactive.server.AnrHelper;
import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodReplacement;

public class ANRHook extends XC_MethodReplacement {
    private final ClassLoader classLoader;
    private final MemData memData;

    public ANRHook(ClassLoader classLoader, MemData memData) {
        this.classLoader = classLoader;
        this.memData = memData;
    }

    @Override
    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
        // 获取方法参数
        Object[] args = param.args;
        AnrHelper anrHelper = new AnrHelper(classLoader, param.thisObject);
        // ANR进程为空就不处理
        if (args[0] == null) return null;
        // ANR进程
        ProcessRecord processRecord = new ProcessRecord(args[0]);
        // 是否系统进程
        boolean isSystem = processRecord.getApplicationInfo().isSystem();
        // 进程对应包名
        String packageName = processRecord.getApplicationInfo().getPackageName();
        boolean isNotBlackSystem = !memData.getBlackSystemApps().contains(packageName);

        // 系统应用并且不是系统黑名单
        if ((isSystem && isNotBlackSystem) || processRecord.getUserId() != ActivityManagerService.MAIN_USER) {
            synchronized (anrHelper.getAnrRecords()) {
                // 添加ANR记录
                anrHelper.add(args);
            }
            // 开启ANR消费如果需要
            anrHelper.startAnrConsumerIfNeeded();
        }
        Log.d("Keep " + (processRecord.getProcessName() != null ? processRecord.getProcessName() : packageName));
        // 不处理
        return null;
    }
}
