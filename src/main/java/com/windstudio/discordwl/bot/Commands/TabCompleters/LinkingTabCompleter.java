package com.windstudio.discordwl.bot.Commands.TabCompleters;

import com.windstudio.discordwl.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class LinkingTabCompleter implements TabCompleter {
    private final Main plugin;

    public LinkingTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("link")) {
            List<String> autoCompletes = new ArrayList<>();
            if (args.length == 1) {
                autoCompletes.add("link");
                return autoCompletes;
            }
        }
        return null;
    }
}
