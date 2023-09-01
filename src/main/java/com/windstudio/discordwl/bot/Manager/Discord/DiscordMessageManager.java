package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.Main;
import com.windstudio.discordwl.bot.Manager.Plugin.ClassManager;
import com.windstudio.discordwl.bot.Manager.Plugin.ColorManager;
import com.windstudio.discordwl.bot.Manager.Plugin.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.windstudio.discordwl.Main.console;

public class DiscordMessageManager extends ListenerAdapter {
    private final Main plugin;
    private final JDA jda;
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public DiscordMessageManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.jda = Main.getJDA();
    }
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        String argument = e.getMessage().getContentDisplay();
        String argument2 = e.getMessage().getId();
        if (!(e.getMessage().getAuthor().isBot() || e.isWebhookMessage()) && (Objects.equals(plugin.getConfig().getString("WhitelistChannelID"), e.getChannel().getId()) && e.isFromType(ChannelType.TEXT))) {
            if ((argument.length() < 3) || (argument.length() > 16)) {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("LengthError")));
                e.getChannel().sendTyping().queue();
                if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                            .delay(Duration.ofSeconds(15))
                            .flatMap(Message::delete)
                            .queue(null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                            1, TimeUnit.SECONDS);
                } else {
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                            1, TimeUnit.SECONDS);
                }
                return;
            }
            List<String> IDs = Main.plugin.getConfig().getStringList("Blacklist-ID");
            if (getStringList("SettingsEnabled").contains("BLACKLIST")) {
                if (plugin.getConfig().getStringList("Blacklist-nick").contains(argument) || IDs.contains(e.getMessage().getAuthor().getId())) {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ErrorTitle"));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("Blacklist-message").replaceAll("%u", argument));
                    e.getChannel().sendTyping().queue();
                    if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                        .delay(Duration.ofSeconds(15))
                                        .flatMap(Message::delete)
                                        .queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                1, TimeUnit.SECONDS);
                    } else {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()),
                                1, TimeUnit.SECONDS);
                        }
                    return;
                    }
                }
            if (!getStringList("SettingsEnabled").contains("FLOODGATE_BEDROCK") && argument.matches("^\\w{3,16}$")) {
                LogicA(e);
            } else if (getStringList("SettingsEnabled").contains("FLOODGATE_BEDROCK") && argument.matches("^(["+getString("Floodgate_Bedrock_Symbol")+"])?([a-zA-Z0-9_ ]{3,16})$")) {
                LogicA(e);
            } else {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get(("SymbolsError")));
                e.getChannel().sendTyping().queue();
                if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                    e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                            .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                            .delay(Duration.ofSeconds(15))
                            .flatMap(Message::delete)
                            .queue(null, new ErrorHandler()
                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                            1, TimeUnit.SECONDS);
                } else {
                    EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                            1, TimeUnit.SECONDS);
                }
            }
        }
    }
    private void LogicA(@NotNull MessageReceivedEvent e) {
        TextChannel textChannel = e.getGuild().getTextChannelById(plugin.getConfig().getString("LogsChannelID"));
        TextChannel globalTextChannel = e.getGuild().getTextChannelById(plugin.getConfig().getString("GlobalChannelID"));
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String argument = e.getMessage().getContentDisplay();
        String argument2 = e.getMessage().getId();
        OfflinePlayer p = Bukkit.getOfflinePlayer(argument);
        Guild guild = e.getGuild();
        if (p.isWhitelisted()) {
            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("ErrorTitle")));
            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ErrorEmbedColor")));
            plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AlreadyIn").replaceAll("%u", argument));
            e.getChannel().sendTyping().queue();
            if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                EXECUTOR.schedule(() -> e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                .delay(Duration.ofSeconds(15))
                                .flatMap(Message::delete)
                                .queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
            } else {
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                        1, TimeUnit.SECONDS);
            }
        }
        if (!p.isWhitelisted()) {
            if (getStringList("SettingsEnabled").contains("CONFIRM_MENU")) {
                plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("ConfirmTitle"));
                plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("ConfirmMenuEmbedColor")));
                plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("ConfirmMessage").replaceAll("%u", argument));
                plugin.getEmbedBuilder().setFooter(argument2);
                plugin.getEmbedBuilder().setTimestamp(Instant.now());
                e.getChannel().sendTyping().queue();
                EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).setActionRow(Button.success("agree", plugin.getLanguageManager().get("ConfirmButtonYes")), Button.danger("notagree", plugin.getLanguageManager().get("ConfirmButtonNo")))
                                .delay(Duration.ofSeconds(15))
                                .flatMap(Message::delete)
                                .queue(null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                        1, TimeUnit.SECONDS);
                e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                plugin.getEmbedBuilder().setTimestamp(null);
            } else {
                if (!getStringList("SettingsEnabled").contains("CONFIRM_MENU")) {
                    plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get(("SuccessTitle")));
                    plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("SuccessEmbedColor")));
                    plugin.getEmbedBuilder().setDescription(plugin.getLanguageManager().get("AddedIn").replaceAll("%u", argument));
                    e.getChannel().sendTyping().queue();
                    Bukkit.getScheduler().runTask(plugin, () -> p.setWhitelisted(true));
                    if (getStringList("SettingsEnabled").contains("OUR_WHITELIST_SYSTEM")) {
                        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
                            public void run() {
                                Date now = new Date();
                                switch (getString("DataBaseType")) {
                                    case "SQLite":
                                        plugin.getClassManager().getSqLiteWhitelistData().addPlayer(argument, "player", now);
                                        plugin.getData().save();
                                        break;
                                    case "MySQL":
                                        plugin.getClassManager().getMySQLWhitelistData().addPlayer(argument, "player", now);
                                        plugin.getData().save();
                                        break;
                                }
                            } });
                    }
                    if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                        .delay(Duration.ofSeconds(15))
                                        .flatMap(Message::delete)
                                        .queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                1, TimeUnit.SECONDS);
                    } else {
                        EXECUTOR.schedule(() -> e.getChannel().sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                1, TimeUnit.SECONDS);
                    }
                    for (String s : getStringList("SettingsEnabled")) {
                        switch (s) {
                            case "EMPHERIAL_MESSAGES":
                                e.getMessage().delete().queueAfter(15, TimeUnit.SECONDS, null, new ErrorHandler()
                                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
                                break;
                            case "CHANGE_NAME":
                                try {
                                    EXECUTOR.schedule(() -> e.getMember().modifyNickname(argument).queue(),
                                            1, TimeUnit.SECONDS);
                                } catch (Exception ex) {
                                    console.sendMessage(ColorManager.translate("&c > &fBot can't change user's nickname. Seems that user has higher role that bot!"));
                                }
                                break;
                            case "SEND_DM":
                                List<String> list = plugin.getLanguageManager().getStringList("DM-Message");
                                String result = StringUtils.join(list, "\n");
                                EXECUTOR.schedule(() -> e.getMessage().getAuthor().openPrivateChannel().queue(messages -> {
                                            plugin.getDMEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("DMEmbedColor")));
                                            plugin.getDMEmbedBuilder().setTitle(plugin.getLanguageManager().get("DMMessageTitle"));
                                            plugin.getDMEmbedBuilder().setDescription(result.replaceAll("%u", argument));
                                    messages.sendMessageEmbeds(plugin.getDMEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                    .ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER));
                                        }),
                                        1, TimeUnit.SECONDS);
                                break;
                            case "SEND_WELCOME_MESSAGE":
                                List<String> listW = plugin.getLanguageManager().getStringList("Welcome-Message");
                                String resultW = StringUtils.join(listW, "\n");
                                switch (getString("WelcomeMessageType")) {
                                    case "EMBED":
                                        if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                                            String mentions = e.getMessage().getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", argument).replaceAll("%u", mentions));
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build())
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);
                                        } else {
                                            String mentions = e.getMessage().getAuthor().getAsMention();
                                            plugin.getEmbedBuilder().setTitle(plugin.getLanguageManager().get("WelcomeMessageTitle"));
                                            plugin.getEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("WelcomeMessageEmbedColor")));
                                            plugin.getEmbedBuilder().setDescription(resultW.replaceAll("%p", argument).replaceAll("%u", mentions));
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessageEmbeds(plugin.getEmbedBuilder().build()).queue(),
                                                    1, TimeUnit.SECONDS);
                                        }
                                        break;
                                    case "TEXT":
                                        if (getStringList("SettingsEnabled").contains("EMPHERIAL_MESSAGES")) {
                                            String mentions = e.getMessage().getAuthor().getAsMention();
                                            EXECUTOR.schedule(() -> globalTextChannel.sendMessage(resultW.replaceAll("%p", argument).replaceAll("%u", mentions))
                                                            .delay(Duration.ofSeconds(60))
                                                            .flatMap(Message::delete)
                                                            .queue(null, new ErrorHandler()
                                                                    .ignore(ErrorResponse.UNKNOWN_MESSAGE)),
                                                    1, TimeUnit.SECONDS);
                                        } else {
                                            String mentions = e.getMessage().getAuthor().getAsMention();
                                            EXECUTOR.schedule(() ->  globalTextChannel.sendMessage(resultW.replaceAll("%p", argument).replaceAll("%u", mentions)).queue(),
                                                    1, TimeUnit.SECONDS);
                                        }
                                        break;
                                }
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
                                String mention = e.getMessage().getAuthor().getAsMention();
                                String discord = e.getMessage().getAuthor().getName() + "#" + e.getMessage().getAuthor().getDiscriminator();
                                plugin.getLogsEmbedBuilder().setTitle(plugin.getLanguageManager().get("WhitelistLogEmbedTitle"));
                                plugin.getLogsEmbedBuilder().setColor(Color.decode(plugin.getLanguageManager().get("LogsEmbedColor")));
                                plugin.getLogsEmbedBuilder().setDescription(plugin.getLanguageManager().get("WhitelistLogEmbedDescription").replaceAll("%p", argument).replaceAll("%u", mention).replaceAll("%d", discord));
                                EXECUTOR.schedule(() -> textChannel.sendMessageEmbeds(plugin.getLogsEmbedBuilder().build()).queue(null, new ErrorHandler()
                                                .ignore(ErrorResponse.UNKNOWN_CHANNEL)),
                                        1, TimeUnit.SECONDS);
                                break;
                        }
                    }
                }
            }
        }
    }
    public List<String> getStringList(String path){
        return plugin.getConfig().getStringList(path);
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}