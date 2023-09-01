package com.windstudio.discordwl.bot.Manager.Discord;

import com.windstudio.discordwl.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordModalManager extends ListenerAdapter {
    private final Main plugin;
    private final JDA jda;
    public DiscordModalManager(Main plugin) {
        this.plugin = plugin;
        this.jda = Main.getJDA();
    }
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent e) {
        switch (e.getModalId()) {
            case "NeedHelpModal":
                TextChannel adminChannel = Main.getJDA().getGuildById(getString("GuildID")).getTextChannelById(getString("AdminChannelID"));
                String first = e.getValue("first").getAsString();
                adminChannel.sendMessage(first).queue();
                e.getInteraction().reply("nice!").queue();
                break;
        }
    }
    public static String getString(String path) { return Main.getPlugin().getConfig().getString(path); }
}
