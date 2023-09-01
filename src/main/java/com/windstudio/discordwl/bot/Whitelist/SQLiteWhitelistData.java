package com.windstudio.discordwl.bot.Whitelist;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import org.bukkit.entity.Player;

import java.awt.*;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class SQLiteWhitelistData {
    public boolean userPlayerExists(String playerNickname) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Whitelist") + " WHERE nickname=?");
            preparedStatement.setString(1, playerNickname);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return false;
    }
    public String getPlayerType(String condition, String conditionNew, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Whitelist") + " WHERE "+condition+"=?");
            preparedStatement.setString(1, conditionNew);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String nickname = resultSet.getString("nickname");
                String playerType = resultSet.getString("player_type");
                String date = resultSet.getString("whitelist_date");
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return null;
    }
    public ArrayList<String> getPlayers() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Whitelist") + " WHERE player_type=?");
            preparedStatement.setString(1, "player");
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<String> array = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("nickname");
                array.add(name);
            }
            resultSet.close(); preparedStatement.close();
            return array;
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return null;
    }
    public ArrayList<String> getAdministrators() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Whitelist") + " WHERE player_type=?");
            preparedStatement.setString(1, "administrator");
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<String> array = new ArrayList<>();
            while (resultSet.next()) {
                String name = resultSet.getString("nickname");
                    array.add(name);
                }
            resultSet.close(); preparedStatement.close();
                return array;
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return null;
    }
    public void addPlayer(String playerNickname, String playerType, Date date) {
        PreparedStatement preparedStatement = null;
        java.sql.Date converted = new java.sql.Date(date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            preparedStatement = SQLite.con.prepareStatement("INSERT INTO " + getString("SQLiteTableName_Whitelist") +"(nickname, player_type, whitelist_date) VALUES (?, ?, ?)");
            preparedStatement.setString(1, playerNickname);
            preparedStatement.setString(2, playerType);
            preparedStatement.setString(3, format.format(converted));
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public void removePlayer(String condition, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("DELETE FROM " + getString("SQLiteTableName_Whitelist") +" WHERE " + condition +"=?");
            preparedStatement.setString(1, placeholderSet);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public void updatePlayerInfo(String column, String newInformation, String condition, String placeholderSet) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("UPDATE " + getString("SQLiteTableName_Whitelist") +" SET " + column + "=" + newInformation +" WHERE " + condition +"=?");
            preparedStatement.setString(1, placeholderSet);
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
