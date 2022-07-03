package cn.myflv.android.noactive.server;

import cn.myflv.android.noactive.entity.FieldEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class BroadcastQueue {
    private final Object broadcastQueue;
    private ActivityManagerService activityManagerService;

    public BroadcastQueue(Object broadcastQueue) {
        this.broadcastQueue = broadcastQueue;
        this.activityManagerService = new ActivityManagerService(XposedHelpers.getObjectField(broadcastQueue, FieldEnum.mService));
    }
}
