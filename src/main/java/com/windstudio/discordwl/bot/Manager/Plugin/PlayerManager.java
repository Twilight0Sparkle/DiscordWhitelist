package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class PlayerManager implements Listener {
    // Bot Version
    Main plugin;
    public ArrayList<String> onlinePlayers = new ArrayList<String>();
    public PlayerManager(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        onlinePlayers.add(event.getPlayer().getName());
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onlinePlayers.remove(event.getPlayer().getName());
        plugin.getPresenceManager().Activities(plugin.getReadyEvent());
    }
    public ArrayList<String> getOnlinePlayers() { return onlinePlayers; }
}