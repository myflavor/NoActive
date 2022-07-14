package cn.myflv.android.noactive.server;

import cn.myflv.android.noactive.entity.FieldEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ProcessStateRecord {
    private final Object processStateRecord;
    private final ProcessRecord processRecord;

    public ProcessStateRecord(Object processStateRecord) {
        this.processStateRecord = processStateRecord;
        this.processRecord = new ProcessRecord(XposedHelpers.getObjectField(processStateRecord, FieldEnum.mApp));
    }
}
