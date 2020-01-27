package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class SpawnCommand extends ConsulatCommand {

    public SpawnCommand() {
        super("/spawn", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        getPlayer().teleport(ConsulatCore.spawnLocation);
    }
}
