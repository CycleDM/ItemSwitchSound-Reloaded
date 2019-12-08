package org.cycledm.itemswitchsound.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.cycledm.itemswitchsound.Main;
import org.cycledm.itemswitchsound.manager.FileManager;
import org.cycledm.itemswitchsound.manager.MessageManager;
import org.cycledm.itemswitchsound.manager.PlayerManager;

import java.io.File;
import java.util.Objects;

/**
 * @author CycleDM
 */
public class BasicCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 屏蔽控制台
        if (!(sender instanceof Player)) {
            System.out.println(MessageManager.getPrefix() + MessageManager.getString("error.no_player"));
            return true;
        }
        
        Player p = ((Player) sender).getPlayer();
        if (p == null) {
            return false;
        }
        File playerFile = PlayerManager.getPlayerFile(p);
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        if (args.length == 0) {
            return false;
        }
        if (args.length == 1 && "info".equalsIgnoreCase(args[0])) {
            // 如果玩家是管理员，此时debug模式已开启，则跳过此设置
            if (p.hasPermission("itemswitchsound.admin") && Main.getInstance().getConfig().getBoolean("debug")) {
                return true;
            }
            if (PlayerManager.sendInfo.get(p)) {
                PlayerManager.sendInfo.put(p, false);
                p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("debug.player-disabled"));
            } else {
                PlayerManager.sendInfo.put(p, true);
                p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("debug.player-enabled"));
            }
            return true;
        }
        if (args.length == 1 && ("toggle".equalsIgnoreCase(args[0]) || "t".equalsIgnoreCase(args[0]))) {
            String temp;
            if (Objects.equals(playerConfig.getString("toggle"), "on")) {
                temp = "off";
            } else {
                temp = "on";
            }
            playerConfig.set("toggle", temp);
            PlayerManager.toggle.put(p, temp);
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            if ("on".equals(temp)) {
                p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("toggle.enabled"));
            } else {
                p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("toggle.disabled"));
            }
            return true;
        }
        if (args.length == 1 && "reset".equalsIgnoreCase(args[0])) {
            playerConfig.set("reset", true);
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            PlayerManager.loadPlayerData(p);
            p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("reset"));
            return true;
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0]) && "sound".equalsIgnoreCase(args[1])) {
            String sound = Main.getInstance().checkSound(args[2]);
            
            if (sound == null) {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.sound_not_found"));
                return true;
            }
            playerConfig.set("sound", sound);
            PlayerManager.sound.put(p, sound);
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.self"));
            return true;
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0]) && "volume".equalsIgnoreCase(args[1])) {
            Double volume = Main.getInstance().checkVolume(args[2]);
            if (volume == null) {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                return true;
            }
            playerConfig.set("volume", volume);
            PlayerManager.volume.put(p, volume);
            FileManager.saveTargetConfigurationFile(playerFile, playerConfig);
            p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.self"));
            return true;
        }
        if (args.length == 4 && "set".equalsIgnoreCase(args[0]) && "pitch".equalsIgnoreCase(args[1])) {
            String pitch = Main.getInstance().checkPitch(args[3]);
            String slot = args[2];
            if (Integer.parseInt(slot) < 1 || Integer.parseInt(slot) > 9) {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                return true;
            }
            if (pitch == null) {
                sender.sendMessage(MessageManager.getPrefix() + MessageManager.getString("error.value_not_allowed"));
                return true;
            }
            int i = Integer.parseInt(args[2]) - 1;
            playerConfig.set("pitch.slot" + i, pitch);
            PlayerManager.pitch.put(p.getUniqueId().toString() + ":slot" + i, Main.pitchList.get(pitch));
            PlayerManager.pitchName.put(p.getUniqueId().toString() + ":slot" + i, pitch);
            p.sendMessage(MessageManager.getPrefix() + MessageManager.getString("changed.self"));
            return true;
        }
        return false;
    }
}
