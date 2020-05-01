package fr.amisoz.consulatcore.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class DiscordCommand extends ConsulatCommand {

    private TextComponent discord;
    
    public DiscordCommand() {
        super("discord", "/discord", 0, Rank.JOUEUR);
        discord = new TextComponent(ChatColor.GREEN + "Clique ici pour accéder au discord");
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/xCm8hAc"));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Clique pour accéder au discord").create()));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage(discord);
    }
}
