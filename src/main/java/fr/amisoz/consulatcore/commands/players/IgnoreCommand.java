package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class IgnoreCommand extends ConsulatCommand {

    public IgnoreCommand(String usage, int argsMin, RankEnum rankMinimum) {
        super("/ignore <Joueur> | /ignore remove <Joueur>", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {

    }


}
