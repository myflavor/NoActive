package cn.myflv.android.noactive;

import cn.myflv.android.noactive.app.Android;
import cn.myflv.android.noactive.app.PowerKeeper;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam packageParam) throws Throwable {
        if (packageParam.packageName.equals("android")) {
            Android android = new Android();
            android.hook(packageParam);
            return;
        }
        // 禁用 millet
        if (packageParam.packageName.equals("com.miui.powerkeeper")) {
            PowerKeeper powerKeeper = new PowerKeeper();
            powerKeeper.hook(packageParam);
        }

    }

}
