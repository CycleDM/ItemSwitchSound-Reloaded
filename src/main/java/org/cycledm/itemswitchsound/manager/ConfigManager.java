package org.cycledm.itemswitchsound.manager;

import org.cycledm.itemswitchsound.Main;

import java.io.File;

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
    
}
