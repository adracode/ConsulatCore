package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class HelpCommand extends ConsulatCommand {

    public HelpCommand() {
        super("consulat.core", "help", "/help", 0, Rank.JOUEUR);
        suggest();
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args) {
        String helpMessage = "§7§l§m-------§r§7§l[ §r§6ConsulatHelp §r§7§l]§m-------"
                + "\n§r§cAdministration §7: §c§oShazen, Thomeryc, Elfas_"
                + "\n§r§9Développement §7: §9§oElfas_, adracode"
                + "\n§r§7§l§m-------------------------------------------"
                + "\n§r§6/spawn §r§7: Téléportation au spawn"
                + "\n§r§6/msg <Joueur> <Message> §r§7:  Envoyer un message privé à un joueur"
                + "\n§r§6/claim §r§7: §7Claim le chunk §7dans lequel §7tu te trouves"
                + "\n§r§6/tpa <Joueur> §r§7:  Envoyer une demande de téléportation §o(Payant : " + ConsulatCore.formatMoney(10) + ")"
                + "\n§r§6/access add|addall <Joueur> §r§7:  Ajoute le joueur §7dans le claim où §7tu te trouves|tous tes claims"
                + "\n§r§6/access remove|removeall <Joueur> §r§7:  Retire le§7 joueur dans §7le claim §7où tu §7te trouves|tous tes claims"
                + "\n§r§6/access list §r§7: §7Montre les joueurs §7ayant accès au §7claim dans lequel §7tu te trouves."
                + "\n§r§6/pay <Joueur> <Somme> §r§7: §7Envoie de §7l'argent à un §7joueur."
                + "\n§r§6/sethome <Nom> §r§7: §7Définis ta position §7 de home"
                + "\n§r§6/delhome <Nom> §r§7: Supprime un de tes homes"
                + "\n§r§6/home <Nom> §r§7: Te téléporte à ton home"
                + "\n§r§6/unclaim §r§7: Unclaim le claim dans lequel tu te trouves en te remboursant de 70%"
                + "\n§r§6Le shop §6est situé au spawn, §6il te permet de vendre §6différents item à différents prix §6afin §6d'obtenir de la monnaie §6pour les tp, les claims, etc.";

        sender.sendMessage(helpMessage);
    }
}
