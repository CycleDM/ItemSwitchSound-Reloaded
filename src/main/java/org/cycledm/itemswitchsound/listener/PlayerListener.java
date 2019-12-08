package org.cycledm.itemswitchsound.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.cycledm.itemswitchsound.Main;
import org.cycledm.itemswitchsound.manager.MessageManager;
import org.cycledm.itemswitchsound.manager.PlayerManager;

/**
 * @author CycleDM
 */
public class PlayerListener implements Listener {
    /**
     * 加入事件
     **/
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        int delay = 20;
        // 加载玩家配置之前，短暂的延迟(ticks)
        new BukkitRunnable() {
            
            @Override
            public void run() {
                PlayerManager.loadPlayerData(p, false);
            }
        }.runTaskLater(Main.getInstance(), delay);
    }
    
    /**
     * 切换物品事件（主要事件）
     */
    @EventHandler
    public void onPlayerSwitchItem(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        int newSlot = e.getNewSlot();
        if ("on".equalsIgnoreCase(PlayerManager.getToggle(p))) {
            MessageManager.sendDebugInfo(p, newSlot);
            p.playSound(p.getLocation(), Sound.valueOf(
                    PlayerManager.getSound(p)),
                    PlayerManager.getVolume(p).floatValue(),
                    PlayerManager.getPitch(p, newSlot).floatValue()
            );
        }
    }
}
