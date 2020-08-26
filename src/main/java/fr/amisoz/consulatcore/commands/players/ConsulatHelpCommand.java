package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.jetbrains.annotations.NotNull;

public class ConsulatHelpCommand extends ConsulatCommand {
    
    private BaseComponent[] help;
    private BaseComponent[] claim;
    private BaseComponent[] ville;
    private BaseComponent[] shop;
    private BaseComponent[] safari;
    
    public ConsulatHelpCommand(){
        super(ConsulatCore.getInstance(), "consulat");
        setDescription("Voir l'aide du Consulat").
                setUsage("/consulat - Voir l'aide du Consulat").
                setRank(Rank.INVITE).
                suggest(LiteralArgumentBuilder.literal("claim"),
                        LiteralArgumentBuilder.literal("ville"),
                        LiteralArgumentBuilder.literal("shop"),
                        LiteralArgumentBuilder.literal("safari"));
        
        HoverEvent details = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Clique pour plus de détails").color(ChatColor.GRAY).create());
        TextComponent title = new TextComponent("\n§7§l§m     §7§l[§6§l Le Consulat §7§l]§m     \n");
        help = new ComponentBuilder(title).append("Le Consulat ").color(ChatColor.RED).append("est un serveur Minecraft vanilla survie et communautaire en 1.14.4, avec de nombreux systèmes:\n").color(ChatColor.GRAY)
                .append("§6- §cClaims§7: Posséder des chunks privés.\n").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/consulat claim")).event(details)
                .append("§6- §cVilles§7: Créer une communauté entre joueurs.\n").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/consulat ville")).event(details)
                .append("§6- §cShops§7: Vendre et acheter des objets.\n").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/consulat shop")).event(details)
                .append("§6- §cSafari§7: Farmer les monstres et animaux.\n").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/consulat safari")).event(details)
                .append("\n").event((ClickEvent)null).event((HoverEvent)null)
                .append("§cCommandes: /help")
                .append("\n")
                .append("§7§oTu peux cliquer pour plus de détails")
                .create();
        claim = new ComponentBuilder(title).append("Tu peux claim un chunk pour §e" + ConsulatCore.formatMoney(Claim.BUY_CLAIM) + " §7afin de le protéger des autres joueurs.\n").color(ChatColor.GRAY)
                .append("Les claims peuvent interagir avec d'autres claims (ex: écoulement d'eau) s'ils ont ").color(ChatColor.GRAY)
                        .append("le même propriétaire").color(ChatColor.RED)
                        .append(" et que ").color(ChatColor.GRAY)
                        .append("l'interaction est active ").color(ChatColor.RED)
                        .append("(/claim options).\n\n").color(ChatColor.GRAY)
                .append("§aCommandes utiles:\n")
                .append("§c/claim §e- §7Claim un chunk.\n")
                .append("§c/claim options §e- §7Gérer le claim.\n")
                .append("§c/access §e- §7Ajouter / retirer des joueurs au claim.\n")
                .append("§c/unclaim §e- §7Rendre sa liberté à un chunk (remboursement: §e" + ConsulatCore.formatMoney(Claim.REFUND) + "§7).\n")
                .append("\n")
                .append("§7§oPour afficher toutes les commandes: §a§o/help claim§7§o.\n")
                .create();
        ville = new ComponentBuilder(title).append("Tu peux créer une ville pour ").color(ChatColor.GRAY)
                .append(ConsulatCore.formatMoney(City.CREATE_TAX)).color(ChatColor.YELLOW)
                .append(" et ajouter des membres pour créer une communauté et partager des claims.\n\n").color(ChatColor.GRAY)
                .append("§aCommandes utiles:\n")
                .append("§c/ville create §e- §7Créer une ville.\n")
                .append("§c/ville options §e- §7Gérer la ville.\n")
                .append("§c/ville invite <joueur> §e- §7Inviter un joueur.\n")
                .append("§c/ville accept <ville> §e- §7Rejoindre une ville.\n")
                .append("\n")
                .append("§7§oPour afficher toutes les commandes: §a§o/ville help§7§o.\n")
                .create();
        shop = new ComponentBuilder(title).append("Les shops te permettent de vendre tes items à n'importe quel prix.\nAu spawn sont disponibles des shops spéciaux te permettant d'acheter et vendre tes items à un prix fixé.\n").color(ChatColor.GRAY)
                .append("\n")
                .append("§aCommandes utiles:\n")
                .append("§c/shop list §e- §7Voir tous les shops disponibles.\n")
                .append("§c/shop locate <item> §e- §7Chercher un type d'item parmis les shops disponibles.\n")
                .append("§c/shop help §e- §7Tutoriel pour créer ton propre shop.\n")
                .create();
        safari = new ComponentBuilder(title).append("Le safari est un monde à part peuplé d'animaux et de monstres que tu peux farmer en illimité.\n" +
                "Ce système permet d'éviter une surcharge importante et d'alléger le serveur.\n").color(ChatColor.GRAY)
                .append("\n")
                .append("§aCommande utile:\n")
                .append("§c/safari §e- §7Se rendre au safari.\n")
                .create();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        if(args.length == 0){
            sender.sendMessage(help);
            return;
        }
        switch(args[0]){
            case "claim":
                sender.sendMessage(claim);
                break;
            case "ville":
                sender.sendMessage(ville);
                break;
            case "shop":
                sender.sendMessage(shop);
                break;
            case "safari":
                sender.sendMessage(safari);
                break;
        }
    }
    
}
