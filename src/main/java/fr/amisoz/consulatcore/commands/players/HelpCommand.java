package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;

public class HelpCommand extends ConsulatCommand {

    public HelpCommand() {
        super("help", "/help", 0, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("help"));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        String helpMessage = "§7§l§m-------§r§7§l[ §r§6ConsulatHelp §r§7§l§m]§m-------"
                + "\n§r§cAdministration §7: §c§oShazen, Thomeryc, Elfas_"
                + "\n§r§9Développement §7: §9§oElfas_, adracode"
                + "\n§r§7§l§m-------------------------------------------"
                + "\n§r§6/spawn §r§7: Téléportation au spawn"
                + "\n§r§6/mp <Joueur> <Message> §r§7:  Envoyer un message privé à un joueur"
                + "\n§r§6/claim §r§7: §7Faire §7ça §7deux §7fois §7pour §7délimiter §7ta zone, §7puis §7une §73e §7fois §7pour §7valider §7le §7claim. §7Si §7tu §7as §7mal §7/claim §7ta §7zone, §7alors §7déconnectes §7pour tout réinitialiser "
                + "\n§r§6/tpa <Joueur> §r§7:  Envoyer une demande de téléportation §o(Payant : 10€)"
                + "\n§r§6/access <Joueur> §r§7:  Ajoute le joueur §7dans le claim où §7tu te trouves"
                + "\n§r§6/access remove <Joueur> §r§7:  Retire le§7 joueur dans §7le claim §7où tu §7te trouves"
                + "\n§r§6/sethome <Nom> §r§7: Ajoute un home dans ton claim"
                + "\n§r§6/delhome <Nom> §r§7: Supprime un de tes homes"
                + "\n§r§6/home <Nom> §r§7: Te téléporte à ton home"
                + "\n§r§6/unclaim §r§7: Unclaim le claim dans lequel tu te trouves en te remboursant de 70%"
                + "\n§r§6Le shop §6est situé au spawn, §6il te permet de vendre §6différents item à différents prix §6afin §6d'obtenir de la monnaie §6pour les tp, les claims, etc.";

        sender.sendMessage(helpMessage);
    }
}
