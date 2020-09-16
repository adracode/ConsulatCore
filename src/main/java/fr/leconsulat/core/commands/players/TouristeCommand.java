package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class TouristeCommand extends ConsulatCommand {

    public TouristeCommand() {
        super(ConsulatCore.getInstance(), "touriste");
        setDescription("Te donne les informations du grade touriste").
                setUsage("/touriste - Informations du grade touriste").
                setRank(Rank.INVITE).
                suggest();
    }

    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        TextComponent title = new TextComponent("\n§7§l§m     §7§l[§6§l Le Consulat §7§l]§m     \n");
        BaseComponent[] touriste = new ComponentBuilder(title).append("Le grade Touriste").color(ChatColor.GOLD).append(" est un grade que tu peux acheter via la monnaie IG. Il te donne :\n").color(ChatColor.GRAY)
                .append("§6- §c12%§7 en plus lors d'une vente aux adminshop.\n")
                .append("§6- §c2 homes§7 au lieu de 1.\n")
                .append("§6- §cAccès§7 au serveur full.\n")
                .append("§6- §c4 plots§7 au hub.\n")
                .create();
        sender.sendMessage(touriste);
    }
}
