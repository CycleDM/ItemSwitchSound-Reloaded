package org.cycledm.itemswitchsound;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.cycledm.itemswitchsound.command.AdminCommand;
import org.cycledm.itemswitchsound.command.BasicCommand;
import org.cycledm.itemswitchsound.command.TableCompleter;
import org.cycledm.itemswitchsound.listener.PlayerListener;
import org.cycledm.itemswitchsound.manager.ConfigManager;
import org.cycledm.itemswitchsound.manager.FileManager;
import org.cycledm.itemswitchsound.manager.MessageManager;
import org.cycledm.itemswitchsound.manager.PlayerManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author CycleDM
 */
public final class Main extends JavaPlugin {
    public static Main instance;
    
    public static Main getInstance() {
        return instance;
    }
    
    public static String[] soundList = {};
    public static Map<String, Double> pitchList = new HashMap<>();
    public static String[] volumeList = {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"};
    
    /**
     * 获取所有可用的声音
     */
    private void getSoundList() {
        for (Sound s : Sound.values()) {
            soundList = Arrays.copyOf(soundList, soundList.length + 1);
            soundList[soundList.length - 1] = s.toString();
        }
    }
    
    /**
     * 从pitch list文件中获取所有可用的音高
     */
    private void getPitchList() {
        File listFile = ConfigManager.getPitchFile();
        FileConfiguration listConfig = YamlConfiguration.loadConfiguration(listFile);
        for (String s : listConfig.getKeys(false)) {
            pitchList.put(s, listConfig.getDouble(s));
        }
    }
    
    /**
     * 检查指定的（离线）玩家是否存在
     */
    public OfflinePlayer checkPlayer(String playerName) {
        OfflinePlayer result = null;
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            if (Objects.equals(p.getName(), playerName)) {
                result = p;
                break;
            }
        }
        return result;
    }
    
    /**
     * 检查指定的音效是否存在
     */
    public String checkSound(String soundName) {
        String result = null;
        for (String s : soundList) {
            if (Objects.equals(soundName, s)) {
                result = s;
                break;
            }
        }
        return result;
    }
    
    /**
     * 检查指定的音量是否符合条件
     */
    public Double checkVolume(String volumeToCheck) {
        Double result = null;
        for (String d : volumeList) {
            if (volumeToCheck.equals(d)) {
                result = Double.valueOf(d);
                break;
            }
        }
        return result;
    }
    
    /**
     * 检查指定的音高是否存在
     */
    public String checkPitch(String pitchName) {
        String result = null;
        for (String s : pitchList.keySet()) {
            if (s.equals(pitchName)) {
                result = s;
                break;
            }
        }
        return result;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        FileManager.saveDefaultFile();
        MessageManager.init();
        FileManager.doFileCheck(this.getDescription().getVersion(), this.getConfig().getString("version"));
        
        this.getSoundList();
        this.getPitchList();
        
        this.refreshPlayerTask();
        
        this.registerListener();
        this.registerCommand();
        
        MessageManager.sendDebugInfo();
    }
    
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }
    
    public void reload(CommandSender sender) {
        sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reload.loading"));
        this.reloadConfig();
        FileManager.saveDefaultFile();
        MessageManager.init();
        this.getSoundList();
        this.getPitchList();
        
        this.refreshPlayerTask();
        MessageManager.sendDebugInfo();
        sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reload.complete"));
    }
    
    private void registerCommand() {
        Objects.requireNonNull(Bukkit.getPluginCommand("iss")).setExecutor(new BasicCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("issadmin")).setExecutor(new AdminCommand());
        Objects.requireNonNull(Bukkit.getPluginCommand("iss")).setTabCompleter(new TableCompleter());
        Objects.requireNonNull(Bukkit.getPluginCommand("issadmin")).setTabCompleter(new TableCompleter());
        
    }
    
    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }
    
    /**
     * 刷新在线玩家的数据（异步任务）
     */
    private void refreshPlayerTask() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            getLogger().info("Checking online players...");
            for (Player p : Bukkit.getOnlinePlayers()) {
                getLogger().info("Refreshing player " + p.getName() + "'s data...");
                PlayerManager.loadPlayerData(p);
            }
        });
    }
}
