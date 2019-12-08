package org.cycledm.itemswitchsound.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.cycledm.itemswitchsound.Main;
import org.cycledm.itemswitchsound.manager.FileManager;
import org.cycledm.itemswitchsound.manager.MessageManager;
import org.cycledm.itemswitchsound.manager.PlayerManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author CycleDM
 */
public class AdminCommand implements CommandExecutor {
    
    /**
     * 计划任务相关的哈希表
     * 分别为：
     * 是否可以确认操作（默认false）
     * 计时器任务的task id（默认null）
     */
    private Map<CommandSender, Boolean> isConfirmAllowed = new HashMap<>();
    private Map<CommandSender, Integer> confirmTaskId = new HashMap<>();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 初始化哈希表
        isConfirmAllowed.putIfAbsent(sender, false);
        confirmTaskId.putIfAbsent(sender, null);
        
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            Main.getInstance().reload(sender);
            return true;
        }
        if (args.length == 1 && "debug".equalsIgnoreCase(args[0])) {
            boolean isDebugMode = Main.getInstance().getConfig().getBoolean("debug");
            if (isDebugMode) {
                Main.getInstance().getConfig().set("debug", false);
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("debug.disabled"));
                Main.getInstance().saveConfig();
            } else {
                Main.getInstance().getConfig().set("debug", true);
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("debug.enabled"));
                Main.getInstance().saveConfig();
            }
            return true;
        }
        if (args.length == 1 && "reset-all".equalsIgnoreCase(args[0])) {
            
            // 确认操作倒计时任务
            int taskid = new BukkitRunnable() {
                
                int time = 15;
                
                @Override
                public void run() {
                    isConfirmAllowed.put(sender, true);
                    time--;
                    if (time == 0) {
                        cancel();
                        isConfirmAllowed.put(sender, false);
                    }
                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 0L, 20L).getTaskId();
            
            confirmTaskId.put(sender, taskid);
            sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reset-all.confirm"));
            return true;
        }
        if (args.length == 1 && "confirm".equalsIgnoreCase(args[0])) {
            // 确认执行
            if (isConfirmAllowed.get(sender)) {
                PlayerManager.resetAllPlayers();
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reset-all.complete"));
                // 取消计时器，并重置哈希表
                Bukkit.getScheduler().cancelTask(confirmTaskId.get(sender));
                confirmTaskId.putIfAbsent(sender, null);
                isConfirmAllowed.put(sender, false);
                return true;
            }
            // 如果没有confirm操作，则发送信息
            else {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reset-all.not_allowed"));
                return true;
            }
        }
        if (args.length >= 4 && "set".equalsIgnoreCase(args[0])) {
            // 检查目标玩家
            OfflinePlayer targetPlayer = Main.getInstance().checkPlayer(args[1]);
            // 是否在线
            boolean isOnline = targetPlayer.isOnline();
            
            File playerFile = PlayerManager.getOfflinePlayerFile(targetPlayer.getUniqueId());
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            if (!PlayerManager.getOfflinePlayerFile(targetPlayer.getUniqueId()).exists()) {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.player_not_found"));
                return true;
            }
            
            if ("sound".equalsIgnoreCase(args[2])) {
                String sound = Main.getInstance().checkSound(args[3]);
                
                if (sound == null) {
                    sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.sound_not_found"));
                    return true;
                }
                playerConfig.set("sound", sound);
                // 如果玩家在线，则立刻应用更改。发送消息提示该玩家（包含自己）
                if (isOnline) {
                    PlayerManager.sound.put(targetPlayer.getPlayer(), args[3]);
                    Objects.requireNonNull(targetPlayer.getPlayer()).sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.op").replace("{0}", sender.getName()));
                }
            } else if ("volume".equalsIgnoreCase(args[2])) {
                Double volume = Main.getInstance().checkVolume(args[3]);
                if (volume == null) {
                    sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                    return true;
                }
                playerConfig.set("volume", volume);
                // 如果玩家在线，则立刻应用更改。发送消息提示该玩家（包含自己）
                if (isOnline) {
                    PlayerManager.volume.put(targetPlayer.getPlayer(), volume);
                    Objects.requireNonNull(targetPlayer.getPlayer()).sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.op").replace("{0}", sender.getName()));
                }
            } else if (args.length == 5 && "pitch".equalsIgnoreCase(args[2])) {
                String pitch = Main.getInstance().checkPitch(args[4]);
                String slot = args[3];
                if (Integer.parseInt(slot) < 1 || Integer.parseInt(slot) > 9) {
                    sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                    return true;
                }
                if (pitch == null) {
                    sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                    return true;
                }
                int i = Integer.parseInt(args[3]) - 1;
                playerConfig.set("pitch.slot" + i, pitch);
                // 如果玩家在线，则立刻应用更改。发送消息提示该玩家（包含自己）
                if (isOnline) {
                    PlayerManager.pitch.put(targetPlayer.getUniqueId().toString() + ":slot" + i, Main.pitchList.get(pitch));
                    PlayerManager.pitchName.put(targetPlayer.getUniqueId().toString() + ":slot" + i, pitch);
                    Objects.requireNonNull(targetPlayer.getPlayer()).sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.op").replace("{0}", sender.getName()));
                }
            } else {
                return false;
            }
            
            // 保存
            FileManager.saveTargetConfigFile(playerFile, playerConfig);
            // 发送消息
            if (sender.getName().equals(targetPlayer.getName())) {
                // 目标如果是自己，发送以下消息
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.self"));
            } else {
                // 目标如果不是自己，发送以下消息
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.other").replace("{0}", Objects.requireNonNull(targetPlayer.getPlayer()).getName()));
            }
            
            return true;
        }
        return false;
    }
}
