package com.windstudio.discordwl.bot.Commands.IngameCommands;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.awt.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.*;

public class LinkingCommand extends ListenerAdapter implements CommandExecutor, Listener {
    public String CODE = randomString(10);
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
    public static HashMap<UUID, String> uuidCodeMap;
    public static HashMap<UUID, String> uuidIdMap;
    public static List<UUID> verifiedmembers;
    public Guild guild;
    public ConsoleCommandSender console;
    private static String Name;
    private static String plUUID;
    private static String Discord;
    private static String did;
    private static Member target;
    private static Player player;
    private final JDA jda;
    private final Main plugin;

    public LinkingCommand(Main plugin) {
        this.plugin=plugin;
        this.jda = Main.getJDA();
        uuidCodeMap = new HashMap<>();
        uuidIdMap = new HashMap<>();
        verifiedmembers = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { // Command
        if (getStringList("SettingsEnabled").contains("LINKING")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorManager.translate("&cOnly players can execute this command!"));
                return true;
            }
            console = Bukkit.getServer().getConsoleSender();
            player = (Player) sender;
            did = uuidIdMap.get(player.getUniqueId());
            if (did == null) {
                sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingStart")), new String[0]);
                return true;
            }
            target = jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).getMemberById(did);
            plUUID = player.getUniqueId().toString();
            Name = player.getName();
            Discord = target.getUser().getName() + "#" + target.getUser().getDiscriminator();
            String actualcode = uuidCodeMap.get(player.getUniqueId());
            if (cmd.getName().equalsIgnoreCase("link")) {
                if (args.length != 1) {
                    sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingUsage")), new String[0]);
                    return true;
                }
                if (!actualcode.equals(args[0])) {
                    sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingInvalidCode")), new String[0]);
                    return true;
                }
                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                    public void run() {
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                if (plugin.getClassManager().getUserdata().userProfileExists(plUUID)) {
                                    sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingAlreadyLinked")), new String[0]);
                                }
                                return;
                            case "MySQL":
                                if (plugin.getClassManager().getUserdataMySQL().userProfileExists(plUUID)) {
                                    sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingAlreadyLinked")), new String[0]);
                                }
                                return;
                        }
                    }
                });
                uuidCodeMap.remove(player.getUniqueId());
                uuidIdMap.remove(player.getUniqueId());
                verifiedmembers.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                    public void run() {
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                plugin.getClassManager().getUserdata().createUserProfile(plUUID, Name, Discord, did);
                                break;
                            case "MySQL":
                                plugin.getClassManager().getUserdataMySQL().createUserProfile(plUUID, Name, Discord, did);
                                break;
                        }
                    }
                });
                if (Main.plugin.getConfig().getString("LinkedRoleID") != null) {
                    if (jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID")) != null) {
                        try {
                            Role verifiedRole = jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID"));
                            jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).addRoleToMember(target, verifiedRole).queue();
                        } catch (Exception e) {
                            console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Seems that user has higher role that bot!"));
                            player.sendMessage(ColorManager.translate("&cBot can't add you role!"));
                        }
                    }
                }
                if (!Objects.equals(Main.plugin.getConfig().getString("LinkedRemoveRoleID"), "notuse")) {
                    if (jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID")) != null) {
                        try {
                            Role verifiedRemoveRole = jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID"));
                            jda.getGuildById(Main.plugin.getConfig().getString("GuildID")).removeRoleFromMember(target, verifiedRemoveRole).queue();
                        } catch (Exception e) {
                            console.sendMessage(ColorManager.translate("&c > &fBot can't remove role from user. Seems that user has higher role that bot!"));
                            player.sendMessage(ColorManager.translate("&cBot can't remove role from you!"));
                        }
                    }
                }
                if (getStringList("SettingsEnabled").contains("LINK_NAME_CHANGE")) {
                    try {
                        target.modifyNickname(Name).queue();
                    } catch (Exception e) {
                            console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Seems that user has higher role that bot!"));
                            sender.sendMessage(ColorManager.translate("&cBot can't modify your nickname!"));
                        }
                }
                EmbedBuilder eb = new EmbedBuilder();
                target.getUser().openPrivateChannel().queue((messages) -> {
                    eb.setTitle(plugin.getLanguageManager().get("LinkingLinkedEmbedTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingLinkedEmbedDescription").replaceAll("%u", player.getName()));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("LinkedEmbedColor")));
                    messages.sendMessageEmbeds(eb.build()).queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                });
                sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingLinkedMsg").replaceAll("%u", Discord)), new String[0]);
                if (getStringList("SettingsEnabled").contains("LOGGING")) {
                    String mention = jda.getGuildById(getString("GuildID")).getMemberById(did).getAsMention();
                    String discord = jda.getUserById(did).getName() + "#" + jda.getUserById(did).getDiscriminator();
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("LinkingLogTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingLogLinkedDescription").replaceAll("%u", mention).replaceAll("%d", discord).replaceAll("%p", player.getName()).replaceAll("%i", player.getUniqueId().toString()));
                    jda.getGuildById(getString("GuildID")).getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID")).sendMessageEmbeds(eb.build()).queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                }
                return true;
            }
        } else {
            sendMessage(sender, ColorManager.translate(plugin.getLanguageManager().get("LinkingTurnedOff")), new String[0]);
        }
        return false;
    }

    public void sendMessage(CommandSender sender, String path, String... placeholder) {
        sender.sendMessage(String.format(ColorManager.translate(path), (Object[])placeholder));
    }
    public String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
    public String getCODE() {
        return CODE;
    }
    public static String getName() {
        return Name;
    }
    public static String getPlUUID() {
        return plUUID;
    }
    public static String getDiscord() {
        return Discord;
    }
    public static String getDid() {
        return did;
    }
    public static Player getPlayer() {
        return player;
    }
    public static Member getTarget() {
        return target;
    }
    public static HashMap<UUID, String> getUuidCodeMap() {
        return uuidCodeMap;
    }
    public static HashMap<UUID, String> getUuidIdMap() {
        return uuidIdMap;
    }
    public static List<UUID> getVerifiedmembers() {
        return verifiedmembers;
    }
    public List<String> getStringList(String path) { return Main.plugin.getConfig().getStringList(path); }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}