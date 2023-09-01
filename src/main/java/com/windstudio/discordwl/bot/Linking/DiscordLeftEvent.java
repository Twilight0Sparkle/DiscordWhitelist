package com.windstudio.discordwl.bot.Linking;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.windstudio.discordwl.Main.plugin;
import static org.bukkit.Bukkit.getServer;

public class DiscordLeftEvent extends ListenerAdapter implements Listener {
    private final Main plugin;
    /*public static HashMap<String, String> leftUser = new HashMap<String, String>();
    public static HashMap<Member, String> leftMember = new HashMap<Member, String>();
    public static HashMap<String, String> memberNickname = new HashMap<>();*/

    /*@Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot() || e.isWebhookMessage()) { return; }
        Member member = e.getMember();
        if (getStringList("SettingsEnabled").contains("CHANGE_NAME") && getStringList("SettingsEnabled").contains("REMOVE_LEFT_USERS")) {
            assert member != null;
            if (member.getNickname() != null) {
            memberNickname.put(member.getId(), member.getNickname());
            }
        }
    }*/
    public DiscordLeftEvent(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
        if (e.getMember() instanceof Webhook) { return; }
        final Member member = e.getMember();
        String memberId = member.getId().toString();
        if (getStringList("SettingsEnabled").contains("REMOVE_LEFT_USERS")) {
            member.modifyNickname(null).queue();
            switch (getString("DataBaseType")) {
                case "SQLite":
                    DoSQLite(memberId);
                    break;
                case "MySQL":
                    DoMySQL(memberId);
                    break;
                default:
                    DoSQLite(memberId);
                    break;
            }
        }
        // System.out.println(leftUser.get(member));
    }
    public void DoSQLite(String memberId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getClassManager().getUserdata().discordUserProfileExists(memberId)) {
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE discord_id=?");
                        preparedStatement.setString(1, memberId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            String uuID = resultSet.getString("uuid");
                            String nickname = resultSet.getString("nickname");
                            String discord = resultSet.getString("discord");
                            String did = resultSet.getString("discord_id");
                            String date = resultSet.getString("linking_date");
                            final Player player = getPlayerByUuid(UUID.fromString(uuID));
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                public void run() {
                                    player.setWhitelisted(false);
                                    if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                                        plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                                        plugin.getData().save();
                                    }
                                    if (player.isOnline()) {
                                        player.kickPlayer(plugin.getLanguageManager().get("LeftDiscordKickReason"));
                                    }
                                    plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("discord_id", memberId);
                                }
                            });
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void DoMySQL(String memberId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getClassManager().getUserdata().discordUserProfileExists(memberId)) {
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE discord_id=?");
                        preparedStatement.setString(1, memberId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        while (resultSet.next()) {
                            String uuID = resultSet.getString("uuid");
                            String nickname = resultSet.getString("nickname");
                            String discord = resultSet.getString("discord");
                            String did = resultSet.getString("discord_id");
                            String date = resultSet.getString("linking_date");
                            final Player player = getPlayerByUuid(UUID.fromString(uuID));
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                public void run() {
                                    player.setWhitelisted(false);
                                    if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                                        plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", nickname);
                                        plugin.getData().save();
                                    }
                                    if (player.isOnline()) {
                                        player.kickPlayer(plugin.getLanguageManager().get("LeftDiscordKickReason"));
                                    }
                                    plugin.getClassManager().getUserdataMySQL().deleteInformationFromUserProfile("discord_id", memberId);
                                }
                            });
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public Player getPlayerByUuid(UUID uuid) {
        for(Player p : getServer().getOnlinePlayers())
            if(!getServer().getOnlinePlayers().isEmpty() && p.getUniqueId().equals(uuid)) return p;
        throw new IllegalArgumentException();
    }
    public List<String> getStringList(String path) { return plugin.getConfig().getStringList(path); }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
