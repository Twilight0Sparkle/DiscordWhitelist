package com.windstudio.discordwl.bot.Manager.Plugin;

import org.bukkit.ChatColor;

public final class ColorManager {
    public static String translate(String msg) {
        if (msg == null) {
            System.out.println(ColorManager.translate("&c > Some message is null. Send feedback to plugin's administrator to get help."));
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public static String buildString(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++)
            sb.append(args[i]).append(" ");
        return sb.toString();
    }
}