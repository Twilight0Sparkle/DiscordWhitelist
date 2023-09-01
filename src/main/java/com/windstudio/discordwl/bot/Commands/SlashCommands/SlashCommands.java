package com.windstudio.discordwl.bot.Commands.SlashCommands;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Commands.IngameCommands.LinkingCommand;
import com.windstudio.discordwl.bot.DataBase.SQLite.SQLite;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SlashCommands extends ListenerAdapter implements Listener {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public ConsoleCommandSender console;
    private final Main plugin;
    JDA jda = Main.getJDA();
    public SlashCommands(Main plugin) { this.plugin = plugin; }
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // System.out.println(event.getName() + event.getOptions().toString()); // Debug
        switch (event.getName()) {
            case "whitelist":
                OptionMapping typeOption = event.getOption("type");
                OptionMapping nickOption = event.getOption("username");
                if (typeOption == null) {
                    event.reply("You need to choose one of this options: add/remove").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String TYPE = typeOption.getAsString();
                switch (TYPE) {
                    case "add":
                        if (nickOption == null) {
                            event.reply("You need to write valid nickname to add/remove it!").setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            return;
                        }
                        String NICK = nickOption.getAsString();
                        if (!NICK.matches("^\\w{3,16}$") && getStringList("SettingsEnabled").contains("SLASHCOMMANDS_REGEX_CHECK")) {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            return;
                        }
                        OfflinePlayer p = Bukkit.getOfflinePlayer(NICK);
                        if (!p.isWhitelisted()) {
                            Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                            if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                                    public void run() {
                                        Date now = new Date();
                                        switch (getString("DataBaseType")) {
                                            case "SQLite":
                                                plugin.getClassManager().getSqLiteWhitelistData().addPlayer(NICK, "player", now);
                                                plugin.getData().save();
                                                break;
                                            case "MySQL":
                                                plugin.getClassManager().getMySQLWhitelistData().addPlayer(NICK, "player", now);
                                                plugin.getData().save();
                                                break;
                                        }
                                    } });
                            }
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistedSuccessful").replaceAll("%p", NICK));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.INVALID_FORM_BODY));
                            if (getStringList("SettingsEnabled").contains("LOGGING")) {
                                String mention = event.getInteraction().getMember().getAsMention();
                                String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistSlashCommandAddLogEmbedDescription").replaceAll("%a", mention).replaceAll("%d", discord).replaceAll("%p", NICK));
                                event.getGuild().getTextChannelById(getString("LogsChannelID")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                            }
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistedAlready").replaceAll("%p", NICK));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        }
                        break;
                    case "remove":
                        if (nickOption == null) {
                            event.reply("You need to write valid nickname to add/remove it!").setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            return;
                        }
                        String NICKNAME = nickOption.getAsString();
                        if (!NICKNAME.matches("^\\w{3,16}$") && getStringList("SettingsEnabled").contains("SLASHCOMMANDS_REGEX_CHECK")) {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.INVALID_FORM_BODY));
                            return;
                        }
                        OfflinePlayer player = Bukkit.getOfflinePlayer(NICKNAME);
                        if (player.isWhitelisted()) {
                            Bukkit.getScheduler().runTask(plugin, () -> player.setWhitelisted(false));
                            if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                                    public void run() {
                                        switch (getString("DataBaseType")) {
                                            case "SQLite":
                                                plugin.getClassManager().getSqLiteWhitelistData().removePlayer("nickname", NICKNAME);
                                                plugin.getData().save();
                                                break;
                                            case "MySQL":
                                                plugin.getClassManager().getMySQLWhitelistData().removePlayer("nickname", NICKNAME);
                                                plugin.getData().save();
                                                break;
                                        }
                                    } });
                            }
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRemovedSuccessful").replaceAll("%p", NICKNAME));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            if (getStringList("SettingsEnabled").contains("LOGGING")) {
                                String mention = event.getInteraction().getMember().getAsMention();
                                String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistSlashCommandRemovedLogEmbedDescription").replaceAll("%a", mention).replaceAll("%d", discord).replaceAll("%p", NICKNAME));
                                event.getGuild().getTextChannelById(getString("LogsChannelID")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                            }
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("NotInWhitelist").replaceAll("%p", NICKNAME));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        }
                        break;
                    case "list":
                        List<String> list = plugin.getLanguageManager().getStringList("WhitelistListChooseDescription");
                        String result = StringUtils.join(list, "\n");
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistListChooseTitle"));
                        plugin.getEmbedBuilder().setDescription(result.replaceAll("%0", plugin.getLanguageManager().get("WhitelistListChooseDefaultWhitelistButton")).replaceAll("%1", plugin.getLanguageManager().get("WhitelistListChooseOurWhitelistButton")));
                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("default", plugin.getLanguageManager().get("WhitelistListChooseDefaultWhitelistButton")), Button.primary("our", plugin.getLanguageManager().get("WhitelistListChooseOurWhitelistButton"))).setEphemeral(true)
                                .delay(Duration.ofSeconds(15))
                                .flatMap(InteractionHook::deleteOriginal)
                                .queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        break;
                }
                break;
            case "checkwhitelist":
                OptionMapping optionWhitelist = event.getOption("whitelisttype");
                if (optionWhitelist == null) {
                    event.reply("You need to provide valid whitelist type: default/our").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                OptionMapping optionNick = event.getOption("username");
                if (optionNick == null) {
                    event.reply("You need to provide valid username").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String type = optionWhitelist.getAsString();
                String nickname = optionNick.getAsString();
                switch (type) {
                    case "default":
                        ArrayList<String> defaultWhitelistedPlayers = new ArrayList<String>();
                        for (OfflinePlayer dwhitelisted : Bukkit.getWhitelistedPlayers()) {
                            defaultWhitelistedPlayers.add(dwhitelisted.getName());
                        }
                        if (defaultWhitelistedPlayers.contains(nickname)) {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        }
                        if (getStringList("SettingsEnabled").contains("LOGGING")) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID"));
                            String mention = event.getInteraction().getMember().getAsMention();
                            String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                            plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                            plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                            plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckWhitelistLogEmbedDescription").replaceAll("%p", nickname).replaceAll("%a", mention).replaceAll("%d", discord));
                            EXECUTOR.schedule(() -> textChannel.sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                    1, TimeUnit.SECONDS);
                        }
                        break;
                    case "our":
                        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                            public void run() {
                                checkDB(plugin.getEmbedBuilder(), event, nickname);
                                if (getStringList("SettingsEnabled").contains("LOGGING")) {
                                    TextChannel textChannel = event.getGuild().getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID"));
                                    String mention = event.getInteraction().getMember().getAsMention();
                                    String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                                    plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                    plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                                    plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckWhitelistLogEmbedDescription").replaceAll("%p", nickname).replaceAll("%a", mention).replaceAll("%d", discord));
                                    EXECUTOR.schedule(() -> textChannel.sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                            1, TimeUnit.SECONDS);
                                }
                            } });
                    }
                break;
            case "checklink":
                OptionMapping optionNickname = event.getOption("username");
                OptionMapping optionDID = event.getOption("did");
                new BukkitRunnable() {
                    public void run() {
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                if (optionNickname == null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replace("%u", uuID).replace("%n", name).replace("%t", date).replace("%d", discord).replace("%i", d_id).replace("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close(); preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (optionNickname != null && optionDID == null) {
                                    String nick = optionNickname.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("nickname", nick)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE nickname=?");
                                        preparedStatement.setString(1, nick);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(d_id).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close();
                                        preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (optionNickname != null && optionDID != null) {
                                    String nick = optionNickname.getAsString();
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdata().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                .delay(Duration.ofSeconds(15))
                                                .flatMap(InteractionHook::deleteOriginal)
                                                .queue(null, new ErrorHandler()
                                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    try {
                                        preparedStatement = SQLite.con.prepareStatement("SELECT * FROM " + getString("SQLiteTableName_Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        ResultSet resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                                    .delay(Duration.ofSeconds(60))
                                                    .flatMap(InteractionHook::deleteOriginal)
                                                    .queue(null, new ErrorHandler()
                                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                        }
                                        resultSet.close();
                                        preparedStatement.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                                            .delay(Duration.ofSeconds(15))
                                            .flatMap(InteractionHook::deleteOriginal)
                                            .queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                }
                                break;
                            case "MySQL":
                                //event.deferReply(true).queue();
                                if (optionNickname == null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else if (optionNickname != null && optionDID == null) {
                                    String nick = optionNickname.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("nickname", nick)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE nickname=?");
                                        preparedStatement.setString(1, nick);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(d_id).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(60, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else if (optionNickname != null && optionDID != null) {
                                    String did = optionDID.getAsString();
                                    if (!plugin.getClassManager().getUserdataMySQL().userProfileExistsString("discord_id", did)) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        return;
                                    }
                                    if (did.length() != 18) {
                                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckDiscordIDNotFound"));
                                        event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                        return;
                                    }
                                    PreparedStatement preparedStatement = null;
                                    ResultSet resultSet = null;
                                    try {
                                        preparedStatement = plugin.getPoolManager().getConnection().prepareStatement("SELECT * FROM " + getString("MySQL_TableName_Linking") + " WHERE discord_id=?");
                                        preparedStatement.setString(1, did);
                                        resultSet = preparedStatement.executeQuery();
                                        List<String> list = plugin.getLanguageManager().getStringList("LinkingCheckFoundDescription");
                                        String result = StringUtils.join(list, "\n");
                                        while (resultSet.next()) {
                                            String uuID = resultSet.getString("uuid");
                                            String name = resultSet.getString("nickname");
                                            String discord = resultSet.getString("discord");
                                            String d_id = resultSet.getString("discord_id");
                                            String date = resultSet.getString("linking_date");
                                            String mention = event.getGuild().getMemberById(did).getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckFoundTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(result.replaceAll("%u", uuID).replaceAll("%n", name).replaceAll("%t", date).replaceAll("%d", discord).replaceAll("%i", d_id).replaceAll("%m", mention));
                                            event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(60, TimeUnit.SECONDS, Message::delete);
                                        }
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    } finally {
                                        plugin.getPoolManager().close(null, preparedStatement, resultSet);
                                    }
                                } else {
                                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingCheckNotFoundTitle"));
                                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingCheckNotFoundDescription"));
                                    event.getInteraction().getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queueAfter(15, TimeUnit.SECONDS, Message::delete);
                                }
                                break;
                        }
                        if (getStringList("SettingsEnabled").contains("LOGGING")) {
                            TextChannel textChannel = event.getGuild().getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID"));
                            String mention = event.getInteraction().getMember().getAsMention();
                            String discord = event.getInteraction().getUser().getName() + "#" + event.getInteraction().getUser().getDiscriminator();
                            plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                            plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("CheckLogEmbedTitle"));
                            if (optionNickname == null && optionDID != null) {
                                String did = optionDID.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkDiscordIDLogEmbedDescription").replaceAll("%a", mention).replaceAll("%p", did).replaceAll("%i", discord));
                            } else if (optionNickname != null && optionDID == null) {
                                String nick = optionNickname.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkNickLogEmbedDescription").replaceAll("%p", nick).replaceAll("%a", mention).replaceAll("%i", discord));
                            } else if (optionNickname != null && optionDID != null) {
                                String nick = optionNickname.getAsString();
                                String did = optionDID.getAsString();
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("CheckLinkLogEmbedDescription").replaceAll("%p", nick).replaceAll("%a", mention).replaceAll("%d", did).replaceAll("%i", discord).replaceAll("%l", did));
                            }
                            EXECUTOR.schedule(() -> textChannel.sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                    1, TimeUnit.SECONDS);
                        }
                    }
                }.runTaskAsynchronously(plugin);
                break;
            case "setupreaction":
                if (getStringList("SettingsEnabled").contains("REACTIONS_WHITELIST")) {
                    String title = plugin.getLanguageManager().get("ReactionEmbedTitle");
                    String color = plugin.getLanguageManager().get("ReactionEmbedColor");
                    List<String> listOne = plugin.getLanguageManager().getStringList("ReactionEmbedDescription");
                    String description = StringUtils.join(listOne, "\n");
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("SuccessTitle"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ConfirmMenuEmbedColor")));
                    event.getHook().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                    plugin.getEmbedBuilder().setTitle(title);
                    plugin.getEmbedBuilder().setDescription(description);
                    plugin.getEmbedBuilder().setColor(Color.decode(color.toString()));
                    event.getInteraction().getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("success", plugin.getLanguageManager().get("ReactionButton"))).queue();
                } else {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ReactionNotEnabled"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                }
                break;
            case "list":
                OptionMapping optionOption = event.getOption("list");
                if (optionOption == null) {
                    event.reply("You need to choose valid option to use it!").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                    String OPTION = optionOption.getAsString();
                    switch (OPTION) {
                        case "players":
                            ArrayList<String> onlineUsualPlayers = new ArrayList<String>();
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                onlineUsualPlayers.add(players.getName());
                            }
                            String online = onlineUsualPlayers.toString();
                            online = online.substring(1, online.length() - 1);
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListPlayersTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListPlayersDescription").replaceAll("%p", "" + online + ""));
                            plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListPlayersFooter").replaceAll("%p", String.valueOf(onlineUsualPlayers.size())));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            break;
                        case "banned":
                            ArrayList<String> BannedPlayers = new ArrayList<String>();
                            for (OfflinePlayer banned : Bukkit.getBannedPlayers()) {
                                BannedPlayers.add(banned.getName());
                            }
                            String banned = BannedPlayers.toString();
                            banned = banned.substring(1, banned.length() - 1);
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ListEmbedColor")));
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ListBannedPlayersTitle"));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ListBannedPlayersDescription").replaceAll("%p", "" + banned + ""));
                            plugin.getEmbedBuilder().setFooter(plugin.getLanguageManager().get("ListBannedPlayersFooter").replaceAll("%p", String.valueOf(BannedPlayers.size())));
                            event.replyEmbeds(plugin.getEmbedBuilder().build())
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                            break;
                    }
                    break;
            case "account":
                OptionMapping type1Option = event.getOption("type");
                OptionMapping nick1Option = event.getOption("username");
                if (!getStringList("SettingsEnabled").contains("LINKING")) {
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingTurnedOffEmbed"));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                if (type1Option == null) {
                    event.reply("You need to choose one of this options: link/unlink").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                if (nick1Option == null) {
                    event.reply("You need to write valid nickname to add or remove it!").setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                String TYPE1 = type1Option.getAsString();
                String NICK1 = nick1Option.getAsString();
                if (!NICK1.matches("^\\w{3,16}$") && getStringList("SettingsEnabled").contains("SLASHCOMMANDS_REGEX_CHECK")) {
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistRegexErrorDescription"));
                    event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true)
                            .delay(Duration.ofSeconds(15))
                            .flatMap(InteractionHook::deleteOriginal)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    return;
                }
                Player player = Bukkit.getPlayerExact(NICK1);
                OfflinePlayer p1 = Bukkit.getOfflinePlayer(NICK1);
                switch (TYPE1) {
                    case "link":
                        event.deferReply();
                        if (!p1.isOnline()) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandPlayerNotOnlne"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        String playerUUID = player.getUniqueId().toString();

                                    switch (getString("DataBaseType")) {
                                        case "SQLite":
                                            if (plugin.getClassManager().getUserdata().userProfileExists(playerUUID)) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                return;
                                            }
                                            break;
                                        case "MySQL":
                                            if (plugin.getClassManager().getUserdataMySQL().userProfileExists(playerUUID)) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                                return;
                                            }
                                            break;
                                    }

                        if ((event.getMember().getRoles().stream().filter(role -> role.getName().equals(Main.plugin.getConfig().getString("LinkedRoleID"))).findAny().orElse(null) != null)) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAlreadyLinked"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        if (NICK1.length() <= 3 || NICK1.length() > 16) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNicknameError"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        if (LinkingCommand.getUuidIdMap().containsValue(event.getInteraction().getUser().getId())) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandCodeGenerated"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        LinkingCommand.getUuidCodeMap().put(p1.getUniqueId(), plugin.getClassManager().getLinkingCommand().getCODE());
                        LinkingCommand.getUuidIdMap().put(p1.getUniqueId(), event.getInteraction().getUser().getId());
                        plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                        plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                        plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandLink").replaceAll("%u", plugin.getClassManager().getLinkingCommand().getCODE()));
                        event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                        break;
                    case "unlink":
                        if (!p1.isOnline()) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandPlayerNotOnlne"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        //event.getInteraction().getHook().sendMessage("...").setEphemeral(true).queue();
                        String playerOUUID = player.getUniqueId().toString();
                        if (event.getMember().getRoles().stream().filter(role -> role.getName().equals(Main.plugin.getConfig().getString("LinkedRoleID"))).findAny().orElse(null) == null) {
                            new BukkitRunnable() {
                                public void run() {
                                    switch (getString("DataBaseType")) {
                                        default:
                                        case "SQLite":
                                            if (!(plugin.getClassManager().getUserdata().userProfileExists(playerOUUID))) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNotLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                            }
                                            break;
                                        case "MySQL":
                                            if (!(plugin.getClassManager().getUserdataMySQL().userProfileExists(playerOUUID))) {
                                                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                                                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                                                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNotLinked"));
                                                event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                                            }
                                            break;
                                    }
                                }
                            }.runTaskAsynchronously(plugin);
                        }
                        if (NICK1.length() <= 3 || NICK1.length() > 16) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandNicknameError"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        if (LinkingCommand.getUuidCodeMap().containsValue(event.getInteraction().getUser().getId())) {
                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingSlashCommandCodeGenerated"));
                            event.replyEmbeds(plugin.getEmbedBuilder().build()).setEphemeral(true).queue();
                            return;
                        }
                        //System.out.println(Main.getUserdata().getSingleInformationFromUserProfile(player, "uuid", player.getUniqueId().toString(), "discord_id"));
                        switch (getString("DataBaseType")) {
                            case "SQLite":
                                DoSQLite(player, event, plugin.getEmbedBuilder());
                                break;
                            case "MySQL":
                                DoMySQL(player, event, plugin.getEmbedBuilder());
                                break;
                            default:
                                DoSQLite(player, event, plugin.getEmbedBuilder());
                                break;
                        }
                        break;
                }
                break;
        }
    }
    public void DoSQLite(Player player, SlashCommandInteractionEvent event, EmbedBuilder eb) {
        new BukkitRunnable() {
            public void run() {
                if (plugin.getClassManager().getUserdata().getSingleInformationFromUserProfile("uuid", player.getUniqueId().toString(), "discord_id").equals(event.getInteraction().getUser().getId())) {
                    if (event.getInteraction().getMember().getRoles().contains(Main.plugin.getConfig().getString("LinkedRoleID"))) {
                        event.getInteraction().getMember().getRoles().remove(Main.plugin.getConfig().getString("LinkedRoleID"));
                    }
                    plugin.getClassManager().getUserdata().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    eb.setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandUnLink"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    String Discord = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
                    player.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("LinkingUnlinkedMessage").replaceAll("%u", Discord)));
                    if (plugin.getConfig().getString("LinkedRoleID") != null) {
                        if (Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID")) != null) {
                            try {
                                Role verifiedRole = Main.getJDA().getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID"));
                                Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).removeRoleFromMember(event.getMember(), verifiedRole).queue();
                            } catch (Exception e) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't add you role!"));
                            }
                        }
                    }
                    if (!Objects.equals(plugin.getConfig().getString("LinkedRemoveRoleID"), "notuse")) {
                        if (Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID")) != null) {
                            try {
                                Role verifiedRemoveRole = Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID"));
                                Main.getJDA().getGuildById(Main.plugin.getConfig().getString("GuildID")).addRoleToMember(event.getMember(), verifiedRemoveRole).queue();
                            } catch (Exception e) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't remove role from user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't remove role from you!"));
                            }
                        }
                    }
                    if (getStringList("SettingsEnabled").contains("UNLINK_NAME_CHANGE")) event.getMember().modifyNickname(null).queue();
                    if (getStringList("SettingsEnabled").contains("LOGGING")) {
                        String mention = event.getInteraction().getMember().getAsMention();
                        plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                        plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingLogTitle"));
                        plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingLogUnLinkedDescription").replaceAll("%u", mention).replaceAll("%d", Discord).replaceAll("%p", player.getName()).replaceAll("%i", player.getUniqueId().toString()));
                        event.getGuild().getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                    }
                } else {
                    eb.setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAccountNotYours"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void DoMySQL(Player player, SlashCommandInteractionEvent event, EmbedBuilder eb) {
        new BukkitRunnable() {
            public void run() {
                if (plugin.getClassManager().getUserdataMySQL().getSingleInformationFromUserProfile("uuid", player.getUniqueId().toString(), "discord_id").equals(event.getInteraction().getUser().getId())) {
                    if (event.getInteraction().getMember().getRoles().contains(Main.plugin.getConfig().getString("LinkedRoleID"))) {
                        event.getInteraction().getMember().getRoles().remove(Main.plugin.getConfig().getString("LinkedRoleID"));
                    }
                    plugin.getClassManager().getUserdataMySQL().deleteInformationFromUserProfile("uuid", player.getUniqueId().toString());
                    eb.setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandUnLink"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    String Discord = event.getUser().getName() + "#" + event.getUser().getDiscriminator();
                    player.sendMessage(ColorManager.translate(plugin.getLanguageManager().get("LinkingUnlinkedMessage").replaceAll("%u", Discord)));
                    if (plugin.getConfig().getString("LinkedRoleID") != null) {
                        if (Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID")) != null) {
                            try {
                                Role verifiedRole = Main.getJDA().getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRoleID"));
                                Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).removeRoleFromMember(event.getMember(), verifiedRole).queue();
                            } catch (Exception e) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't add role to user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't add you role!"));
                            }
                        }
                    }
                    if (!Objects.equals(plugin.getConfig().getString("LinkedRemoveRoleID"), "notuse")) {
                        if (Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID")) != null) {
                            try {
                                Role verifiedRemoveRole = Main.getJDA().getGuildById(Main.plugin.getConfig().getString("GuildID")).getRoleById(Main.plugin.getConfig().getString("LinkedRemoveRoleID"));
                                Main.getJDA().getGuildById(plugin.getConfig().getString("GuildID")).addRoleToMember(event.getMember(), verifiedRemoveRole).queue();
                            } catch (Exception e) {
                                console.sendMessage(ColorManager.translate("&c > &fBot can't remove role from user. Seems that user has higher role that bot!"));
                                player.sendMessage(ColorManager.translate("&cBot can't remove role from you!"));
                            }
                        }
                    }
                    if (getStringList("SettingsEnabled").contains("UNLINK_NAME_CHANGE")) event.getMember().modifyNickname(null).queue();
                    if (getStringList("SettingsEnabled").contains("LOGGING")) {
                        String mention = event.getInteraction().getMember().getAsMention();
                        plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                        plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("LinkingLogTitle"));
                        plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("LinkingLogUnLinkedDescription").replaceAll("%u", mention).replaceAll("%d", Discord).replaceAll("%p", player.getName()).replaceAll("%i", player.getUniqueId().toString()));
                        event.getGuild().getTextChannelById(Main.plugin.getConfig().getString("LogsChannelID")).sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                .ignore(ErrorResponse.UNKNOWN_CHANNEL));
                    }
                } else {
                    eb.setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    eb.setDescription(plugin.getLanguageManager().get("LinkingSlashCommandAccountNotYours"));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public void checkDB(EmbedBuilder eb, SlashCommandInteractionEvent event, String nickname) {
        new BukkitRunnable() {
            public void run() {
                switch (getString("DataBaseType")) {
                    case "SQLite":
                        if (plugin.getClassManager().getSqLiteWhitelistData().userPlayerExists(nickname)) {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundOurWhitelistDescription").replaceAll("%t", plugin.getClassManager().getSqLiteWhitelistData().getPlayerType("nickname", nickname, "player_type")).replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        }
                        break;
                    case "MySQL":
                        if (plugin.getClassManager().getMySQLWhitelistData().userPlayerExists(nickname)) {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckFoundOurWhitelistDescription").replaceAll("%t", plugin.getClassManager().getMySQLWhitelistData().getPlayerType("nickname", nickname, "player_type")).replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        } else {
                            eb.setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                            eb.setTitle(plugin.getLanguageManager().get("WhitelistCheckNotFoundTitle"));
                            eb.setDescription(plugin.getLanguageManager().get("WhitelistCheckNotFoundDescription").replaceAll("%p", nickname));
                            event.replyEmbeds(eb.build()).setEphemeral(true)
                                    .delay(Duration.ofSeconds(15))
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue(null, new ErrorHandler()
                                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        }
                        break;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
    public List<String> getStringList(String path){
        return Main.plugin.getConfig().getStringList(path);
    }
}