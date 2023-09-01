package com.windstudio.discordwl.bot.Whitelist;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PlayerPreLoginListener implements Listener {
    private final Main plugin;
    public PlayerPreLoginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getConfig().getStringList("Blacklist-nick").contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate("&cYour nickname isin blacklist!"));
        }
        if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                    switch(getString("DataBaseType")) {
                        case "SQLite":
                            DoSQLite(event);
                            break;
                        case "MySQL":
                            DoMySQL(event);
                            break;
                    }
                }
        }
        public void DoSQLite(AsyncPlayerPreLoginEvent event) {
            if (this.plugin.getData().isWhitelist_locked() && !plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().contains(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(this.plugin.getData().getLockMessage()));
            }
            if (this.plugin.getData().isWhitelist() &&
                    !plugin.getClassManager().getSqLiteWhitelistData().getPlayers().contains(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(this.plugin.getData().getMessage()));
            }
        }
    public void DoMySQL(AsyncPlayerPreLoginEvent event) {
        if (this.plugin.getData().isWhitelist_locked() && !plugin.getClassManager().getMySQLWhitelistData().getAdministrators().contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(this.plugin.getData().getLockMessage()));
        }
        if (this.plugin.getData().isWhitelist() &&
                !plugin.getClassManager().getMySQLWhitelistData().getPlayers().contains(event.getName())) {
            //event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ColorManager.translate(this.plugin.getData().getMessage()));
            //event.getPlayer().kickPlayer(ColorManager.translate(this.plugin.getData().getMessage()));
            //event.setKickMessage(ColorManager.translate(this.plugin.getData().getMessage()));
        }
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
