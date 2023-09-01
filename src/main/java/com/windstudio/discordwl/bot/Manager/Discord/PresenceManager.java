package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.PlayerManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PresenceManager extends ListenerAdapter {
    // Bot Version
    private final Main plugin;
    public PresenceManager(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onReady(@NotNull ReadyEvent e) {
        if (getConfigBoolean("Status&Activities")) {
            Status(e); Activities(e);
        }
    }

    private String getConfigPath(String path) {
        return Main.plugin.getConfig().getString(path);
    }
    private boolean getConfigBoolean(String path) {
        return Main.plugin.getConfig().getBoolean(path);
    }
    public void Status(ReadyEvent e) {
        switch (getConfigPath("Status")) {
            case "Online":
                e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
            break;
            case "DND":
                e.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                break;
            case "Idle":
                e.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
                break;
            case "Invisible":
                e.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
                break;
        }
    }
    public void Activities(ReadyEvent e) {
        switch (getConfigPath("ActivitiesType")) {
            case "Playing":
                e.getJDA().getPresence().setActivity(Activity.playing(getConfigPath("Activity").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Listening":
                e.getJDA().getPresence().setActivity(Activity.listening(getConfigPath("Activity").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Watching":
                e.getJDA().getPresence().setActivity(Activity.watching(getConfigPath("Activity").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
            case "Streaming":
                e.getJDA().getPresence().setActivity(Activity.streaming(getConfigPath("Activity").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size())), getConfigPath("ActivityURL")));
                break;
            case "Competing":
                e.getJDA().getPresence().setActivity(Activity.competing(getConfigPath("Activity").replaceAll("%0", String.valueOf(plugin.getPlayerManager().getOnlinePlayers().size()))));
                break;
        }
    }
}