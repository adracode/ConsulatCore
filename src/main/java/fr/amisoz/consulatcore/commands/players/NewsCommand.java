package fr.amisoz.consulatcore.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;

public class NewsCommand extends ConsulatCommand {

    public NewsCommand() {
        super("/news", 0, Rank.JOUEUR);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        String newsMessage = "§7§l§m-------§r§7§l[ §r§6ConsulatNews §r§7§l§m]§m-------"
                + "\n§r§cModifications du 01/03/2020"
                + "\n§r§7§l§m-------------------------------------------"
                + "\n§r§7- §6Le /shop list est désormais fonctionnel à 100%"
                + "\n§r§7- §6Retrait des admins du /baltop"
                + "\n§r§7- §6Retrait de la possibilité de modifier le type d'un spawner"
                + "\n§r§c[Correction bug]§6 Il arrivait que les shops ne soient pas décomptés & que la limite soit atteinte alors que ce n'était pas le cas"
                + "\n§r§c- Note : Le plugin de duel arrive très vite, le plugin de ville.. vite :p.";

        sender.sendMessage(newsMessage);
    }
}
