package com.windstudio.discordwl.bot.Whitelist;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MySQLWhitelistData {
    private final Main plugin;
    public MySQLWhitelistData(Main plugin) {
        this.plugin = plugin;
    }
    public boolean userPlayerExists(String playerNickname) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Whitelist") + " WHERE nickname=?");
            preparedStatement.setString(1, playerNickname);
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
    public String getPlayerType(String condition, String conditionNew, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Whitelist") + " WHERE "+condition+"=?");
            preparedStatement.setString(1, conditionNew);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public ArrayList<String> getPlayers() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Whitelist") + " WHERE player_type=?");
            preparedStatement.setString(1, "player");
            resultSet = preparedStatement.executeQuery();
            ArrayList<String> array = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("nickname");
                array.add(name);
            }
            return array;
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public ArrayList<String> getAdministrators() {
        PreparedStatement preparedStatement = null; ResultSet resultSet = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Whitelist") + " WHERE player_type=?");
            preparedStatement.setString(1, "administrator");
            resultSet = preparedStatement.executeQuery();
            ArrayList<String> array = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("nickname");
                array.add(name);
            }
            return array;
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, resultSet);
        }
        return null;
    }
    public void addPlayer(String playerNickname, String playerType, Date date) {
        PreparedStatement preparedStatement = null;
        java.sql.Date converted = new java.sql.Date(date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("INSERT INTO " + getString("MySQL_TableName_Whitelist") +"(nickname, player_type, whitelist_date) VALUES (?, ?, ?)");
            preparedStatement.setString(1, playerNickname);
            preparedStatement.setString(2, playerType);
            preparedStatement.setString(3, format.format(converted));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }
    public void removePlayer(String condition, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("DELETE FROM " + getString("MySQL_TableName_Whitelist") +" WHERE " + condition +"=?");
            preparedStatement.setString(1, placeholderSet);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }
    public void updatePlayerInfo(String column, String newInformation, String condition, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("UPDATE " + getString("MySQL_TableName_Whitelist") +" SET " + column + "=" + newInformation +" WHERE " + condition +"=?");
            preparedStatement.setString(1, placeholderSet);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        } finally {
            plugin.getPoolManager().close(null, preparedStatement, null);
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
