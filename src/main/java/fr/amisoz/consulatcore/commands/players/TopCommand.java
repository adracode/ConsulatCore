package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class TopCommand extends ConsulatCommand {
    
    public TopCommand(){
        super(ConsulatCore.getInstance(), "top");
        setDescription("Se téléporter à la surface").
                setUsage("/top - Se TP à la surface").
                suggest((listener) -> {
                    SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayer(listener);
                    return player != null && player.hasPerkTop();
                });
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(!player.hasPerkTop()){
            sender.sendMessage(Text.DONT_HAVE_PERK);
            return;
        }
        Location playerLocation = sender.getPlayer().getLocation();
        Block higherBlock = sender.getPlayer().getWorld().getHighestBlockAt(playerLocation);
        sender.getPlayer().teleportAsync(new Location(playerLocation.getWorld(), playerLocation.getX(), higherBlock.getY(), playerLocation.getZ()));
        sender.sendMessage(Text.TOP_TELEPORTED);
    }
}
