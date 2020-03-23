package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class TopCommand extends ConsulatCommand {

    public TopCommand() {
        super("/top", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        if(!getCorePlayer().canUp){
            getPlayer().sendMessage(ChatColor.RED + "Tu n'as pas ce privilège.");
            return;
        }

        getPlayer().sendMessage(ChatColor.GREEN + "Tu as été téléporté en haut !");

        Location playerLocation = getPlayer().getLocation();
        Block higherBlock = getPlayer().getWorld().getHighestBlockAt(playerLocation);

        getPlayer().teleport(new Location(playerLocation.getWorld(), playerLocation.getX(), higherBlock.getY(), playerLocation.getZ()));
    }
}
