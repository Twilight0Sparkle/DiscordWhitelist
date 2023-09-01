package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.windstudio.discordwl.Main.console;
import static org.bukkit.Bukkit.getPlayer;

public class DiscordButtonManager extends ListenerAdapter {
    private final Main plugin;
    private final JDA jda;
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public DiscordButtonManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.jda = Main.getJDA();
    }
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        LogicA(e); LogicB(e);
    }
    public void LogicA(@NotNull ButtonInteractionEvent e) {
        switch (e.getButton().getId()) {
            case "agree":
                e.getChannel().retrieveMessageById(e.getMessage().getEmbeds().get(0).getFooter().getText()).queue(message -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(message.getContentDisplay());
                    Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                    if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Date now = new Date();
                                switch (getString("DataBaseType")) {
                                    case "SQLite":
                                        if (!plugin.getClassManager().getSqLiteWhitelistData().getPlayers().contains(message.getContentDisplay())) {
                                            plugin.getClassManager().getSqLiteWhitelistData().addPlayer(message.getContentDisplay(), "player", now);
                                            plugin.getData().save();
                                        }
                                        break;
                                    case "MySQL":
                                        if (!plugin.getClassManager().getMySQLWhitelistData().getPlayers().contains(message.getContentDisplay())) {
                                            plugin.getClassManager().getMySQLWhitelistData().addPlayer(message.getContentDisplay(), "player", now);
                                            plugin.getData().save();
                                        }
                                        break;
                                    default:
                                        if (!plugin.getClassManager().getSqLiteWhitelistData().getPlayers().contains(message.getContentDisplay())) {
                                            plugin.getClassManager().getSqLiteWhitelistData().addPlayer(message.getContentDisplay(), "player", now);
                                            plugin.getData().save();
                                        }
                                        break;
                                }
                            }
                        }.runTaskAsynchronously(plugin);
                    }
                    plugin.getEmbedBuilder().setFooter(null);
                    // System.out.println(message.getContentDisplay()); // Debug string
                    if (!e.getUser().equals(message.getAuthor())) {
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageError")));
                        e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        return;
                    }
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AddedIn").replaceAll("%u", message.getContentDisplay()));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    for (String s : getStringList("SettingsEnabled")) {
                        TextChannel textChannel = e.getGuild().getTextChannelById(plugin.getConfig().getString("LogsChannelID"));
                        TextChannel globalTextChannel = e.getGuild().getTextChannelById(plugin.getConfig().getString("GlobalChannelID"));
                        switch (s) {
                            case "CHANGE_NAME":
                                try {
                                    e.getMember().modifyNickname(message.getContentDisplay()).queue();
                                } catch (Exception ex) {
                                    console.sendMessage(ColorManager.translate("&c > &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                }
                                break;
                            case "SEND_WELCOME_MESSAGE":
                                List<String> listW = plugin.getLanguageManager().getStringList("Welcome-Message");
                                String resultW = StringUtils.join(listW, "\n");
                                switch (getString("WelcomeMessageType")) {
                                    case "EMBED":
                                        if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                                            String mentions = message.getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions));
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);
                                            

                                        } else {
                                            String mentions = message.getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions));
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                                    1, TimeUnit.SECONDS);
                                            
                                        }
                                        break;
                                    case "TEXT":
                                        if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                                            String mentions = message.getAuthor().getAsMention();
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessage(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions))
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);
                                            
                                        } else {
                                            String mentions = message.getAuthor().getAsMention();
                                            globalTextChannel.sendTyping().queue();
                                            EXECUTOR.schedule(() ->  globalTextChannel.sendMessage(resultW.replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mentions)).queue(),
                                                    1, TimeUnit.SECONDS);
                                            
                                        }
                                        break;
                                }
                                break;
                            case "SEND_DM":
                                List<String> list = plugin.getLanguageManager().getStringList("DM-Message");
                                String result = StringUtils.join(list, "\n");
                                EXECUTOR.schedule(() -> message.getAuthor().openPrivateChannel().queue((messages) -> {
                                            plugin.getDMEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("DMEmbedColor")));
                                            plugin.getDMEmbedBuilder().setTitle(plugin.getLanguageManager().get("DMMessageTitle"));
                                            plugin.getDMEmbedBuilder().setDescription(result.replaceAll("%u", message.getContentDisplay()));
                                            messages.sendMessageEmbeds(plugin.getDMEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                                        }),
                                        1, TimeUnit.SECONDS);
                                break;
                            case "WHITELIST_ROLE":
                                if (!Objects.equals(Main.plugin.getConfig().getString("RoleID"), "notuse")) {
                                    try {
                                        if (e.getGuild().getRoleById(plugin.getConfig().getString("RoleID")) != null) {
                                            Role whitelistRole = e.getGuild().getRoleById(plugin.getConfig().getString("RoleID"));
                                            e.getGuild().addRoleToMember(e.getMember(), whitelistRole).queue();
                                        }
                                    } catch (Exception ex) {
                                        console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "REMOVE_ROLE":
                                if (!Objects.equals(Main.plugin.getConfig().getString("RemoveRoleID"), "notuse")) {
                                    try {
                                        if (e.getGuild().getRoleById(plugin.getConfig().getString("RemoveRoleID")) != null) {
                                            Role removeRole = e.getGuild().getRoleById(plugin.getConfig().getString("RemoveRoleID"));
                                            e.getGuild().removeRoleFromMember(e.getMember(), removeRole).queue();
                                        }
                                    } catch (Exception ex) {
                                        console.sendMessage(ColorManager.translate("&c > &fBot can't remove role from user. Either user have higher role that bot, either roleID isn't correct!"));
                                    }
                                }
                                break;
                            case "LOGGING":
                                String mention = message.getAuthor().getAsMention();
                                String discord = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistLogEmbedDescription").replaceAll("%p", message.getContentDisplay()).replaceAll("%u", mention).replaceAll("%d", discord));
                                EXECUTOR.schedule(() -> textChannel.sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                        1, TimeUnit.SECONDS);
                                break;
                        }
                    }
                    e.getInteraction().getMessage().delete().queue();
                    message.delete().queue();
                });
                break;
            case "notagree":
                e.getChannel().retrieveMessageById(e.getMessage().getEmbeds().get(0).getFooter().getText()).queue((message) -> {
                    if (!e.getUser().equals(message.getAuthor())) {
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageError")));
                        e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        
                        return;
                    }
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("TitleRefused")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("MessageRefused")));
                    plugin.getEmbedBuilder().setTimestamp(Instant.now());
                    message.delete().queue();
                    e.getMessage().delete().queue();
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    
                });
                break;
            case "success":
                if (getStringList("SettingsEnabled").contains("REACTIONS_WHITELIST")) {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionSuccess"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    if (!Objects.equals(Main.plugin.getConfig().getString("ReactionsRoleID"), "notuse")) {
                        if (e.getGuild().getRoleById(plugin.getConfig().getString("ReactionsRoleID")) != null)
                            try {
                                Role reactionRole = e.getGuild().getRoleById(plugin.getConfig().getString("ReactionsRoleID"));
                                e.getGuild().addRoleToMember(e.getMember(), reactionRole).queue();
                            } catch (Exception ex) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Seems that user has higher role that bot!"));
                            }
                    }
                    if (!Objects.equals(Main.plugin.getConfig().getString("ReactionsRemoveRoleID"), "notuse")) {
                        if (e.getGuild().getRoleById(plugin.getConfig().getString("ReactionsRemoveRoleID")) != null)
                            try {
                                Role reactionRemoveRole = e.getGuild().getRoleById(plugin.getConfig().getString("ReactionsRemoveRoleID"));
                                e.getGuild().removeRoleFromMember(e.getMember(), reactionRemoveRole).queue();
                            } catch (Exception ex) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't remove role from user. Seems that user has higher role that bot!"));
                            }
                    }
                } else {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionNotEnabled"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    
                }
                break;
            case "default":
                ArrayList<String> defaultWhitelistedPlayers = new ArrayList<String>();
                for (OfflinePlayer dwhitelisted : Bukkit.getWhitelistedPlayers()) {
                    defaultWhitelistedPlayers.add(dwhitelisted.getName());
                }
                String dwhitelistedPlayer = defaultWhitelistedPlayers.toString();
                dwhitelistedPlayer = dwhitelistedPlayer.substring(1, dwhitelistedPlayer.length() - 1);
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListWhitelistedDefaultTitle"));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListWhitelistedDefaultDescription").replaceAll("%p", "" + dwhitelistedPlayer + ""));
                plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListWhitelistedDefaultFooter").replaceAll("%p", String.valueOf(defaultWhitelistedPlayers.size())));
                e.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                        .delay(Duration.ofSeconds(15))
                        .flatMap(InteractionHook::deleteOriginal)
                        .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                
                break;
            case "our":
                List<String> list = plugin.getLanguageManager().getStringList("WhitelistListOurWhitelistChooseDescription");
                String result = StringUtils.join(list, "\n");
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseTitle"));
                plugin.getEmbedBuilder().setDescription(result.replaceAll("%0", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonPlayers")).replaceAll("%1", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonAdministrators")));
                e.replyEmbeds(plugin.getEmbedBuilder().build()).setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.success("players", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonPlayers")), Button.primary("admins", plugin.getLanguageManager().get("WhitelistListOurWhitelistChooseButtonAdministrators"))).setEphemeral(true)
                        .delay(Duration.ofSeconds(15))
                        .flatMap(InteractionHook::deleteOriginal)
                        .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                

                break;
            case "players":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoPlayersSQLite(e, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoPlayersMySQL(e, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoPlayersSQLite(e, plugin.getEmbedBuilder());
                                break;
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
            case "admins":
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoAdminsSQLite(e, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoAdminsMySQL(e, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoAdminsSQLite(e, plugin.getEmbedBuilder());
                                break;
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
        }
    }
    public void LogicB(@NotNull ButtonInteractionEvent e) {
        String playerUUID = plugin.getClassManager().getUserdata().getInfoFromUserProfile("discord_id", e.getInteraction().getUser().getId(), "uuid");
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (e.getButton().getId()) {
                    case "verify":
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoSQLiteVerify(e, playerUUID);
                                break;
                            case "MySQL":
                                break;
                        }
                        break;
                    case "lock":
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoSQLiteLock(e, playerUUID);
                                break;
                            case "MySQL":

                                break;
                        }
                        break;
                    case "help":
                        TextInput FirstLineModal = TextInput.create("first", "I need help", TextInputStyle.PARAGRAPH)
                                .setMaxLength(150)
                                .setMinLength(5)
                                .setRequired(true)
                                .build();
                        Modal CaptchaExtraModal = Modal.create("NeedHelpModal", "I need help yeah!")
                                .addActionRows(ActionRow.of(FirstLineModal))
                                .build();
                        e.replyModal(CaptchaExtraModal).queue();
                        e.editButton(e.getInteraction().getButton().asDisabled()).queue();
                        break;
                    case "switch":
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoSQLiteSwitch(e, playerUUID);
                                break;
                            case "MySQL":

                                break;
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void updateInformation(String playerUUID, Integer newInt, String column) {
        PreparedStatement preparedStatement = null;
        /*try {
            preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_LoginPanel") + " WHERE uuid=?");
            preparedStatement.setString(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                //preparedStatement = SQLite.con.prepareStatement("UPDATE " + getString("SQLiteTableName_LoginPanel") + " SET "+column+"=? WHERE "+column+"=");
                resultSet.last();
                resultSet.beforeFirst();
                resultSet.updateInt("locked", newInt);
                resultSet.updateRow();
            }

          resultSet.close(); preparedStatement.close();*/
        try {
        preparedStatement = SQLite.con.prepareStatement("UPDATE " + getString("SQLiteTableName_LoginPanel") + " SET "+column+"='"+newInt+"' WHERE uuid=?");
        preparedStatement.setString(1, playerUUID);
        preparedStatement.execute(); preparedStatement.executeUpdate(); preparedStatement.closeOnCompletion();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }
    public void DoSQLiteVerify(ButtonInteractionEvent e, String playerUUID) {
        //String playerNickname = Main.getUserdata().getInfoFromUserProfile("discord_id", e.getInteraction().getUser().getId(), "nickname");
        plugin.getClassManager().getPlayerPreLoginHandler().getHashSetUUID().remove(playerUUID);
        plugin.getClassManager().getTimer().StartTimer(playerUUID, 0);
        e.reply("done!").queue();
        e.editButton(e.getInteraction().getButton().asDisabled()).queue();
    }
    public void DoSQLiteLock(ButtonInteractionEvent e, String playerUUID) {
        if (plugin.getClassManager().getInformationSQLite().checkUserAccountIsLocked(playerUUID, 0)) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                public void run() {
                    updateInformation(playerUUID, 1, "locked");
                    Player player = getPlayer(UUID.fromString(playerUUID));
                    if (player != null && player.isOnline()) player.kickPlayer(ColorManager.translate("&cYour account is locked by Discord request!"));
                    plugin.getEmbedBuilder().setColor(Color.ORANGE);
                    plugin.getEmbedBuilder().setTitle("LOCKED");
                    e.replyEmbeds(plugin.getEmbedBuilder().build()).queue();
                    
                }
            });
        } else {
            updateInformation(playerUUID, 0, "locked");
            plugin.getEmbedBuilder().setColor(Color.ORANGE);
            plugin.getEmbedBuilder().setTitle("UNLOCKED");
            e.replyEmbeds(plugin.getEmbedBuilder().build()).queue();
            
        }
    }
    public void DoSQLiteSwitch(ButtonInteractionEvent e, String playerUUID) {
        if (plugin.getClassManager().getInformationSQLite().isUserLoginPanelUseIt(playerUUID, 1)) {
            updateInformation(playerUUID, 0, "using_panel");
            plugin.getEmbedBuilder().setColor(Color.ORANGE);
            plugin.getEmbedBuilder().setTitle("NOW NOT USING");
            e.replyEmbeds(plugin.getEmbedBuilder().build()).queue();
            
        } else {
            updateInformation(playerUUID, 1, "using_panel");
            plugin.getEmbedBuilder().setColor(Color.GREEN);
            plugin.getEmbedBuilder().setTitle("NOW USING");
            e.replyEmbeds(plugin.getEmbedBuilder().build()).queue();
            
        }
    }
    public void DoPlayersSQLite(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getSqLiteWhitelistData().getPlayers() != null) {
                    /*ArrayList<String> ourWhitelistedPlayers = new ArrayList<String>();
                    for (String oWhitelistedPlayer : Main.plugin.getData().getPlayers()) {
                        ourWhitelistedPlayers.add(oWhitelistedPlayer);
                    }*/
                    String oWhitelisted = plugin.getClassManager().getSqLiteWhitelistData().getPlayers().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getPlayers().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public void DoPlayersMySQL(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getMySQLWhitelistData().getPlayers() != null) {
                    /*ArrayList<String> ourWhitelistedPlayers = new ArrayList<String>();
                    for (String oWhitelistedPlayer : Main.plugin.getData().getPlayers()) {
                        ourWhitelistedPlayers.add(oWhitelistedPlayer);
                    }*/
                    String oWhitelisted = plugin.getClassManager().getMySQLWhitelistData().getPlayers().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getPlayers().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public void DoAdminsSQLite(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getSqLiteWhitelistData().getAdministrators() != null) {
                    /*ArrayList<String> ourWhitelistedPlayers = new ArrayList<String>();
                    for (String oWhitelistedPlayer : Main.plugin.getData().getPlayers()) {
                        ourWhitelistedPlayers.add(oWhitelistedPlayer);
                    }*/
                    String oWhitelisted = plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getSqLiteWhitelistData().getAdministrators().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
        }
    public void DoAdminsMySQL(ButtonInteractionEvent e, EmbedBuilder eb) {
                if (plugin.getClassManager().getMySQLWhitelistData().getAdministrators() != null) {
                    /*ArrayList<String> ourWhitelistedPlayers = new ArrayList<String>();
                    for (String oWhitelistedPlayer : Main.plugin.getData().getPlayers()) {
                        ourWhitelistedPlayers.add(oWhitelistedPlayer);
                    }*/
                    String oWhitelisted = plugin.getClassManager().getMySQLWhitelistData().getAdministrators().toString();
                    oWhitelisted = oWhitelisted.substring(1, oWhitelisted.length() - 1);
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                    eb.setTitle(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistTitle"));
                    eb.setDescription(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistDescription").replaceAll("%p", "" + oWhitelisted + ""));
                    eb.setFooter(plugin.getLanguageManager().get("ListAdministratorsWhitelistedOurWhitelistFooter").replaceAll("%p", String.valueOf(plugin.getClassManager().getMySQLWhitelistData().getAdministrators().size())));
                    e.replyEmbeds(eb.build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(60))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    
                }
            }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
    public void clearEmbedBuilder() {
        EXECUTOR.schedule(() -> plugin.getEmbedBuilder().clear(),
                11, TimeUnit.MILLISECONDS);
    }
}
