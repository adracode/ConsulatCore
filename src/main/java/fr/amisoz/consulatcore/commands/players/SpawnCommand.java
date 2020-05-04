package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class SpawnCommand extends ConsulatCommand {
    
    public SpawnCommand(){
        super("spawn","/spawn", 0, Rank.JOUEUR);
        suggest(true);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length == 1 && sender.hasPower(Rank.ADMIN) && args[0].equalsIgnoreCase("set")){
            ConsulatCore.getInstance().setSpawn(sender.getPlayer().getLocation());
            sender.sendMessage("§aLocation définie.");
        } else {
            sender.getPlayer().teleport(ConsulatCore.getInstance().getSpawn());
        }
    }
}
