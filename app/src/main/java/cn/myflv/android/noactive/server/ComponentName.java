package cn.myflv.android.noactive.server;

import cn.myflv.android.noactive.entity.FieldEnum;
import de.robv.android.xposed.XposedHelpers;
import lombok.Data;

@Data
public class ComponentName {
    private final Object componentName;
    private final String packageName;

    public ComponentName(Object componentName) {
        this.componentName = componentName;
        this.packageName = (String) XposedHelpers.getObjectField(componentName, FieldEnum.mPackage);
    }
}
