package com.windstudio.discordwl.bot.Linking;

import com.windstudio.discordwl.Main;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserdataMySQL {
    private final Main plugin;
    public UserdataMySQL(Main plugin) {
        this.plugin = plugin;
    }
    public void createUserProfile(String playerUUID, String playerName, String Discord, String DiscordID) {
                if (!userProfileExists(playerUUID)) {
                    PreparedStatement preparedStatement = null;
                    Date now = new Date();
                    java.sql.Date converted = new java.sql.Date(now.getTime());
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    try {
                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("INSERT INTO " + getString("MySQL_TableName_Linking") + "(uuid, nickname, discord, discord_id, linking_date) VALUES (?, ?, ?, ?, ?)");
                        preparedStatement.setString(1, playerUUID);
                        preparedStatement.setString(2, playerName);
                        preparedStatement.setString(3, Discord);
                        preparedStatement.setString(4, DiscordID);
                        preparedStatement.setString(5, format.format(converted));
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    } catch (SQLException ex) {
                        Main.console.sendMessage(ex.toString());
                    } finally {
                        plugin.getPoolManager().close(null, preparedStatement, null);
                    }
                }
    }
    public String getInformationFromUserProfile(String whatYouNeedExactly, String placeholderSet) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE " + whatYouNeedExactly + "=?");
            preparedStatement.setString(1, placeholderSet);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
        } catch (SQLException ex){
            Main.console.sendMessage(ex.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public String getSingleInformationFromUserProfile(String condition, String conditionNew, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE " + condition + "=?");
            preparedStatement.setString(1, conditionNew);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
        } catch (SQLException ex) {
            Main.console.sendMessage(ex.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public void deleteInformationFromUserProfile(String whatYouNeedExactly, String placeholderSet) {
                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("DELETE FROM " + getString("MySQL_TableName_Linking") + " WHERE " + whatYouNeedExactly + "=?");
                    preparedStatement.setString(1, placeholderSet);
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    Main.console.sendMessage(ex.toString());
                } finally {
                    plugin.getPoolManager().close(null, preparedStatement, null);
                }
    }
    public void setInformationToUserProfile(Player player, Integer parameterIndex, String x) {
        PreparedStatement preparedStatement = null;
        /*Date now = new Date();
        java.sql.Date converted = new java.sql.Date(now.getTime());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); */
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("INSERT INTO "+getString("MySQL_TableName_Linking") + "(uuid, nickname, discord, discord_id, linking_date) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(parameterIndex, x);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            Main.console.sendMessage(ex.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }
    public boolean userProfileExists(String playerUUID) {
                PreparedStatement preparedStatement = null; ResultSet resultSet = null;
                try {
                    preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE uuid=?");
                    preparedStatement.setString(1, playerUUID);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return true;
                    }
                } catch (SQLException e) {
                    Main.console.sendMessage(e.toString());
                } finally {
                    plugin.getPoolManager().close(null, preparedStatement, resultSet);
                }
        return false;
    }
    public boolean userProfileExistsString(String condition, String condition2) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE "+condition+"=?");
            preparedStatement.setString(1, condition2);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return false;
    }
    public boolean discordUserProfileExists(String DiscordID) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE discord_id=?");
            preparedStatement.setString(1, DiscordID);
             resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return false;
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
