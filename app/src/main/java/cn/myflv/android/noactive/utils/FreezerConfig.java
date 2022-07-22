package cn.myflv.android.noactive.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FreezerConfig {
    public final static String ConfigDir = "/data/system/NoActive";
    public final static String whiteAppConfig = "whiteApp.conf";
    public final static String blackSystemAppConfig = "blackSystemApp.conf";
    public final static String whiteProcessConfig = "whiteProcess.conf";
    public final static String killProcessConfig = "killProcess.conf";
    public final static String disableOOM = "disable.oom";
    public final static String kill19 = "kill.19";
    public final static String kill20 = "kill.20";

    public static boolean isKill19() {
        File config = new File(ConfigDir,kill19);
        return config.exists();
    }

    public static boolean isKill20() {
        File config = new File(ConfigDir,kill20);
        return config.exists();
    }


    public static boolean isDisableOOM() {
        File config = new File(ConfigDir,disableOOM);
        return config.exists();
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
            Log.i("init config dir");
        }

        if (!whiteApp.exists()) {
            createFile(whiteApp);
            Log.i("init white app conf");
        }

        if (!whiteProcess.exists()) {
            createFile(whiteProcess);
            Log.i("init white process conf");
        }
        if (!killProcess.exists()) {
            createFile(killProcess);
            Log.i("init kill process conf");
        }

        if (!blackSystemApp.exists()) {
            createFile(blackSystemApp);
            Log.i("init black system app conf");
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
            Log.i(name + " file not found");
        } catch (IOException ioException) {
            Log.i(name + " file read filed");
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
            Log.i(file.getName() + " file create filed");
        }
    }
}
