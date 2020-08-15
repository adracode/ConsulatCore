package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand extends ConsulatCommand {
    
    private TextComponent discord;
    
    public DiscordCommand(){
        super(ConsulatCore.getInstance(), "discord");
        setDescription("Affiche un lien d'invitation").
                setUsage("/discord - Affiche un lien d'invitation").
                setRank(Rank.JOUEUR).
                suggest();
        discord = new TextComponent("§aClique ici pour accéder au discord");
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/xCm8hAc"));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour accéder au discord").create()));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.sendMessage(discord);
    }
}
