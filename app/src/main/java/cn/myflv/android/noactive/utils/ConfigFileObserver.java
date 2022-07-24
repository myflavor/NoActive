package cn.myflv.android.noactive.utils;

import android.os.FileObserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.myflv.android.noactive.entity.MemData;

public class ConfigFileObserver extends FileObserver {
    private final MemData memData;
    private final String[] files = {FreezerConfig.whiteAppConfig, FreezerConfig.whiteProcessConfig,
            FreezerConfig.killProcessConfig, FreezerConfig.blackSystemAppConfig};

    public ConfigFileObserver(MemData memData) {
        super(FreezerConfig.ConfigDir);
        this.memData = memData;
        FreezerConfig.checkAndInit();
        reload();
    }

    @Override
    public void startWatching() {
        super.startWatching();
        for (String file : files) {
            Log.d("Start monitor " + file);
        }
    }

    @Override
    public void onEvent(int event, String path) {
        int e = event & ALL_EVENTS;
        switch (e) {
            case DELETE:
            case DELETE_SELF:
                FreezerConfig.checkAndInit();
                break;
            case MODIFY:
            case MOVE_SELF:
                ThreadUtil.sleep(2000);
                reload();
        }
    }

    public void reload() {
        for (String file : files) {
            Log.d("Reload " + file);
            Set<String> newConfig = new HashSet<>(FreezerConfig.get(file));
            switch (file) {
                case FreezerConfig.whiteAppConfig:
                    memData.setWhiteApps(newConfig);
                    break;
                case FreezerConfig.whiteProcessConfig:
                    memData.setWhiteProcessList(newConfig);
                    break;
                case FreezerConfig.killProcessConfig:
                    memData.setKillProcessList(newConfig);
                    break;
                case FreezerConfig.blackSystemAppConfig:
                    memData.setBlackSystemApps(newConfig);
                    break;
            }
        }
    }
}
