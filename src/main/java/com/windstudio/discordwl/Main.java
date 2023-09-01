package com.windstudio.discordwl;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.jeff_media.updatechecker.UserAgentBuilder;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkinfoCommand;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import com.windstudio.discordwl.bot.Commands.IngameCommands.WhitelistCommand;
import com.windstudio.discordwl.bot.Commands.SlashCommands.SlashCommands;
import com.windstudio.discordwl.bot.Commands.TabCompleters.LinkingTabCompleter;
import com.windstudio.discordwl.bot.Commands.TabCompleters.WhitelistTabCompleter;
import com.windstudio.discordwl.bot.DataBase.MySQL.CPoolManager;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Linking.DiscordLeftEvent;
import com.windstudio.discordwl.bot.Linking.PlayerEventHandler;
import com.windstudio.discordwl.bot.LoginPanel.PlayerPreLoginHandler;
import com.windstudio.discordwl.bot.Manager.Discord.DiscordButtonManager;
import com.windstudio.discordwl.bot.Manager.Discord.DiscordMessageManager;
import com.windstudio.discordwl.bot.Manager.Discord.DiscordModalManager;
import com.windstudio.discordwl.bot.Manager.Discord.PresenceManager;
import com.windstudio.discordwl.bot.Manager.Plugin.*;
import com.windstudio.discordwl.bot.Whitelist.PlayerPreLoginListener;
import com.windstudio.discordwl.bot.Whitelist.WhitelistData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static net.dv8tion.jda.api.utils.ChunkingFilter.ALL;

public class Main extends JavaPlugin {

