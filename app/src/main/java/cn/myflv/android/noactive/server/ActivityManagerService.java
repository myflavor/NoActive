package cn.myflv.android.noactive.server;


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

    public ActivityManagerService(Object activityManagerService) {
        this.activityManagerService = activityManagerService;
        this.processList = new ProcessList(XposedHelpers.getObjectField(activityManagerService, FieldEnum.mProcessList));
        this.activeServices = new ActiveServices(XposedHelpers.getObjectField(activityManagerService, FieldEnum.mServices));
    }

    public boolean isAppForeground(int uid) {
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

}
