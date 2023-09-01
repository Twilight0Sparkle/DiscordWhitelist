package com.windstudio.discordwl.bot.LoginPanel;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class Timer {
    private final Main plugin;
    public Timer(Main plugin) {
        this.plugin = plugin;
    }
    HashMap<String, BukkitTask> taskMap = new HashMap<>();
    HashMap<String, Integer> timerMap = new HashMap<>();
    public void StartTimer(String playerUUID, Integer timerStartTime) {
        if (plugin.getClassManager().getInformationSQLite().getSessionTime(playerUUID, -1)) {
            updateInformation(playerUUID, timerStartTime);
        }
         int timer;
         timer = timerStartTime;
         timerMap.putIfAbsent(playerUUID, timer); timer = 0;
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (Objects.equals(timerMap.get(playerUUID),getInt("SessionTime")+1)) {
                    updateInformation(playerUUID, timerMap.replace(playerUUID, -1)); cancel(); return;
                }
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        updateInformation(playerUUID, timerMap.replace(playerUUID, timerMap.get(playerUUID) + 1));
                        break;
                    case "MySQL":

                        break;
                    default:
                        updateInformation(playerUUID, timerMap.replace(playerUUID, timerMap.get(playerUUID) + 1));
                        break;
                }
            }
        }.runTaskTimer(Main.getPlugin(), 72000L, 72000L);
        taskMap.putIfAbsent(playerUUID, task);
    }
    public void updateInformation(String playerUUID, Integer newTime) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = SQLite.con.prepareStatement("UPDATE " + getString("SQLiteTableName_LoginPanel") + " SET session_time='"+newTime+"' WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            preparedStatement.execute(); preparedStatement.executeUpdate(); preparedStatement.closeOnCompletion();
        } catch (SQLException e) {
            Main.console.sendMessage(e.toString());
        }
    }
    public String getString(String path) { return plugin.getConfig().getString(path); }
    public Integer getInt(String path) { return plugin.getConfig().getInt(path); }
    public HashMap<String, BukkitTask> getTaskMap() { return taskMap; }
    public HashMap<String, Integer> getTimerMap() { return timerMap; }
}
