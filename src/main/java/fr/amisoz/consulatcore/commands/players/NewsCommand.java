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
                + "\n§r§cModifications du 19/01/2020"
                + "\n§r§7§l§m-------------------------------------------"
                + "\n§r§7- §6Modifications de commandes pour la modération"
                + "\n§r§7- §6Modifications d'outils pour le développement"
                + "\n§r§7- §6Retrait des messages d'achievements"
                + "\n§r§7- §6Pour activer l'IA aux mobs, on peut désormais faire clique droit dessus"
                + "\n§r§7- §6Mise à jour PaperSpigot"
                + "\n§r§c[Correction bug]§6 Les ravageurs pouvaient casser dans les claims"
                + "\n§r§c[Correction bug]§6 Les itemframe vides pouvaient être cassés"
                + "\n§r§c[Correction bug]§6 Les claims d\'une ligne avaient un prix erroné";
        getPlayer().sendMessage(newsMessage);
    }
}
