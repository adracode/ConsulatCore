package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand extends ConsulatCommand {
    
    public SpawnCommand(){
        super(ConsulatCore.getInstance(), "spawn");
        setDescription("Se téléporter au spawn").
                setUsage("/spawn - Se TP au spawn").
                setRank(Rank.JOUEUR).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        if(args.length == 1 && sender.hasPower(Rank.ADMIN) && args[0].equalsIgnoreCase("set")){
            ConsulatCore.getInstance().setSpawn(sender.getPlayer().getLocation());
            sender.sendMessage("§aLocation définie.");
        } else {
            sender.getPlayer().teleportAsync(ConsulatCore.getInstance().getSpawn());
        }
    }
}
