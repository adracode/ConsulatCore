package fr.amisoz.consulatcore.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class SiteCommand extends ConsulatCommand {

    private TextComponent site;
    
    public SiteCommand() {
        super("site", "/site", 0, Rank.JOUEUR);
        site = new TextComponent(ChatColor.GREEN + "Clique ici pour accéder au site");
        site.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://leconsulat.fr"));
        site.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Clique pour accéder au site").create()));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args) {
        sender.sendMessage(site);
    }
}
