package cn.myflv.android.noactive.server;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.lang.reflect.InvocationTargetException;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.FieldEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import cn.myflv.android.noactive.utils.Log;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ActivityManagerService {
    private final Object activityManagerService;
    private final ProcessList processList;
    private final ActiveServices activeServices;
    private final Context context;

    public ActivityManagerService(Object activityManagerService) {
        this.activityManagerService = activityManagerService;
        this.processList = new ProcessList(XposedHelpers.getObjectField(activityManagerService, FieldEnum.mProcessList));
        this.activeServices = new ActiveServices(XposedHelpers.getObjectField(activityManagerService, FieldEnum.mServices));
        this.context = (Context) XposedHelpers.getObjectField(activityManagerService, FieldEnum.mContext);
    }

    public boolean isAppForeground(String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName);
        if (applicationInfo == null) {
            return true;
        }
        int uid = applicationInfo.uid;
        Class<?> clazz = activityManagerService.getClass();
        while (clazz != null && !clazz.getName().equals(Object.class.getName()) && !clazz.getName().equals(ClassEnum.ActivityManagerService)) {
            clazz = clazz.getSuperclass();
        }
        if (clazz == null || !clazz.getName().equals(ClassEnum.ActivityManagerService)) {
            Log.d("super activityManagerService is not found");
            return true;
        }
        try {
            return (boolean) XposedHelpers.findMethodBestMatch(clazz, MethodEnum.isAppForeground, uid).invoke(activityManagerService, uid);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.d("call isAppForeground method error");
        }
        return true;
    }

    public boolean isSystem(String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName);
        if (applicationInfo == null) {
            return true;
        }
        return (applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
    }

    public boolean isImportantSystemApp(String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName);
        if (applicationInfo == null) {
            return true;
        }
        return applicationInfo.uid < 10000;
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(packageName + " not found");
        }
        return null;
    }

}
