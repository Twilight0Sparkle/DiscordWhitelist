package com.windstudio.discordwl.bot.Commands.IngameCommands;

import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import com.windstudio.discordwl.Main;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LinkinfoCommand implements CommandExecutor {
    private final Main plugin;
    public LinkinfoCommand(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorManager.translate("&cOnly players can execute this command!"));
            return true;
        }
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("account")) {
            String playerUUID = player.getUniqueId().toString();
            new BukkitRunnable() {
                public void run() {
                    switch (getString("DataBaseType")) {
                        case "SQLite":
                            DoSQLite(playerUUID, player);
                            break;
                        case "MySQL":
                            DoMySQL(playerUUID, player);
                            break;
                    }
                }
            }.runTaskAsynchronously(plugin);
                    return true;
        }
        return false;
    }
    public boolean DoSQLite(String playerUUID, Player player) {
        if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE uuid=?");
                preparedStatement.setString(1, playerUUID);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<String> list = plugin.getLanguageManager().getStringList("LinkingInfo");
                String result = StringUtils.join(list, "\n");
                while (resultSet.next()) {
                    String uuID = resultSet.getString("uuid");
                    String nickname = resultSet.getString("nickname");
                    String discord = resultSet.getString("discord");
                    String did = resultSet.getString("discord_id");
                    String date = resultSet.getString("linking_date");
                    player.sendMessage(ColorManager.translate(result.replaceAll("%0", nickname).replaceAll("%1", uuID).replaceAll("%2", discord).replaceAll("%3", did).replaceAll("%4", date)));
                }
                resultSet.close(); preparedStatement.close();
            } catch (SQLException ex) {
                ex.printStackTrace(); }
            return true;
        } else {
            sendMessage(player, ColorManager.translate(plugin.getLanguageManager().get("LinkingProfileNotExist")), new String[0]);
        }
        return false;
    }
    public boolean DoMySQL(String playerUUID, Player player) {
        if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE uuid=?");
                preparedStatement.setString(1, player.getUniqueId().toString());
                resultSet = preparedStatement.executeQuery();
                List<String> list = plugin.getLanguageManager().getStringList("LinkingInfo");
                String result = StringUtils.join(list, "\n");
                while (resultSet.next()) {
                    String uuID = resultSet.getString("uuid");
                    String nickname = resultSet.getString("nickname");
                    String discord = resultSet.getString("discord");
                    String did = resultSet.getString("discord_id");
                    String date = resultSet.getString("linking_date");
                    player.sendMessage(ColorManager.translate(result.replaceAll("%0", nickname).replaceAll("%1", uuID).replaceAll("%2", discord).replaceAll("%3", did).replaceAll("%4", date)));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                plugin.getPoolManager().close(null, preparedStatement, resultSet);
            }
            return true;
        } else {
            sendMessage(player, ColorManager.translate(plugin.getLanguageManager().get("LinkingProfileNotExist")), new String[0]);
        }
        return false;
    }
    public void sendMessage(CommandSender sender, String path, String... placeholder) {
        sender.sendMessage(String.format(ColorManager.translate(path), (Object[])placeholder));
    }
        public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
