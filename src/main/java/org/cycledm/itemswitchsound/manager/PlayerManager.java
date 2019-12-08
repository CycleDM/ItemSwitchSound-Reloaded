package org.cycledm.itemswitchsound.manager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.cycledm.itemswitchsound.Main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author CycleDM
 */
public class PlayerManager {
    public static File playerFolder = new File(Main.getInstance().getDataFolder(), File.separator + "player");
    
    /**
     * 用于存储玩家个人数据的哈希表
     */
    public static Map<Player, String> toggle = new HashMap<>();
    
    public static String getToggle(Player player) {
        return toggle.get(player);
    }
    
    public static Map<Player, String> sound = new HashMap<>();
    
    public static String getSound(Player player) {
        return sound.get(player);
    }
    
    public static Map<Player, Double> volume = new HashMap<>();
    
    public static Double getVolume(Player player) {
        return volume.get(player);
    }
    
    public static Map<String, Double> pitch = new HashMap<>();
    
    public static Map<String, String> pitchName = new HashMap<>();
    
    public static Map<Player, Boolean> sendInfo = new HashMap<>();
    
    public static Double getPitch(Player player, Integer slot) {
        return pitch.get(player.getUniqueId().toString() + ":slot" + slot);
    }
    
    public static String getPitchName(Player player, Integer slot) {
        return pitchName.get(player.getUniqueId().toString() + ":slot" + slot);
    }
    
    public static File getPlayerFile(Player player) {
        return new File(playerFolder + File.separator + player.getUniqueId().toString() + ".yml");
    }
    
    public static File getOfflinePlayerFile(UUID uuid) {
        return new File(playerFolder + File.separator + uuid.toString() + ".yml");
    }
    
    /**
     * 加载指定玩家的数据
     */
    public static void loadPlayerData(Player player, Boolean ignoreResetMessage) {
        File playerFile = getPlayerFile(player);
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        // 如果玩家配置中reset为true，先删除玩家的文件
        if (playerConfig.getBoolean("reset")) {
            playerFile.delete();
            // 向该玩家发送消息
            if (!ignoreResetMessage) {
                player.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.reset"));
            }
        }
        // 创建玩家的配置文件
        if (!getPlayerFile(player).exists()) {
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            // 填充数据
            playerConfig.set("toggle", "on");
            playerConfig.set("sound", Main.getInstance().getConfig().getString("sound"));
            playerConfig.set("volume", Main.getInstance().getConfig().getDouble("volume"));
            int i = 0;
            for (String s : Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("pitch")).getKeys(false)) {
                String temp = Main.getInstance().getConfig().getString("pitch." + s);
                playerConfig.set("pitch.slot" + i, temp);
                i++;
            }
            playerConfig.set("reset", false);
            // 保存配置文件
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
        }
        
        // 玩家配置文件已存在，加载玩家配置，写入哈希表
        if (getPlayerFile(player).exists()) {
            toggle.put(player, playerConfig.getString("toggle"));
            sound.put(player, playerConfig.getString("sound"));
            volume.put(player, playerConfig.getDouble("volume"));
            sendInfo.put(player, false);
            int i = 0;
            for (String s : Objects.requireNonNull(playerConfig.getConfigurationSection("pitch")).getKeys(false)) {
                // 从pitch_list获得音调名
                String temp = playerConfig.getString("pitch.slot" + i);
                // 音调名存入对应哈希表
                pitchName.put(player.getUniqueId().toString() + ":" + s, temp);
                // 翻译成double类型数据后存到对应哈希表
                pitch.put(player.getUniqueId().toString() + ":" + s, Main.pitchList.get(temp));
                i++;
            }
        }
    }
    
    /**
     * 重置所有玩家的数据
     */
    public static void resetAllPlayers() {
        // 为每一位玩家执行
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            File playerFile = PlayerManager.getOfflinePlayerFile(player.getUniqueId());
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            
            // 将reset值设置为true，只在玩家配置存在时执行
            if (playerFile.exists()) {
                playerConfig.set("reset", true);
                FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            }
            
            // 如果此时玩家在线，则立刻开始重载操作
            if (player.isOnline()) {
                loadPlayerData(player.getPlayer(), false);
            }// 否则，不进行任何操作即可（下一次登录时会自动重载）
        }
    }
}
