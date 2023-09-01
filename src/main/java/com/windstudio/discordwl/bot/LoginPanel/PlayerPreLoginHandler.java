package com.windstudio.discordwl.bot.LoginPanel;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class PlayerPreLoginHandler implements Listener {
    private Main plugin;
    HashSet<String> hashSetUUID = new HashSet<>();
    public PlayerPreLoginHandler(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerUUID = event.getUniqueId().toString();
        if (getStringList("SettingsEnabled").contains("LOGIN_CONTROL_PANEL")) {
            switch (getString("DataBaseType")) {
                case "SQLite":
                    if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) DoSQLite(playerUUID, event);
                    break;
                case "MySQL":

                    break;
            }
        }
    }
    public void updateInformation(String playerUUID, Date newDate) {
        PreparedStatement preparedStatement = null;
        try {
            java.sql.Date converted = new java.sql.Date(newDate.getTime());
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            preparedStatement = SQLite.con.prepareStatement("UPDATE " + getString("SQLiteTableName_LoginPanel") + " SET login_date='"+format.format(converted)+"' WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            preparedStatement.execute(); preparedStatement.executeUpdate(); preparedStatement.closeOnCompletion();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public void DoSQLite(String playerUUID, AsyncPlayerPreLoginEvent event) {
        if (plugin.getClassManager().getInformationSQLite().userLoginPanelInfoExist(playerUUID) == Boolean.valueOf(false)) {
            String lastSeenIP = event.getAddress().getHostAddress();
            plugin.getClassManager().getInformationSQLite().createLoginProfile(playerUUID, event.getName(), lastSeenIP, lastSeenIP, 1, 0);
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ColorManager.translate("&cYou need to verify your login through Discord!"));
            if (!hashSetUUID.contains(playerUUID)) {
                hashSetUUID.add(playerUUID);
                User user = Main.getJDA().getUserById(plugin.getClassManager().getUserdata().getInfoFromUserProfile("uuid", playerUUID, "discord_id"));
                user.openPrivateChannel().queue((messages) -> {
                    plugin.getEmbedBuilder().setTitle("Hello!");
                    plugin.getEmbedBuilder().setColor(Color.GREEN);
                    plugin.getEmbedBuilder().setDescription("We just got that you need to confirm your connection!");
                    messages.sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("verify", "Verify"), Button.danger("lock", "(Un)Lock account"), Button.secondary("switch", "Switch panel use"), Button.primary("help", "Request administrator's help")).queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                });
            }
        }
        if (plugin.getClassManager().getInformationSQLite().checkUserAccountIsLocked(playerUUID, 1)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ColorManager.translate("&cYour account is locked by Discord request!"));
        }
        /*if (Main.getInformationSQLite().userLoginPanelInfoExist(playerUUID) &&
                Main.getInformationSQLite().isUserLoginPanelUseIt(playerUUID, 1) &&
                hashSetUUID.contains(playerUUID)) {
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(ColorManager.translate("&cYou need to verify your login through Discord!"));
        }*/
            for (String s : getStringList("LoginSettings")) {
                switch (s) {
                    case "CHECK_SESSION_TIME":
                        SQLiteCheckSessionTime(playerUUID, event);
                        break;
                    case "COMPARE_IP":
                        SQLiteCompareIP(playerUUID, event);
                        break;
                    case "ALWAYS_ON":
                        if (s.contains("COMPARE_IP") || s.contains("CHECK_SESSION_TIME")) {
                            Main.console.sendMessage(ColorManager.translate("&c > &fIf you've enabled &cALWAYS_ON&f setting, then you need turn off &call&f other settings!"));
                            return;
                        }
                        SQLiteCompareIP(playerUUID, event);
                        SQLiteCheckSessionTime(playerUUID, event);
                        break;
                }
            }
            Date now = new Date();
            updateInformation(playerUUID, now);
        }
    public void SQLiteCompareIP(String playerUUID, AsyncPlayerPreLoginEvent event) {
        String lastLoginIP = plugin.getClassManager().getInformationSQLite().getSingleInformationFromUserLoginInformation("uuid", playerUUID, "lastseen_ip");
        String newLoginIP = plugin.getClassManager().getInformationSQLite().getSingleInformationFromUserLoginInformation("uuid", playerUUID, "login_ip");
        if (!Objects.equals(lastLoginIP, newLoginIP)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ColorManager.translate("&cYou need to verify your login through &5Discord &cserver!"));
            if (!hashSetUUID.contains(playerUUID)) {
                hashSetUUID.add(playerUUID);
                User user = Main.getJDA().getUserById(plugin.getClassManager().getUserdata().getInfoFromUserProfile("uuid", playerUUID, "discord_id"));
                user.openPrivateChannel().queue((messages) -> {
                    plugin.getEmbedBuilder().setTitle("Hello!");
                    plugin.getEmbedBuilder().setColor(Color.GREEN);
                    plugin.getEmbedBuilder().setDescription("We just got that you need to confirm your connection!");
                    messages.sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("verify", "Verify"), Button.danger("lock", "(Un)Lock account"), Button.secondary("switch", "Switch panel use"), Button.primary("help", "Request administrator's help")).queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                });
            }
        }
    }
    public void SQLiteCheckSessionTime(String playerUUID, AsyncPlayerPreLoginEvent event) {
       //System.out.println(Main.getInformationSQLite().getSessionTimeTest(playerUUID));
        if (plugin.getClassManager().getInformationSQLite().getSessionTime(playerUUID, -1)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ColorManager.translate("&cYou need to verify your login through &5Discord &cserver!"));
            if (!hashSetUUID.contains(playerUUID)) {
                hashSetUUID.add(playerUUID);
                User user = Main.getJDA().getUserById(plugin.getClassManager().getUserdata().getInfoFromUserProfile("uuid", playerUUID, "discord_id"));
                user.openPrivateChannel().queue((messages) -> {
                    plugin.getEmbedBuilder().setTitle("Hello!");
                    plugin.getEmbedBuilder().setColor(Color.GREEN);
                    plugin.getEmbedBuilder().setDescription("We just got that you need to confirm your connection!");
                    messages.sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("verify", "Verify"), Button.danger("lock", "(Un)Lock account"), Button.secondary("switch", "Switch panel use"), Button.primary("help", "Request administrator's help")).queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                });
            }
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
    public List<String> getStringList(String path) { return Main.getPlugin().getConfig().getStringList(path); }
    public HashSet<String> getHashSetUUID() { return hashSetUUID; }
}
