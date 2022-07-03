package cn.myflv.android.noactive.server;

import java.util.ArrayList;
import java.util.List;

import cn.myflv.android.noactive.entity.FieldEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ProcessList {
    private final Object ProcessList;
    private final List<ProcessRecord> processRecords = new ArrayList<>();

    public ProcessList(Object processList) {
        this.ProcessList = processList;
        try {
            List<?> processRecordList = (List<?>) XposedHelpers.getObjectField(processList, FieldEnum.mLruProcesses);
            for (Object proc : processRecordList) {
                ProcessRecord processRecord = new ProcessRecord(proc);
                processRecords.add(processRecord);
            }
        } catch (Exception ignored) {

        }
    }
}
