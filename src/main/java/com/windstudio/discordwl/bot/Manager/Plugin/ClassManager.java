package com.windstudio.discordwl.bot.Manager.Plugin;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import com.windstudio.discordwl.bot.Linking.UserdataMySQL;
import com.windstudio.discordwl.bot.Linking.UserdataSQLite;
import com.windstudio.discordwl.bot.LoginPanel.InformationSQLite;
import com.windstudio.discordwl.bot.LoginPanel.PlayerPreLoginHandler;
import com.windstudio.discordwl.bot.LoginPanel.Timer;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import com.windstudio.discordwl.bot.Whitelist.MySQLWhitelistData;
import com.windstudio.discordwl.bot.Whitelist.SQLiteWhitelistData;

public class ClassManager {
    private Main plugin;
    private UserdataSQLite userdataSQLite;
    private UserdataMySQL userdataMySQL;
    private SQLiteWhitelistData sqLiteWhitelistData;
    private MySQLWhitelistData mySqlWhitelistData;
    private InformationSQLite informationSQLite;
    private PlayerPreLoginHandler playerPreLoginHandler;
    private PresenceManager presenceManager;
    private LinkingCommand linkingCommand;
    private static Timer timer;
    public ClassManager(Main plugin) {
        this.plugin = plugin;
        presenceManager = new PresenceManager(plugin);
        playerPreLoginHandler = new PlayerPreLoginHandler(plugin);
        timer = new Timer(plugin);
        userdataMySQL = new UserdataMySQL(plugin);
        linkingCommand = new LinkingCommand(plugin);
        mySqlWhitelistData = new MySQLWhitelistData(plugin);
        userdataSQLite = new UserdataSQLite();
        sqLiteWhitelistData = new SQLiteWhitelistData();
        informationSQLite = new InformationSQLite();
    }
    public UserdataSQLite getUserdata() { return userdataSQLite; }
    public UserdataMySQL getUserdataMySQL() { return userdataMySQL; }
    public SQLiteWhitelistData getSqLiteWhitelistData() { return sqLiteWhitelistData; }
    public MySQLWhitelistData getMySQLWhitelistData() { return mySqlWhitelistData; }
    public InformationSQLite getInformationSQLite() { return informationSQLite; }
    public PlayerPreLoginHandler getPlayerPreLoginHandler() { return playerPreLoginHandler; }
    public PresenceManager getPresenceManager() { return presenceManager; }
    public Timer getTimer() { return timer; }
    public LinkingCommand getLinkingCommand() { return linkingCommand; }
    public String getString(String path) { return plugin.getConfig().getString(path); }
}
