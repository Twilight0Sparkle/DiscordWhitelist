package com.windstudio.discordwl.bot.Commands.TabCompleters;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class WhitelistTabCompleter implements TabCompleter {
    private final Main plugin;
    public WhitelistTabCompleter(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("whitelist") &&
                getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
            List<String> autoCompletes = new ArrayList<>();
            if (args.length == 1) {
                autoCompletes.add("on");
                autoCompletes.add("off");
                autoCompletes.add("lock_on");
                autoCompletes.add("lock_off");
                autoCompletes.add("add");
                autoCompletes.add("add_administrator");
                autoCompletes.add("remove");
                autoCompletes.add("remove_administrator");
                autoCompletes.add("message");
                autoCompletes.add("lock_message");
                autoCompletes.add("list");
                autoCompletes.add("list_administrators");
                return autoCompletes;
            }
            switch (getString("DataBaseType")) {
                case "SQLite":
                    if (args[0].equalsIgnoreCase("remove"))
                        return plugin.getClassManager().getSqLiteWhitelistData().getPlayers();
                    if (args[0].equalsIgnoreCase("remove_administrator"))
                        return plugin.getClassManager().getSqLiteWhitelistData().getAdministrators();
                    break;
                case "MySQL":
                    if (args[0].equalsIgnoreCase("remove"))
                        return plugin.getClassManager().getMySQLWhitelistData().getPlayers();
                    if (args[0].equalsIgnoreCase("remove_administrator"))
                        return plugin.getClassManager().getMySQLWhitelistData().getAdministrators();
                    break;
            }
        }
        return null;
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
