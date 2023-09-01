package com.windstudio.discordwl.bot.Linking;

import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PlayerEventHandler implements Listener {
    String guild = Main.plugin.getConfig().getString("GuildID");
    JDA jda;
    private final Main plugin;
    public PlayerEventHandler(JDA jda, Main plugin) {
        this.jda = jda;
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String playerUUID = e.getPlayer().getUniqueId().toString();
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.add(e.getPlayer().getUniqueId());
                            DoSQLite(e.getPlayer());
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.add(e.getPlayer().getUniqueId());
                            DoMySQL(e.getPlayer());
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String playerUUID = e.getPlayer().getUniqueId().toString();
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidCodeMap.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidIdMap.remove(e.getPlayer().getUniqueId());
                            DoSQLite(e.getPlayer());
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                            LinkingCommand.verifiedmembers.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidCodeMap.remove(e.getPlayer().getUniqueId());
                            LinkingCommand.uuidIdMap.remove(e.getPlayer().getUniqueId());
                            DoMySQL(e.getPlayer());
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void DoSQLite(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (getStringList("SettingsEnabled").contains("REMOVE_LEFT_USERS") && plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE uuid=?");
                preparedStatement.setString(1, playerUUID);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String uuID = resultSet.getString("uuid");
                    String nickname = resultSet.getString("nickname");
                    String discord = resultSet.getString("discord");
                    String did = resultSet.getString("discord_id");
                    String date = resultSet.getString("linking_date");
                    if (jda.getGuildById(getString("GuildID")).getMemberById(did) == null) {
                        player.setWhitelisted(false);
                        if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                            plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                            plugin.getData().save();
                        }
                        player.kickPlayer(ColorManager.translate(plugin.getLanguageManager().get("LeftDiscordKickReason")));
                        plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    }

                }
                resultSet.close(); preparedStatement.close();
            } catch (SQLException ex) {
                ex.printStackTrace(); }
        }
    }
    public void DoMySQL(Player player) {
        String playerUUID = player.getUniqueId().toString();
                if (getStringList("SettingsEnabled").contains("REMOVE_LEFT_USERS") && plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                    PreparedStatement preparedStatement = null;
                    ResultSet resultSet = null;
                    try {
                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE uuid=?");
                        preparedStatement.setString(1, playerUUID);
                        resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            String uuID = resultSet.getString("uuid");
                            String nickname = resultSet.getString("nickname");
                            String discord = resultSet.getString("discord");
                            String did = resultSet.getString("discord_id");
                            String date = resultSet.getString("linking_date");
                            if (jda.getGuildById(getString("GuildID")).getMemberById(did) == null) {
                                player.setWhitelisted(false);
                                if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                                    plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                                    plugin.getData().save();
                                }
                                player.kickPlayer(ColorManager.translate(plugin.getLanguageManager().get("LeftDiscordKickReason")));
                                plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                            }

                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } finally {
                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                    }
                }
    }
    public List<String> getStringList(String path){
        return Main.plugin.getConfig().getStringList(path);
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
