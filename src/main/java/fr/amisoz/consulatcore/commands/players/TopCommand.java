package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class TopCommand extends ConsulatCommand {

    public TopCommand() {
        super("top", "/top", 0, Rank.JOUEUR);
        suggest(true, (listener) -> {
                    SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayer(listener);
                    return player != null && player.hasPerkTop();
                }
        );
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(!player.hasPerkTop()){
            sender.sendMessage("§cTu n'as pas ce privilège.");
            return;
        }
        Location playerLocation = sender.getPlayer().getLocation();
        Block higherBlock = sender.getPlayer().getWorld().getHighestBlockAt(playerLocation);
        sender.getPlayer().teleportAsync(new Location(playerLocation.getWorld(), playerLocation.getX(), higherBlock.getY(), playerLocation.getZ()));
        sender.sendMessage("§aTu as été téléporté en haut !");
    }
}
