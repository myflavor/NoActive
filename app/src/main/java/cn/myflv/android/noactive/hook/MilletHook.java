package cn.myflv.android.noactive.hook;

import de.robv.android.xposed.XC_MethodReplacement;

public class MilletHook extends XC_MethodReplacement {
    @Override
    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
        return false;
    }
}
