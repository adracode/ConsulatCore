package fr.amisoz.consulatcore.commands.claims;

import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class GetkeyCommand extends ConsulatCommand {
    
    public GetkeyCommand(){
        super("consulat.core", "getkey", "/getkey", 0, Rank.RESPONSABLE);
        suggest();
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.getPlayer().getInventory().addItem(ClaimManager.getKey());
    }
}
