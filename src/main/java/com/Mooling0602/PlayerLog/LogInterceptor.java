package com.Mooling0602.PlayerLog;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogInterceptor extends JavaPlugin implements Listener {

    private boolean chatInterceptEnabled = false; // 默认不拦截聊天消息
    private String dynamicString = "debug";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("chatmsg").setExecutor(this);
        registerDynamicCommand();
        getLogger().info(ChatColor.GREEN + "PlayerLog 已启用！" + ChatColor.RESET);
    }

    private void registerDynamicCommand() {
        Command dynamicCommand = new Command(dynamicString) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                sender.sendMessage("执行了动态注册命令！");
                return true;
            }
        };

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) Bukkit.getServer()
                    .getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
            commandMap.register("dynamiccmd", dynamicCommand);
            getLogger().info("动态命令注册成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterDynamicCommand() {
        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) Bukkit.getServer()
                    .getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
            java.lang.reflect.Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            if (knownCommands.containsKey(dynamicString)) {
                knownCommands.remove(dynamicString);
                getLogger().info("动态命令已取消注册！");
            } else {getLogger().info("没有要取消注册的动态命令！");}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.RED + "PlayerLog 已禁用！" + ChatColor.RESET);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (chatInterceptEnabled) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (chatInterceptEnabled) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (chatInterceptEnabled) {
            event.setCancelled(true);
        }
        Bukkit.getConsoleSender().sendMessage("<" + event.getPlayer().getName() + "> " + event.getMessage());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chatmsg")) {
            if (!(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "该命令只能通过控制台或Rcon执行！" + ChatColor.RESET);
                return true;
            }
            // if (args.length != 1) {
            //     getLogger().info(ChatColor.YELLOW + "用法: 在控制台执行chatmsg on|off" + ChatColor.RESET);
            //     return true;
            // }
            if (args[0].equalsIgnoreCase("on")) {
                chatInterceptEnabled = false;
                sender.sendMessage(ChatColor.GOLD + "客户端聊天拦截功能已关闭" + ChatColor.GRAY + "，若MCDR插件功能未关闭会出现聊天消息重复或功能无法使用等异常情况，" + ChatColor.RESET + ChatColor.GOLD + "BukkitChatManager需配置启用兼容模式（compatibility_mode）" + ChatColor.RESET);
            }
            else if (args[0].equalsIgnoreCase("off")) {
                chatInterceptEnabled = true;
                sender.sendMessage(ChatColor.GREEN + "客户端聊天拦截功能已开启" + ChatColor.GRAY + "，MCDR插件可开始接管聊天内容，" + ChatColor.RESET + ChatColor.GOLD + "BukkitChatManager需配置禁用兼容模式（compatibility_mode）" + ChatColor.RESET);
            }
            else if (args[0].equalsIgnoreCase("set_test_node")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.GRAY + "用法: chatmsg set_test_node <新值>" + ChatColor.RESET);
                    return true;
                }
                unregisterDynamicCommand();
                dynamicString = args[1];
                registerDynamicCommand();
                sender.sendMessage("[调试] dynamicString 已更新为: " + ChatColor.YELLOW + dynamicString + ChatColor.RESET);
                return true;
            }
            else
                getLogger().info(ChatColor.YELLOW + "正确用法: 在控制台执行chatmsg on|off" + ChatColor.RESET);
            sender.sendMessage("[调试] 当前命令节点数量有：" + args.length);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("chatmsg")) {
            if (args.length == 1) {
                completions.add("on");
                completions.add("off");
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase(dynamicString)) {
                    completions.add("test");
                }
            }
            if (args.length > 2) {
                completions = new ArrayList<>();
            }
        }
        if (command.getName().equalsIgnoreCase("!!MCDR")) {
            if (args.length == 1) {
                completions.add("status");
            }
        }
        if (args.length == 0) {
            completions.add("!!MCDR");
        }
        return completions;
    }
}