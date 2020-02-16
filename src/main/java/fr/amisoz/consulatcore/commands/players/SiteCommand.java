package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class SiteCommand extends ConsulatCommand {

    public SiteCommand() {
        super("/site", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        TextComponent textComponent = new TextComponent(ChatColor.GREEN + "Clique ici pour accéder au site");
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://leconsulat.fr"));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Clique pour accéder au site").create()));

        getPlayer().spigot().sendMessage(textComponent);
    }
}