    private static final int SPIGOT_RESOURCE_ID = 97587;
    public static Main plugin;
    private static JDA jda;
    public FileConfiguration playerData;
    public File data;
    private WhitelistData dataW;
    public static ConsoleCommandSender console;
    public static ClassManager classManager;
    public static JDA getJDA() {
        return jda;
    }
    private EmbedBuilder embedBuilder;
    private EmbedBuilder logsEmbedBuilder;
    private EmbedBuilder dmEmbedBuilder;
    private LanguageManager languageManager;
    private CPoolManager cPoolManager;
    private PlayerManager playerManager;
    private PresenceManager presenceManager;
    private ReadyEvent readyEvent;
    public void onLoad() {
        plugin = this;
        console = Bukkit.getServer().getConsoleSender();
        classManager = new ClassManager(plugin);
        jda = getJDA();
        embedBuilder = new EmbedBuilder();
        dmEmbedBuilder = new EmbedBuilder();
        logsEmbedBuilder = new EmbedBuilder();
        languageManager = new LanguageManager(plugin);
    }
    @Override
    public void onEnable() {
        onLoad();
        int pluginId = 15019;
        Metrics metrics = new Metrics(this, pluginId);
        if (Bukkit.getServer().getName().equals("Folia")) {
            console.sendMessage(ColorManager.translate(" &e> &fAs that you run &eFolia&f in your server, some features may be broken! ..."));
            console.sendMessage(ColorManager.translate("     &f...You can't use this plugin with &eFolia&f!"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        playerManager = new PlayerManager(this);
        if (plugin.getConfig().getBoolean("Check-Updates") && !Bukkit.getServer().getName().equals("Folia")) {
            new UpdateChecker(this, UpdateCheckSource.SPIGOT, "97587")
                    .setDownloadLink("https://www.spigotmc.org/resources/discord-whitelist-third-generation.97587/")
                    .setChangelogLink("https://www.spigotmc.org/resources/discord-whitelist-third-generation.97587/updates")
                    .setNotifyOpsOnJoin(true)
                    .setNotifyByPermissionOnJoin("whitelist.admin")
                    .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                    .checkEveryXHours(3)
                    .checkNow();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
            try {
                ConfigUpdater.update(this, "config.yml", configFile, Arrays.asList());
            } catch (IOException e) {
                console.sendMessage(ColorManager.translate("&c > &fCannot update config.yml!"));
            }
            plugin.getLanguageManager().FileUpdate();
        if (getResource("license.txt") == null) {
            console.sendMessage(ColorManager.translate("&c > &fFile &clicense.txt &fin plugin jar does not exist. Seems you deleted it. Restore it by rollback it or re-downloading plugin."));
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        File license = new File(getDataFolder(), "license.txt");
        if (!license.exists()) {
            try {
                saveResource("license.txt", false);
            } catch (Exception e) {
                console.sendMessage(ColorManager.translate("&c> &fCannot create license file, be sure to report this issue to the developer!"));
            }
        }
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        console.sendMessage(ColorManager.translate("&e > &fConnecting to &eSQLite&f..."));
                        SQLite.connect();
                        console.sendMessage(ColorManager.translate("&a > &fSuccessfully connected to &aSQLite&f!"));
                        break;
                    case "MySQL":
                        console.sendMessage(ColorManager.translate("&e > &fConnecting to &eMySQL&f..."));
                        try {
                            cPoolManager = new CPoolManager(this);
                            console.sendMessage(ColorManager.translate("&a > &fConnected to &eMySQL&f!&r"));
                        } catch (Exception e) {
                            console.sendMessage(ColorManager.translate("&c > &fAn error occurred while connecting to &cMySQL&f:&r"));
                            console.sendMessage(ColorManager.translate(e.toString()));
                            Bukkit.getPluginManager().disablePlugin(this);
                            return;
                        }
                        break;
                    default:
                        console.sendMessage(ColorManager.translate("&e > &fEither plugin see DataBase type incorrectly, either you include incorrect DataBase type, ..."));
                        console.sendMessage(ColorManager.translate("&e > &f... but plugin has force set DataBase type to &eSQLite&f! If that was mistake - contact with developer! "));
                        getConfig().set("DataBaseType", "SQLite");
                        SQLite.connect();
                        break;
                }
        String argument = getConfig().getString("Version");
        if (!argument.equals(getDescription().getVersion())) {
                this.getConfig().set("Version", getDescription().getVersion());
            saveConfig();
            reloadConfig();
        }
        try {
            try {
                jda = JDABuilder.createDefault(plugin.getConfig().getString("Token"))
                        .addEventListeners(new SlashCommands(this), new PresenceManager(this), new LinkingCommand(this), new DiscordMessageManager(this), new DiscordLeftEvent(this), new DiscordButtonManager(this), new DiscordModalManager(this))
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setChunkingFilter(ALL)
                        .build().awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (jda != null) {
                console.sendMessage(ColorManager.translate( "&r\n" +
                                                                "  &5╔══╗   &f╔╗╔╗╔╗╔╗╔╗╔══╗╔════╗╔═══╗╔╗  ╔══╗╔══╗╔════╗    &r\n" +
                                                                "  &5║╔╗╚╗  &f║║║║║║║║║║╚╗╔╝╚═╗╔═╝║╔══╝║║  ╚╗╔╝║╔═╝╚═╗╔═╝  &7│ &fPlugin by &bWIND Studio&r\n" +
                                                                "  &5║║╚╗║  &f║║║║║║║╚╝║ ║║   ║║  ║╚══╗║║   ║║ ║╚═╗  ║║    &7│ &5Discord&fWhitelist &7· &e2023&r\n" +
                                                                "  &5║║ ║║  &f║║║║║║║╔╗║ ║║   ║║  ║╔══╝║║   ║║ ╚═╗║  ║║    &7│ &r\n" +
                                                                "  &5║╚═╝║&7╔╗&f║╚╝╚╝║║║║║╔╝╚╗  ║║  ║╚══╗║╚═╗╔╝╚╗╔═╝║  ║║    &7│ &8("+Bukkit.getServer().getName()+") &fVersion: &b" + getDescription().getVersion() + " &7· &aStarting...&r\n" +
                                                                "  &5╚═══╝&7╚╝&f╚═╝╚═╝╚╝╚╝╚══╝  ╚╝  ╚═══╝╚══╝╚══╝╚══╝  ╚╝      &r\n"));
                console.sendMessage(ColorManager.translate("&f > Registering commands..."));
                presenceManager = new PresenceManager(this);
                readyEvent = new ReadyEvent(jda);
                Bukkit.getPluginManager().registerEvents(new PlayerPreLoginListener(this), this); Bukkit.getPluginManager().registerEvents(playerManager, this);
                Bukkit.getPluginManager().registerEvents(new PlayerPreLoginHandler(this), this);
                PluginCommand commandW = getCommand("owhitelist");
                if (commandW == null) throw new IllegalStateException("Command 'owhitelist' is null, have you modified the plugin.yml file?");
                PluginCommand commandL = getCommand("link");
                if (commandL == null) throw new IllegalStateException("Command 'link' is null, have you modified the plugin.yml file?");
                PluginCommand commandLI = getCommand("account");
                if (commandLI == null) throw new IllegalStateException("Command 'account' is null, have you modified the plugin.yml file?");
                commandW.setExecutor((CommandExecutor)new WhitelistCommand(this));
                commandW.setTabCompleter((TabCompleter)new WhitelistTabCompleter(this));
                commandL.setExecutor((CommandExecutor) new LinkingCommand(this));
                commandL.setTabCompleter((TabCompleter)new LinkingTabCompleter(this));
                commandLI.setExecutor((CommandExecutor) new LinkinfoCommand(this));
                Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(jda, this),this);
                Guild guild = jda.getGuildById(plugin.getConfig().getString("GuildID"));
                if (guild != null) {
                    jda.updateCommands().addCommands(
                            Commands.slash("whitelist", "Allows add/remove players to/from whitelist.")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "type", "Type of usage of command", true)
                                                    .addChoice("add", "add")
                                                    .addChoice("remove", "remove")
                                                    .addChoice("list", "list"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to add/remove it ", false),
                            Commands.slash("list", "Shows list of server players")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "list", "Type of usage of command", false)
                                                    .addChoice("players", "players")
                                                    .addChoice("banned", "banned")),
                            Commands.slash("account", "Allows link discord account with minecraft account.")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "type", "Type of usage of command", true)
                                                    .addChoice("link", "link")
                                                    .addChoice("unlink", "unlink"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to link with", true),
                            Commands.slash("setupreaction", "Allows setup message with reaction button to add/remove role to/from user"),
                            Commands.slash("checkwhitelist", "Provides information about user, is he in whitelist or not")
                                    .setGuildOnly(true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "whitelisttype", "Type of whitelist", true)
                                                    .addChoice("default", "default")
                                                    .addChoice("our", "our"))
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to link with", true),
                            Commands.slash("checklink", "Provides information about user, is he linked his account or not")
                                    .setGuildOnly(true)
                                    .addOption(OptionType.STRING, "username", "Insert nickname here to check linked account with this name and UUID", false)
                                    .addOption(OptionType.STRING, "did", "Insert DiscordID here to check linked account with this DiscordID", false)
                    ).queue();
                } else {
                    console.sendMessage(ColorManager.translate("&f[DiscordWhitelist] &cWe can't retrieve guild by your GuildID, so we cant update slash-commands!"));
                }
                console.sendMessage(ColorManager.translate("&f > Registering interactions..."));
                if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                    console.sendMessage(ColorManager.translate("&f > Registering Whitelist... It can take some time to you!"));
                    console.sendMessage(ColorManager.translate("&e > &fWhitelist version: 0.7A2803"));
                    this.dataW = new WhitelistData(getDataFolder() + "/data.yml");
                    console.sendMessage(ColorManager.translate("&e > &fData was created, loading..."));
                     try {
                         this.dataW.load();
                         console.sendMessage(ColorManager.translate("&a > &fData loaded successfully!"));
                     } catch (Exception e) {
                         console.sendMessage(ColorManager.translate("&c > &fData can't loaded!"));
                     }
                }
                if (getStringList("SettingsEnabled").contains("LINKING")) {
                    console.sendMessage(ColorManager.translate("&e > &fLinking version: 0.2A2803"));
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerManager.getOnlinePlayers().add(player.getName());
                    presenceManager.Activities(readyEvent);
                }
                console.sendMessage(ColorManager.translate("&a> &5Discord&fWhitelist has started &acorrectly&f! Have a nice day &c<3"));
                console.sendMessage("\n");
            } else {
                console.sendMessage(ColorManager.translate("&c > &fJDA still not initialized, returning..."));
                Bukkit.getServer().getPluginManager().disablePlugin(this);
            }
        } catch (Exception ex) {
            console.sendMessage(ColorManager.translate("&c > &fInvalid bot token! Check your config.yml!"));
            console.sendMessage(ColorManager.translate("&c > &fSome possible reasons of that issue: "));
            console.sendMessage(ColorManager.translate("  &7· &fYour token is invalid and plugin can't connect to bot;"));
            console.sendMessage(ColorManager.translate("  &7· &fYou forget to enable all &cPrivileged Gateway Intents&f into Developer Portal;"));
            console.sendMessage(ColorManager.translate("  &7· &fYour plugin is outdated and you need to update it."));
        }
        // LanguageManager plugin.getLanguageManager() = new LanguageManager(this); // Debug
        // String SymbolsError = plugin.getLanguageManager().get("SymbolsError");         // ^^^
        // console.sendMessage(ColorManager.translate(SymbolsError));    // ^^^
    }


