package com.windstudio.discordwl.bot.DataBase.SQLite;

import com.windstudio.discordwl.Main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLite {
    public static Connection con;
    public static void connect() {
        File DBFile = new File(Main.getPlugin().getDataFolder(), getString("SQLiteDatabaseName") + ".db");
        if (!DBFile.exists()) {
            try {
                DBFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String URL = "jdbc:sqlite:"+DBFile;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(URL);
            createTableLinking();
            createTableWhtitelist();
            createTableIP();
            con.setAutoCommit(true);
        } catch (Exception e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static void disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static void createTableLinking() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+getString("SQLiteTableName_Linking")+ "(uuid varchar(36) PRIMARY KEY, nickname varchar(16), discord varchar(37), discord_id varchar(18), linking_date varchar(19))");
            preparedStatement.execute(); preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static void createTableWhtitelist() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+getString("SQLiteTableName_Whitelist")+ "(nickname varchar(16), player_type varchar(13), whitelist_date varchar(19))");
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static void createTableIP() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+getString("SQLiteTableName_LoginPanel")+ "(uuid varchar(36) PRIMARY KEY, nickname varchar(16), lastseen_ip varchar(15), login_ip varchar(15), login_date varchar(19), session_time int, using_panel int, locked int)");
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
