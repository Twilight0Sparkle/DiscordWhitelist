package com.windstudio.discordwl.bot.LoginPanel;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class InformationSQLite {
    public boolean userLoginPanelInfoExist(String playerUUID) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
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
    public void createLoginProfile(String playerUUID, String playerName, String lastSeenIP, String loginIP, Integer usingPanel, Integer accountLocked) {
        if (!userLoginPanelInfoExist(playerUUID)) {
            PreparedStatement preparedStatement = null;
            Date now = new Date();
            java.sql.Date converted = new java.sql.Date(now.getTime());
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            try {
                preparedStatement = SQLite.con.prepareStatement("INSERT INTO "+getString("SQLiteTableName_LoginPanel") + "(uuid, nickname, lastseen_ip, login_ip, login_date, session_time, using_panel, locked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, playerUUID);
                preparedStatement.setString(2, playerName);
                preparedStatement.setString(3, lastSeenIP);
                preparedStatement.setString(4, loginIP);
                preparedStatement.setString(5, format.format(converted));
                preparedStatement.setInt(6, -1);
                preparedStatement.setInt(7, usingPanel);
                preparedStatement.setInt(8, accountLocked);
                preparedStatement.executeLargeUpdate();
                preparedStatement.close();
            } catch (SQLException ex) {
                Main.console.sendMessage(ex.toString());
            }
        }
    }
    public boolean isUserLoginPanelUseIt(String playerUUID, Integer checkInteger) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int isUsingPanel = resultSet.getInt("using_panel");
                if (Objects.equals(isUsingPanel, checkInteger)) {
                return true;
                }
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return false;
    }
    public boolean checkUserAccountIsLocked(String playerUUID, Integer checkInteger) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int isUsingPanel = resultSet.getInt("locked");
                if (Objects.equals(isUsingPanel, checkInteger)) {
                    return true;
                }
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
        return false;
    }
    public String getSingleInformationFromUserLoginInformation(String condition, String conditionNew, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE " + condition + "=?");
            preparedStatement.setString(1, conditionNew);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex) {
            Main.console.sendMessage(ex.toString());
        }
        return null;
    }
    public boolean getSessionTime(String playerUUID, Integer conditionInt) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int sessionTime = resultSet.getInt("session_time");
                if (sessionTime == conditionInt) return true;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex) {
            Main.console.sendMessage(ex.toString());
        }
        return false;
    }
   /*public int getSessionTimeTest(String playerUUID) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int sessionTime = resultSet.getInt("session_time");
                if (sessionTime == -1) return sessionTime;
            }
            resultSet.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 9999;
    }*/
    public String getInformationFromUserProfile(String condition, String placeholderSet, String whatYouNeedExactly) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE " + condition + "=?");
            preparedStatement.setString(1, placeholderSet);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String gotYouNeedExactly = resultSet.getString(whatYouNeedExactly);
                return gotYouNeedExactly;
            }
            resultSet.close(); preparedStatement.close();
        } catch (SQLException ex){
            Main.console.sendMessage(ex.toString());
        }
        return null;
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
