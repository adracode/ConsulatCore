package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class NewsCommand extends ConsulatCommand {

    public NewsCommand() {
        super("/news", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        String newsMessage = "§7§l§m-------§r§7§l[ §r§6ConsulatNews §r§7§l§m]§m-------"
                + "\n§r§cModifications du 09/02/2020"
                + "\n§r§7§l§m-------------------------------------------"
                + "\n§r§7- §6Mise en place du site"
                + "\n§r§7- §6/baltop : Donne accès aux 10 joueurs les plus riches"
                + "\n§r§7- §6/claimlist : Donne la liste des claims (Et la liste des personnes access par claim)"
                + "\n§r§c[Correction bug]§6 Lorsque l'on faisait /tpa, on avait un message rouge même en utilisant bien la commande"
                + "\n§r§c[Correction bug]§6 Un mot manquait lorsque l'on retirait quelqu'un de son claim"
                + "\n§r§c[Correction bug]§6 On ne peut plus retirer l'objet d'un item frame avec un trident"
                + "\n§r§c[Correction bug]§6 Désormais, lors d'un rename, on n'a plus besoin d'attendre un redémarrage serveur pour avoir accès à ses claims/access"
                + "\n§r§c- Note : Les bouquins enchantés et les maps sont temporairement désactivés dans les shops joueurs.";

        getPlayer().sendMessage(newsMessage);
    }
}
