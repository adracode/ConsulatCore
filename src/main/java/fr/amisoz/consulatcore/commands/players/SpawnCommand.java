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
        if(getArgs().length == 1){
            if(getArgs()[0].equalsIgnoreCase("set") && getConsulatPlayer().getRank().getRankPower() == RankEnum.ADMIN.getRankPower()){
                ConsulatCore.spawnLocation = getPlayer().getLocation();
                getPlayer().sendMessage("§aLocation définie.");
            }
        }else{
            getPlayer().teleport(ConsulatCore.spawnLocation);
        }
    }
}
