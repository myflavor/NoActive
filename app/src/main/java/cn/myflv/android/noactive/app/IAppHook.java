package cn.myflv.android.noactive.app;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public interface IAppHook {
    void hook(XC_LoadPackage.LoadPackageParam packageParam);
}
