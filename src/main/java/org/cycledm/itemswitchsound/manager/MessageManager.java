package org.cycledm.itemswitchsound.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.cycledm.itemswitchsound.Main;

import java.io.File;
import java.util.Objects;

/**
 * @author CycleDM
 */
public class MessageManager {
    public static String filePath = Main.getInstance().getDataFolder() + File.separator + "lang" + File.separator;
    public static String insidePath = "lang" + File.separator;
    public static FileConfiguration messageConfig = new YamlConfiguration();
    
    public static File getMessageFile() {
        return new File(filePath, Main.getInstance().getConfig().getString("language") + ".yml");
    }
    
    public static void init() {
        FileManager.saveDefaultFile();
        messageConfig = YamlConfiguration.loadConfiguration(getMessageFile());
    }
    
    public static String getString(String node) {
        String str = messageConfig.getString(node);
        if (str == null) {
            return ("§c" + node);
        }
        return str.replace("&", "§");
    }
    
    public static String getPrefix() {
        return getString("prefix") + " ";
    }
    
    /**
     * 向控制台发送debug消息（异步任务）
     */
    public static void sendDebugInfo() {
        if (Main.getInstance().getConfig().getBoolean("debug")) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                String debugPrefix = getString("debug.prefix") + " ";
                Main.getInstance().getServer().getConsoleSender()
                        .sendMessage(getPrefix() + debugPrefix + getString("debug.title"));
                Main.getInstance().getServer().getConsoleSender().sendMessage(getPrefix() + debugPrefix
                        + ChatColor.RESET + "sound: " + Main.getInstance().getConfig().getString("sound"));
                Main.getInstance().getServer().getConsoleSender().sendMessage(getPrefix() + debugPrefix
                        + ChatColor.RESET + "volume: " + Main.getInstance().getConfig().getDouble("volume"));
                Main.getInstance().getServer().getConsoleSender()
                        .sendMessage(getPrefix() + debugPrefix + ChatColor.RESET + "pitch: ");
                for (String s : Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("pitch"))
                        .getKeys(false)) {
                    String pitchName = Main.getInstance().getConfig().getString("pitch." + s);
                    Main.getInstance().getServer().getConsoleSender()
                            .sendMessage(getPrefix() + debugPrefix + ChatColor.RESET + "  " + s + ": " + pitchName + "("
                                    + Main.pitchList.get(pitchName) + ")");
                }
            });
        }
    }
    
    /**
     * 向符合条件的玩家发送debug消息
     */
    public static void sendDebugInfo(Player player, int slot) {
        if (player.hasPermission("itemswitchsound.admin") && Main.getInstance().getConfig().getBoolean("debug")) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                player.sendMessage(getString("debug.prefix") + " " + getString("debug.title"));
                player.sendMessage(getString("debug.alias.slot") + ": " + ChatColor.AQUA + slot);
                player.sendMessage(
                        getString("debug.alias.sound") + ": " + ChatColor.AQUA + PlayerManager.getSound(player));
                player.sendMessage(
                        getString("debug.alias.volume") + ": " + ChatColor.AQUA + PlayerManager.getVolume(player));
                player.sendMessage(getString("debug.alias.pitch") + ": " + ChatColor.AQUA
                        + PlayerManager.getPitchName(player, slot) + ChatColor.GREEN + " ("
                        + PlayerManager.getPitch(player, slot) + ")");
            });
        }
        else if (PlayerManager.sendInfo.get(player)) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                player.sendMessage(getString("debug.prefix") + " " + getString("debug.title"));
                player.sendMessage(getString("debug.alias.slot") + ": " + ChatColor.AQUA + slot);
                player.sendMessage(
                        getString("debug.alias.sound") + ": " + ChatColor.AQUA + PlayerManager.getSound(player));
                player.sendMessage(
                        getString("debug.alias.volume") + ": " + ChatColor.AQUA + PlayerManager.getVolume(player));
                player.sendMessage(getString("debug.alias.pitch") + ": " + ChatColor.AQUA
                        + PlayerManager.getPitchName(player, slot) + ChatColor.GREEN + " ("
                        + PlayerManager.getPitch(player, slot) + ")");
            });
        }
    }
}
