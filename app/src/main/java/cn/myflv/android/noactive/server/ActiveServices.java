package cn.myflv.android.noactive.server;

import cn.myflv.android.noactive.entity.FieldEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ActiveServices {
    private final Object activeServices;

    public ActiveServices(Object activeServices) {
        this.activeServices = activeServices;
    }

    public void killServicesLocked(ProcessRecord processRecord) {
        XposedHelpers.callMethod(activeServices, MethodEnum.killServicesLocked, processRecord.getProcessRecord(), false);
    }
}
