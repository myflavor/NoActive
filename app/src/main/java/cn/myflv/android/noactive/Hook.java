package cn.myflv.android.noactive;

import android.os.Build;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.MemData;
import cn.myflv.android.noactive.entity.MethodEnum;
import cn.myflv.android.noactive.hook.ANRHook;
import cn.myflv.android.noactive.hook.OomAdjHook;
import cn.myflv.android.noactive.hook.AppSwitchHook;
import cn.myflv.android.noactive.hook.BroadcastDeliverHook;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    private final MemData memData = new MemData();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam packageParam) throws Throwable {
        if (!packageParam.packageName.equals("android")) return;
        Log.i("Load success");
        ClassLoader classLoader = packageParam.classLoader;

        XposedHelpers.findAndHookMethod(ClassEnum.ActivityManagerService, classLoader,
                MethodEnum.updateActivityUsageStats,
                ClassEnum.ComponentName, int.class, int.class,
                ClassEnum.IBinder, ClassEnum.ComponentName, new AppSwitchHook(classLoader, memData, AppSwitchHook.DIFFICULT));

        XposedHelpers.findAndHookMethod(ClassEnum.BroadcastQueue, classLoader, MethodEnum.deliverToRegisteredReceiverLocked,
                ClassEnum.BroadcastRecord,
                ClassEnum.BroadcastFilter, boolean.class, int.class, new BroadcastDeliverHook(memData));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i("Auto lmk");
            XposedHelpers.findAndHookMethod(ClassEnum.ProcessStateRecord, classLoader, MethodEnum.setCurAdj, int.class, new OomAdjHook(memData,OomAdjHook.Android_S));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            Log.i("Auto lmk");
            XposedHelpers.findAndHookMethod(ClassEnum.OomAdjuster, classLoader, MethodEnum.applyOomAdjLocked, ClassEnum.ProcessRecord, boolean.class, long.class, long.class, new OomAdjHook(memData,OomAdjHook.Android_Q_R));
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Log.i("Auto keep process");
            XposedHelpers.findAndHookMethod(ClassEnum.AnrHelper, classLoader, MethodEnum.appNotResponding,
                    ClassEnum.ProcessRecord,
                    String.class,
                    ClassEnum.ApplicationInfo,
                    String.class,
                    ClassEnum.WindowProcessController,
                    boolean.class,
                    String.class, new ANRHook(classLoader, memData));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            Log.i("Android Q");
            Log.i("Force keep process");
            XposedHelpers.findAndHookMethod(ClassEnum.ProcessRecord, classLoader, MethodEnum.appNotResponding,
                    String.class, ClassEnum.ApplicationInfo, String.class, ClassEnum.WindowProcessController, boolean.class, String.class, XC_MethodReplacement.DO_NOTHING);

        } else {
            Log.i("Android N-P");
            Log.i("Force keep process");
            XposedHelpers.findAndHookMethod(ClassEnum.AppErrors, classLoader, MethodEnum.appNotResponding,
                    ClassEnum.ProcessRecord, ClassEnum.ActivityRecord, ClassEnum.ActivityRecord, boolean.class, String.class,
                    XC_MethodReplacement.DO_NOTHING

            );
        }

    }

}
