package cn.myflv.android.noactive.app;

import android.content.Intent;
import android.os.Build;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.entity.MethodEnum;
import cn.myflv.android.noactive.hook.ANRHook;
import cn.myflv.android.noactive.hook.AppSwitchHook;
import cn.myflv.android.noactive.hook.BroadcastDeliverHook;
import cn.myflv.android.noactive.hook.CacheFreezerHook;
import cn.myflv.android.noactive.hook.OomAdjHook;
import cn.myflv.android.noactive.utils.FreezerConfig;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Android implements IAppHook {
    @Override
    public void hook(XC_LoadPackage.LoadPackageParam packageParam) {
        // 类加载器
        ClassLoader classLoader = packageParam.classLoader;

        // 加载内存配置
        MemData memData = new MemData();

        // 打印设备厂商
        Log.i(Build.MANUFACTURER + " device");

        // 禁用暂停执行已缓存
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            XposedHelpers.findAndHookMethod(ClassEnum.CachedAppOptimizer, classLoader, MethodEnum.useFreezer, new CacheFreezerHook());
            Log.i("Disable cache freezer");
        }

        // Hook 切换事件
        if (Build.MANUFACTURER.equals("samsung")) {
            XposedHelpers.findAndHookMethod(ClassEnum.ActivityManagerService, classLoader,
                    MethodEnum.updateActivityUsageStats,
                    ClassEnum.ComponentName, int.class, int.class,
                    ClassEnum.IBinder, ClassEnum.ComponentName, Intent.class, new AppSwitchHook(classLoader, memData, AppSwitchHook.DIFFICULT));
        } else {
            XposedHelpers.findAndHookMethod(ClassEnum.ActivityManagerService, classLoader,
                    MethodEnum.updateActivityUsageStats,
                    ClassEnum.ComponentName, int.class, int.class,
                    ClassEnum.IBinder, ClassEnum.ComponentName, new AppSwitchHook(classLoader, memData, AppSwitchHook.DIFFICULT));
        }

        // Hook 广播分发
        XposedHelpers.findAndHookMethod(ClassEnum.BroadcastQueue, classLoader, MethodEnum.deliverToRegisteredReceiverLocked,
                ClassEnum.BroadcastRecord,
                ClassEnum.BroadcastFilter, boolean.class, int.class, new BroadcastDeliverHook(memData));

        // Hook oom_adj
        if (!FreezerConfig.isConfigOn(FreezerConfig.disableOOM)) {
            boolean colorOs = FreezerConfig.isColorOs();
            if (!colorOs && (Build.MANUFACTURER.equals("OPPO") || Build.MANUFACTURER.equals("OnePlus"))) {
                Log.w("If you are using ColorOS");
                Log.w("You can create file color.os");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (colorOs) {
                    Log.i("Hello ColorOS");
                    XposedHelpers.findAndHookMethod(ClassEnum.OomAdjuster, classLoader, MethodEnum.computeOomAdjLSP, ClassEnum.ProcessRecord, int.class, ClassEnum.ProcessRecord, boolean.class, long.class, boolean.class, boolean.class, new OomAdjHook(classLoader, memData, OomAdjHook.Color));
                } else {
                    XposedHelpers.findAndHookMethod(ClassEnum.ProcessStateRecord, classLoader, MethodEnum.setCurAdj, int.class, new OomAdjHook(classLoader, memData, OomAdjHook.Android_S));
                }
                Log.i("Auto lmk");
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                XposedHelpers.findAndHookMethod(ClassEnum.OomAdjuster, classLoader, MethodEnum.applyOomAdjLocked, ClassEnum.ProcessRecord, boolean.class, long.class, long.class, new OomAdjHook(classLoader, memData, OomAdjHook.Android_Q_R));
                Log.i("Auto lmk");
            }
        }

        // Hook ANR
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            XposedHelpers.findAndHookMethod(ClassEnum.AnrHelper, classLoader, MethodEnum.appNotResponding,
                    ClassEnum.ProcessRecord,
                    String.class,
                    ClassEnum.ApplicationInfo,
                    String.class,
                    ClassEnum.WindowProcessController,
                    boolean.class,
                    String.class, new ANRHook(classLoader, memData));
            Log.i("Auto keep process");
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            XposedHelpers.findAndHookMethod(ClassEnum.ProcessRecord, classLoader, MethodEnum.appNotResponding,
                    String.class, ClassEnum.ApplicationInfo, String.class, ClassEnum.WindowProcessController, boolean.class, String.class, XC_MethodReplacement.DO_NOTHING);
            Log.i("Android Q");
            Log.i("Force keep process");
        } else {
            XposedHelpers.findAndHookMethod(ClassEnum.AppErrors, classLoader, MethodEnum.appNotResponding,
                    ClassEnum.ProcessRecord, ClassEnum.ActivityRecord, ClassEnum.ActivityRecord, boolean.class, String.class,
                    XC_MethodReplacement.DO_NOTHING
            );
            Log.i("Android N-P");
            Log.i("Force keep process");
        }

        Log.i("Load success");
    }
}
