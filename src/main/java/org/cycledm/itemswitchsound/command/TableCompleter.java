package org.cycledm.itemswitchsound.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.cycledm.itemswitchsound.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.cycledm.itemswitchsound.Main.*;

/**
 * @author CycleDM
 */
public class TableCompleter implements TabCompleter {
    /** 引入所有指令字符串 */
    private String[] basicCommands = {TOGGLE_COMMAND, TOGGLE_SHORT_COMMAND, SET_COMMAND, RESET_COMMAND, INFO_COMMAND};
    private String[] setArgs = {SOUND_ARG, VOLUME_ARG, PITCH_ARG};
    private String[] adminCommands = {RELOAD_COMMAND, DEBUG_COMMAND, RESET_ALL_COMMAND, SET_COMMAND, CONFIRM_COMMAND};
    
    /** 根据输入的开头字符从指定的列表筛选，以对应字符串开头的值作为数组返回 */
    private List<String> getAfterResult(String[] listGroup, String start) {
        String[] result = {};
        // 检查每一个字符，返回匹配值
        for (String s : listGroup) {
            if (s.startsWith(start)) {
                result = Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = s;
            }
        }
        return Arrays.asList(result);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list;
        // 判断是否为玩家
        if (!(sender instanceof Player)) {
            list = new ArrayList<>();
        }
        else if (BASIC_COMMAND.equalsIgnoreCase(command.getName()) && sender.hasPermission(Objects.requireNonNull(command.getPermission()))) {
            list = basicList(args);
        }
        else if (ADMIN_COMMAND.equalsIgnoreCase(command.getName()) && sender.hasPermission(Objects.requireNonNull(command.getPermission()))) {
            list = adminList(args);
        }
        else {
            list = new ArrayList<>();
        }
        return list;
    }
    
    /** 基础指令补全 */
    private List<String> basicList(String[] args) {
        List<String> list;
        if (args.length == 1) {
            // 返回基本命令
            list = getAfterResult(basicCommands, args[0]);
        }
        else if (args.length == 2) {
            // 返回set命令的子命令
            list = getAfterResult(setArgs, args[1]);
        }
        else if (args.length == 3 && SET_COMMAND.equalsIgnoreCase(args[0]) && SOUND_ARG.equalsIgnoreCase(args[1])) {
            // 返回声音列表
            list = getAfterResult(Main.soundList, args[2]);
        }
        else if (args.length == 3 && SET_COMMAND.equalsIgnoreCase(args[0]) && VOLUME_ARG.equalsIgnoreCase(args[1])) {
            // 返回可用的音量列表
            list = getAfterResult(VOLUME_LIST, args[2]);
        }
        else if (args.length == 3 && SET_COMMAND.equalsIgnoreCase(args[0]) && PITCH_ARG.equalsIgnoreCase(args[1]) && args[args.length - 1].length() < 1) {
            // 返回物品栏数字
            list = Arrays.asList(getSlotList());
        }
        else if (args.length == 4 && SET_COMMAND.equalsIgnoreCase(args[0]) && PITCH_ARG.equalsIgnoreCase(args[1])) {
            // 返回可用的音调名称列表
            list = getAfterResult(getPitchList(), args[3]);
        }
        else {
            // 返回空列表
            list = new ArrayList<>();
        }
        return list;
    }
    
    /** 管理员指令补全 */
    private List<String> adminList(String[] args) {
        List<String> list;
        // 如果已经输入了一个以上的字符，则返回空列表，soundList及玩家列表除外
        if (args.length == 1) {
            list = getAfterResult(adminCommands, args[0]);
        }
        else if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            // 返回在线玩家列表
            String[] temp = {};
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().startsWith(args[1])) {
                    temp = Arrays.copyOf(temp, temp.length + 1);
                    temp[temp.length - 1] = p.getName();
                }
            }
            list = getAfterResult(temp, args[1]);
        }
        else if (args.length == 3 && SET_COMMAND.equalsIgnoreCase(args[0])) {
            // 返回set的子命令
            list = getAfterResult(setArgs, args[2]);
        }
        else if (args.length == 4 && SET_COMMAND.equalsIgnoreCase(args[0]) && SOUND_ARG.equalsIgnoreCase(args[2])) {
            // 返回可用声音列表
            list = getAfterResult(Main.soundList, args[3]);
        }
        else if (args.length == 4 && SET_COMMAND.equalsIgnoreCase(args[0]) && VOLUME_ARG.equalsIgnoreCase(args[2])) {
            // 返回可用音量列表
            list = getAfterResult(VOLUME_LIST, args[3]);
        }
        else if (args.length == 4 && SET_COMMAND.equalsIgnoreCase(args[0]) && PITCH_ARG.equalsIgnoreCase(args[2]) && args[args.length - 1].length() < 1) {
            // 返回物品栏数字
            list = Arrays.asList(getSlotList());
        }
        else if (args.length == 5 && SET_COMMAND.equalsIgnoreCase(args[0]) && PITCH_ARG.equalsIgnoreCase(args[2])) {
            // 返回可用音调名称列表
            list = getAfterResult(getPitchList(), args[4]);
        }
        else {
            list = new ArrayList<>();
        }
        return list;
    }
    
    private String[] getPitchList() {
        String[] list = {};
        for (String s : Main.pitchList.keySet()) {
            list = Arrays.copyOf(list, list.length + 1);
            list[list.length - 1] = s;
        }
        return list;
    }
    
    private String[] getSlotList() {
        String[] list = new String[0];
        for (int i = 0; i < 9; i++) {
            list = Arrays.copyOf(list, list.length + 1);
            list[list.length - 1] = String.valueOf(i + 1);
        }
        return list;
    }
}
