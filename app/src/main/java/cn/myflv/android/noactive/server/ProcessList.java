package cn.myflv.android.noactive.server;

import java.util.ArrayList;
import java.util.List;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.FieldEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ProcessList {
    private final Object processList;
    private final List<ProcessRecord> processRecords = new ArrayList<>();

    public ProcessList(Object processList) {
        this.processList = processList;
        try {
            List<?> processRecordList = (List<?>) XposedHelpers.getObjectField(processList, FieldEnum.mLruProcesses);
            for (Object proc : processRecordList) {
                ProcessRecord processRecord = new ProcessRecord(proc);
                processRecords.add(processRecord);
            }
        } catch (Exception ignored) {

        }
    }

    public static void setOomAdj(ClassLoader classLoader,int pid, int uid, int oomAdj) {
        Class<?> ProcessList = XposedHelpers.findClass(ClassEnum.ProcessList, classLoader);
        XposedHelpers.callStaticMethod(ProcessList, MethodEnum.setOomAdj,pid,uid,oomAdj);
    }
}