    public void onDisable() {
        console.sendMessage(ColorManager.translate( "&r\n" +
                "  &5╔══╗   &f╔╗╔╗╔╗╔╗╔╗╔══╗╔════╗╔═══╗╔╗  ╔══╗╔══╗╔════╗    &r\n" +
                "  &5║╔╗╚╗  &f║║║║║║║║║║╚╗╔╝╚═╗╔═╝║╔══╝║║  ╚╗╔╝║╔═╝╚═╗╔═╝  &7│ &fPlugin by &bWIND Studio&r\n" +
                "  &5║║╚╗║  &f║║║║║║║╚╝║ ║║   ║║  ║╚══╗║║   ║║ ║╚═╗  ║║    &7│ &5Discord&fWhitelist &7· &e2023&r\n" +
                "  &5║║ ║║  &f║║║║║║║╔╗║ ║║   ║║  ║╔══╝║║   ║║ ╚═╗║  ║║    &7│ &r\n" +
                "  &5║╚═╝║&7╔╗&f║╚╝╚╝║║║║║╔╝╚╗  ║║  ║╚══╗║╚═╗╔╝╚╗╔═╝║  ║║    &7│ &8("+Bukkit.getServer().getName()+") &fVersion: &b" + getDescription().getVersion() + " &7· &cStopping...&r\n" +
                "  &5╚═══╝&7╚╝&f╚═╝╚═╝╚╝╚╝╚══╝  ╚╝  ╚═══╝╚══╝╚══╝╚══╝  ╚╝      &r\n"));
        switch (getString("DataBaseType")) {
            case "SQLite":
                console.sendMessage(ColorManager.translate("&e > &fDisconnecting from &eSQLite&f..."));
                try {
                    SQLite.disconnect();
                    console.sendMessage(ColorManager.translate("&a > &fDisconnected from &eSQLite&f!"));
                } catch (Exception e) {
                    console.sendMessage(ColorManager.translate("&c > &fAn error occurred while disconnecting from &cSQLite&f:"));
                    console.sendMessage(ColorManager.translate(e.toString()));
                }
                break;
            case "MySQL":
                console.sendMessage(ColorManager.translate("&e > &fClosing &eMySQL&f connection..."));
                try {
                    getPoolManager().close(getPoolManager().getConnection(), null, null);
                    console.sendMessage(ColorManager.translate("&a > &fClosed &aMySQL&f connection!"));
                } catch (Exception e) {
                    console.sendMessage(ColorManager.translate("&c > &fAn error occurred while closing &cMySQL&f oonnection:"));
                    console.sendMessage(ColorManager.translate(e.toString()));
                }
                break;
        }
            if (dataW != null && getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                this.dataW.save();
            }
            if (jda != null) {
                jda.shutdown();
                try {
                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow();
                    jda.awaitShutdown();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            console.sendMessage(ColorManager.translate("&f[DiscordWhitelistBot] &ePlugin was stopped."));
        }

    public WhitelistData getData() {
        return this.dataW;
    }
    public List<String> getStringList(String path) { return plugin.getConfig().getStringList(path); }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
    public static Plugin getPlugin() { return plugin; }
    public ClassManager getClassManager() { return classManager; }
    public EmbedBuilder getEmbedBuilder() {return embedBuilder; }
    public EmbedBuilder getDMEmbedBuilder() { return dmEmbedBuilder; }
    public EmbedBuilder getLogsEmbedBuilder() {return logsEmbedBuilder; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public CPoolManager getPoolManager() { return cPoolManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PresenceManager getPresenceManager() { return presenceManager; }
    public ReadyEvent getReadyEvent() { return readyEvent; }
}