package cn.myflv.android.noactive.hook;

import android.os.Process;

import cn.myflv.android.noactive.server.ProcessRecord;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XC_MethodHook;

public class TestHook extends XC_MethodHook {
    private final String TAG;

    public TestHook(String TAG) {
        this.TAG = TAG;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Log.d(TAG);
    }
}
