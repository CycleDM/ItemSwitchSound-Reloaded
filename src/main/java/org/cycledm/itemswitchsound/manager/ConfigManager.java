package org.cycledm.itemswitchsound.manager;

import org.cycledm.itemswitchsound.Main;

import java.io.File;
import java.util.Objects;
import java.util.Set;

/**
 * @author CycleDM
 */
public class ConfigManager {
    public static File getConfigFile() {
        return new File(Main.getInstance().getDataFolder(), "config.yml");
    }
    
    public static File getPitchFile() {
        return new File(Main.getInstance().getDataFolder(), "pitch_list.yml");
    }
    
    public static Double[] getDefaultPitchValues() {
        int i = 0;
        Set<String> keys = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("pitch")).getKeys(false);
        Double[] defaultPitch = new Double[keys.size()];
        for (String s : keys) {
            String temp = Main.getInstance().getConfig().getString("pitch." + s);
            defaultPitch[i] = Main.pitchList.get(temp);
            i++;
        }
        return defaultPitch;
    }
}
