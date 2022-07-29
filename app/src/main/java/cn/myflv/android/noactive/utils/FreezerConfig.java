package cn.myflv.android.noactive.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cn.myflv.android.noactive.entity.ClassEnum;
import cn.myflv.android.noactive.entity.MethodEnum;
import de.robv.android.xposed.XposedHelpers;

public class FreezerConfig {
    public final static String ConfigDir = "/data/system/NoActive";
    public final static String whiteAppConfig = "whiteApp.conf";
    public final static String blackSystemAppConfig = "blackSystemApp.conf";
    public final static String whiteProcessConfig = "whiteProcess.conf";
    public final static String killProcessConfig = "killProcess.conf";
    public final static String disableOOM = "disable.oom";
    public final static String kill19 = "kill.19";
    public final static String kill20 = "kill.20";
    public final static String freezerV1 = "freezer.v1";
    public final static String freezerV2 = "freezer.v2";
    public final static String colorOs = "color.os";

    public static boolean isConfigOn(String configName) {
        File config = new File(ConfigDir, configName);
        return config.exists();
    }

    public static int getKillSignal() {
        if (isConfigOn(kill19)) {
            return 19;
        }
        if (isConfigOn(kill20)) {
            return 20;
        }
        return 19;
    }


    public static int getFreezerVersion(ClassLoader classLoader) {
        if (isConfigOn(freezerV2)) {
            return 2;
        }
        if (isConfigOn(freezerV1)) {
            return 1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Class<?> CachedAppOptimizer = XposedHelpers.findClass(ClassEnum.CachedAppOptimizer, classLoader);
            boolean isSupportV2 = (boolean) XposedHelpers.callStaticMethod(CachedAppOptimizer, MethodEnum.isFreezerSupported);
            if (isSupportV2) {
                return 2;
            }
        }
        return 1;
    }


    public static boolean isUseKill() {
        return isConfigOn(kill19) || isConfigOn(kill20);
    }

    public static boolean isColorOs() {
        return isConfigOn(colorOs);
    }


    public static void checkAndInit() {
        File configDir = new File(ConfigDir);
        File whiteApp = new File(ConfigDir, whiteAppConfig);
        File whiteProcess = new File(ConfigDir, whiteProcessConfig);
        File killProcess = new File(ConfigDir, killProcessConfig);
        File blackSystemApp = new File(ConfigDir, blackSystemAppConfig);
        if (!configDir.exists()) {
            boolean mkdir = configDir.mkdir();
            if (!mkdir) return;
            Log.i("Init config dir");
        }

        if (!whiteApp.exists()) {
            createFile(whiteApp);
            Log.i("Init white app conf");
        }

        if (!whiteProcess.exists()) {
            createFile(whiteProcess);
            Log.i("Init white process conf");
        }
        if (!killProcess.exists()) {
            createFile(killProcess);
            Log.i("Init kill process conf");
        }

        if (!blackSystemApp.exists()) {
            createFile(blackSystemApp);
            Log.i("Init black system app conf");
        }
    }


    public static Set<String> get(String name) {
        Set<String> set = new HashSet<>();
        try {
            File file = new File(ConfigDir, name);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if ("".equals(line.trim())) continue;
                if (line.startsWith("#")) continue;
                set.add(line.trim());
                Log.i(name.replace(".conf", "") + " add " + line);
            }
            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(name + " file not found");
        } catch (IOException ioException) {
            Log.e(name + " file read filed");
        }
        return set;
    }

    public static void createFile(File file) {
        try {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                throw new IOException();
            }
        } catch (IOException e) {
            Log.e(file.getName() + " file create filed");
        }
    }
}
