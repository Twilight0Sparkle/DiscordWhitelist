package com.windstudio.discordwl.bot.Commands.IngameCommands;

import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import com.windstudio.discordwl.Main;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.List;

public class WhitelistCommand implements CommandExecutor {
    private final Main plugin;
    public WhitelistCommand(Main plugin) {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
            String player, message;
            if (args.length < 1) {
                sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
                return true;
            }
            switch (args[0]) {
                case "on":
                    if (!Main.plugin.getData().isWhitelist()) {
                        Main.plugin.getData().setWhitelist(true);
                        Main.plugin.getData().save();
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistOn"), new String[0]);
                    } else {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOn"), new String[0]);
                    }
                    return true;
                case "off":
                    if (Main.plugin.getData().isWhitelist()) {
                        Main.plugin.getData().setWhitelist(false);
                        Main.plugin.getData().save();
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistOff"), new String[0]);
                    } else {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOff"), new String[0]);
                    }
                    return true;
                case "lock_on":
                    if (!Main.plugin.getData().isWhitelist_locked()) {
                        Main.plugin.getData().setWhitelistLocked(true);
                        Main.plugin.getData().save();
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockOn"), new String[0]);
                    } else {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOn"), new String[0]);
                    }
                    return true;
                case "lock_off":
                    if (plugin.getData().isWhitelist_locked()) {
                        plugin.getData().setWhitelistLocked(false);
                        plugin.getData().save();
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockOff"), new String[0]);
                    } else {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyOff"), new String[0]);
                    }
                    return true;
                case "add":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    player = args[1];
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            if (!plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(player)) {
                                Date now = new Date();
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAdded"), new String[]{player});
                                plugin.getClassManager().getSqLiteWhitelistData().addPlayer(player, "player", now);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                            }
                            return true;
                        case "MySQL":
                            if (!plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(player)) {
                                Date now = new Date();
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAdded"), new String[]{player});
                                plugin.getClassManager().getMySQLWhitelistData().addPlayer(player, "player", now);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                            }
                            return true;
                    }
                    return true;
                case "add_administrator":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    player = args[1];
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        if (!plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(player)) {
                            Date now = new Date();
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockAdded"), new String[]{player});
                            plugin.getClassManager().getSqLiteWhitelistData().addPlayer(player, "administrator", now);
                            plugin.getData().save();
                        } else {
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                        }
                        return true;
                    case "MySQL":
                        if (!plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(player)) {
                            Date now = new Date();
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockAdded"), new String[]{player});
                            plugin.getClassManager().getMySQLWhitelistData().addPlayer(player, "administrator", now);
                            plugin.getData().save();
                        } else {
                            sendMessage(sender, plugin.getLanguageManager().get("WhitelistAlreadyAdded"), new String[0]);
                        }
                        return true;
                }
                return true;
                case "remove":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    player = args[1];
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            if (plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(player)) {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistRemoved"), new String[]{player});
                                plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", player);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                            }
                            return true;
                        case "MySQL":
                            if (plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(player)) {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistRemoved"), new String[]{player});
                                plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", player);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                            }
                            return true;
                    }
                    return true;
                case "remove_administrator":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                        return true;
                    }
                    player = args[1];
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            if (plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(player)) {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockRemoved"), new String[]{player});
                                plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", player);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                            }
                            return true;
                        case "MySQL":
                            if (plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(player)) {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistLockRemoved"), new String[]{player});
                                plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", player);
                                plugin.getData().save();
                            } else {
                                sendMessage(sender, plugin.getLanguageManager().get("WhitelistPlayerNotFound"), new String[0]);
                            }
                            return true;
                    }
                    return true;
                case "message":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("MessageNotFound"), new String[0]);
                        return true;
                    }
                    message = ColorManager.buildString(args, 1);
                    sendMessage(sender, plugin.getLanguageManager().get("WhitelistMsgSet"), new String[]{message});
                    plugin.getData().setMessage(message);
                    plugin.getData().save();
                    return true;
                case "lock_message":
                    if (args.length < 2) {
                        sendMessage(sender, plugin.getLanguageManager().get("MessageNotFound"), new String[0]);
                        return true;
                    }
                    message = ColorManager.buildString(args, 1);
                    sendMessage(sender, plugin.getLanguageManager().get("LockMsgSet"), new String[]{message});
                    plugin.getData().setLockMessage(message);
                    plugin.getData().save();
                    return true;
                case "list":
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("WhitelistList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getSqLiteWhitelistData().getPlayers(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getPlayers().size()))));
                            return true;
                        case "MySQL":
                            sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("WhitelistList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getMySQLWhitelistData().getPlayers(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getPlayers().size()))));
                            return true;
                    }
                    return true;
                case "list_administrators":
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("AdministratorsList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().size()))));
                            return true;
                        case "MySQL":
                            sender.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("AdministratorsList").replaceAll("%s", StringUtils.join(plugin.getClassManager().getMySQLWhitelistData().getAdministrators(), ", ")).replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getAdministrators().size()))));
                            return true;
                    }
                    return true;
            }
            sendMessage(sender, plugin.getLanguageManager().get("WhitelistUsage"), new String[0]);
            return true;
        } else {
            Main.console.sendMessage(ColorManager.translate(" &e> &fYou should enable OUR_WHITELIST_SYSTEM in the config to use OurWhitelist!"));
        }
        return false;
    }


    public void sendMessage(CommandSender sender, String path, String... placeholder) {
        sender.sendMessage(String.format(ColorManager.translate(path), (Object[])placeholder));
    }
    public List<String> getStringList(String path){
        return Main.plugin.getConfig().getStringList(path);
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}