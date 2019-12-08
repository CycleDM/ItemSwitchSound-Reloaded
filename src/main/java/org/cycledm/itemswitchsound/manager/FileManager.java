package org.cycledm.itemswitchsound.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.FileUtil;
import org.cycledm.itemswitchsound.Main;

import java.io.File;
import java.io.IOException;

/**
 * @author CycleDM
 */
public class FileManager {
    private static File config = null;
    private static File message = null;
    private static File config_old = null;
    private static File message_old = null;
    private static File pitch_list = null;
    
    public static void saveDefaultFile() {
        File tempPath;
        // pitch list file
        if (!ConfigManager.getPitchFile().exists()) {
            Main.getInstance().getLogger().info("Pitch list file doesn't exist, saving default list file...");
            Main.getInstance().saveResource("pitch_list.yml", true);
        }
        // update
        tempPath = Main.getInstance().getDataFolder();
        pitch_list = new File(tempPath, "pitch_list.yml");
        // config file
        if (!ConfigManager.getConfigFile().exists()) {
            Main.getInstance().getLogger().info("Config file doesn't exist, saving default config...");
            Main.getInstance().saveDefaultConfig();
        }
        // update
        tempPath = Main.getInstance().getDataFolder();
        config = new File(tempPath, "config.yml");
        config_old = new File(tempPath, "config_old.yml");
        
        // language file
        if (!MessageManager.getMessageFile().exists()) {
            tempPath = new File(MessageManager.insidePath + Main.getInstance().getConfig().getString("language") + ".yml");
            Main.getInstance().getLogger().info("Language file doesn't exist, saving " + Main.getInstance().getConfig().getString("language") + " language file...");
            Main.getInstance().saveResource(tempPath.getPath(), true);
        }
        // update
        tempPath = new File(Main.getInstance().getDataFolder() + File.separator + "lang" + File.separator);
        message = new File( tempPath.getPath(), Main.getInstance().getConfig().getString("language") + ".yml");
        message_old = new File( tempPath.getPath(), Main.getInstance().getConfig().getString("language") + "_old.yml");
        
        // player folder
        if (!PlayerManager.playerFolder.exists()) {
            Main.getInstance().getLogger().info("Player folder doesn't exist, create now...");
            PlayerManager.playerFolder.mkdir();
        }
        
    }
    
    public static void doFileCheck(String pluginVersion, String configVersion) {
        Main.getInstance().getLogger().info(MessageManager.getString("update_check.check"));
        if (!pluginVersion.equals(configVersion)) {
            Main.getInstance().getLogger().info(MessageManager.getString("update_check.update"));
            doFileUpdate();
        }
    }
    
    private static void doFileUpdate() {
        //config
        if (config_old.exists()) {
            boolean isDeleted = config_old.delete();
            if (isDeleted) {
                FileUtil.copy(config, config_old);
            }
        } else {
            FileUtil.copy(config, config_old);
        }
        config.delete();
        
        //message
        if (message_old.exists()) {
            boolean isDeleted = message_old.delete();
            if (isDeleted) {
                FileUtil.copy(message, message_old);
            }
        } else {
            FileUtil.copy(message, message_old);
        }
        message.delete();
        
        saveDefaultFile();
    }
    
    /**
     * 用于保存指定的配置文件，失败时返回日志
     */
    public static void saveTargetConfigFile(File targetFile, FileConfiguration targetConfiguration) {
        // v1.0.3添加: 保存文件操作 改为异步进行
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () ->{
            try {
                targetConfiguration.save(targetFile);
            } catch (IOException e) {
                e.printStackTrace();
                // send error message
                System.out.println(ChatColor.DARK_RED + "Error occurred while saving player data!");
            }
        });
    }
}
