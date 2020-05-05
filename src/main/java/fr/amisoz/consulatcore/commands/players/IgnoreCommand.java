package fr.amisoz.consulatcore.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.entity.Player;

public class IgnoreCommand extends ConsulatCommand {

    public IgnoreCommand(String usage, int argsMin, Rank rankMinimum) {
        super("ignore", "/ignore <Joueur> | /ignore remove <Joueur>", 1, Rank.JOUEUR);
    }

    //TODO
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){

    }


}
