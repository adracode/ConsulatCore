package fr.leconsulat.core.commands.players;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
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
            SurvivalPlayer player = (SurvivalPlayer)sender;
            if(player.isInCombat()){
                player.sendMessage(Text.IN_COMBAT);
                return;
            }
            sender.getPlayer().teleportAsync(ConsulatCore.getInstance().getSpawn());
        }
    }
}
