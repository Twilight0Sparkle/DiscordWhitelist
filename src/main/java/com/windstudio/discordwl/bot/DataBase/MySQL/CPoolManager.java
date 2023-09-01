package com.windstudio.discordwl.bot.DataBase.MySQL;

import com.windstudio.discordwl.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CPoolManager {
    private final Main plugin;
    private HikariDataSource dataSource;
    private String hostname;
    private String port;
    private String database;
    private String username;
    private String password;
    private long connectionTimeout;
    private String tableLinking = getString("MySQL_TableName_Linking");
    private String tableWhitelist = getString("MySQL_TableName_Whitelist");
    private String tableIP = getString("MySQL_TableName_LoginPanel");

    public CPoolManager(Main plugin) {
        this.plugin = plugin;
        init();
        setupPool();
        createTableIP(); createTableLinking(); createTableWhtitelist();
    }

    private void init() {
        hostname = getString("MySQL_Host");
        port = getString("MySQL_Port");
        database = getString("MySQL_DatabaseName");
        username = getString("MySQL_Login");
        password = getString("MySQL_Password");
        connectionTimeout = getInt("MySQL_Connection_TimeOut");
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(connectionTimeout);
        dataSource = new HikariDataSource(config);
    }
    private void createTableLinking() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS " +tableLinking+"(uuid varchar(36) PRIMARY KEY, nickname varchar(16), discord varchar(37), discord_id varchar(18), linking_date varchar(19));");
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.console.sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    private void createTableWhtitelist() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+tableWhitelist+"(nickname varchar(16), player_type varchar(13), whitelist_date varchar(19));");
            preparedStatement.executeUpdate(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.console.sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    private void createTableIP() {
        PreparedStatement preparedStatement = null;
        Connection con = null;
        try {
            con = getConnection();
            preparedStatement = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+tableIP+ "(nickname varchar(16) PRIMARY KEY, uuid varchar(36), lastseen_ip varchar(15), login_ip varchar(15), login_date varchar(19), session_time int, using_panel bit default 1)");
            preparedStatement.execute(); preparedStatement.close();
        } catch (SQLException e) {
            plugin.console.sendMessage(e.toString());
        } finally {
            close(con, preparedStatement, null);
        }
    }
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) { Main.console.sendMessage(ignored.toString());}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {Main.console.sendMessage(ignored.toString());}
        if (res != null) try { res.close(); } catch (SQLException ignored) {Main.console.sendMessage(ignored.toString());}
    }
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
    public static Integer getInt(String path) { return Main.getPlugin().getConfig().getInt(path); }
}
