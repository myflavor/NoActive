package cn.myflv.android.noactive.entity;

import android.os.FileObserver;

import java.util.HashSet;
import java.util.Set;

import cn.myflv.android.noactive.utils.ConfigFileObserver;
import lombok.Data;

@Data
public class MemData {
    private Set<String> whiteApps = new HashSet<>();
    private Set<String> blackSystemApps = new HashSet<>();
    private Set<String> whiteProcessList = new HashSet<>();
    private Set<String> killProcessList = new HashSet<>();
    private Set<String> appBackgroundSet = new HashSet<>();
    private final FileObserver fileObserver = new ConfigFileObserver(this);

    public MemData() {
        fileObserver.startWatching();
    }

}
