package cn.myflv.android.noactive.app;

import android.content.Context;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import cn.myflv.android.noactive.hook.MilletHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PowerKeeper implements IAppHook {
    @Override
    public void hook(XC_LoadPackage.LoadPackageParam packageParam) {
        try {
            XposedHelpers.findAndHookMethod(ClassEnum.PowerStateMachine, packageParam.classLoader, MethodEnum.clearAppWhenScreenOffTimeOut, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod(ClassEnum.PowerStateMachine, packageParam.classLoader, MethodEnum.clearAppWhenScreenOffTimeOutInNight, XC_MethodReplacement.DO_NOTHING);
            XposedHelpers.findAndHookMethod(ClassEnum.PowerStateMachine, packageParam.classLoader, MethodEnum.clearUnactiveApps, Context.class, XC_MethodReplacement.DO_NOTHING);
            XposedBridge.log("NoActive(info) -> Disable MIUI clearApp");
        } catch (Throwable throwable) {
            XposedBridge.log("NoActive(error) -> Disable MIUI clearApp failed: " + throwable.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(ClassEnum.MilletConfig, packageParam.classLoader, MethodEnum.getEnable, Context.class, new MilletHook());
            XposedBridge.log("NoActive(info) -> Disable millet");
        } catch (Throwable throwable) {
            XposedBridge.log("NoActive(error) -> Disable millet failed: " + throwable.getMessage());
        }
    }
}
